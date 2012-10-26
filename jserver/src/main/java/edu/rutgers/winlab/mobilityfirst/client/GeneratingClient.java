/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import org.apache.mina.core.file.DefaultFileRegion;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mobilityfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mobilityfirst.messages.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mobilityfirst.messages.InsertMessage;
import edu.rutgers.winlab.mobilityfirst.messages.LookupMessage;
import edu.rutgers.winlab.mobilityfirst.messages.MessageType;
import edu.rutgers.winlab.mobilityfirst.structures.GUID;
import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class GeneratingClient extends IoHandlerAdapter implements Runnable {

  static final Logger log = LoggerFactory.getLogger(GeneratingClient.class);

  /**
   * @param args
   * @throws InterruptedException if interrupted while waiting for the clients to finish.
   */
  public static void main(String[] args) throws InterruptedException {
    if (args.length < 3) {
      printUsageInfo();
      return;
    }

    XStream x = new XStream();

    Configuration config = (Configuration) x.fromXML(new File(args[0]));
    log.debug("Loaded configuration file \"{}\".", args[0]);

    int delay = Integer.parseInt(args[1]);
    int numClients = Integer.parseInt(args[2]);
    int numLookups = 100000;

    GeneratingClient[] clients = new GeneratingClient[numClients];
    for (int i = 0; i < clients.length; ++i) {
      clients[i] = new GeneratingClient(config, delay, numLookups);
    }
    
    Thread[] threads = new Thread[numClients];

    
    for (int i = 0; i < clients.length; ++i) {
      threads[i] = new Thread(clients[i]);
    }

    log.info("Created {} clients.", numClients);

    for(int i = 0; i < clients.length; ++i){
      threads[i].start();
    }
    
    for(int i = 0; i < clients.length; ++i){
      threads[i].join();
    }
  }

  public static void printUsageInfo() {
    System.out.println("Usage: <Config File> <Request Delay> <Num Clients>");
  }

  private NioDatagramConnector connector;
  private final Configuration config;

  private final int delay;
  private final int numLookups;

  public GeneratingClient(final Configuration config, final int delay,
      final int numLookups) {
    super();

    this.config = config;
    this.delay = delay;
    this.numLookups = numLookups;

    this.connector = new NioDatagramConnector();
    this.connector.setHandler(this);
    DatagramSessionConfig sessionConfig = this.connector.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);
    DefaultIoFilterChainBuilder chain = this.connector.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(false)));

  }
  
  @Override
  public void run(){
    this.connect();
  }

  public boolean connect() {
    log.debug("Creating connect future.");
    ConnectFuture connectFuture = this.connector.connect(new InetSocketAddress(
        this.config.getServerHost(), this.config.getServerPort()));

    // FIXME: Must call awaitUninterruptably. This is a known issue in MINA <https://issues.apache.org/jira/browse/DIRMINA-911>
    connectFuture.awaitUninterruptibly();

    connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
      @Override
      public void operationComplete(ConnectFuture future) {
        if (future.isConnected()) {
          GeneratingClient.log.info("Connected to {}", future.getSession());
          GeneratingClient.this.generateSamples(future.getSession());
          future.getSession().close(true);
          GeneratingClient.this.connector.dispose(true);
        }
      }

    });

    log.debug("Future listener will handle connection event and start trace.");

    return true;
  }

  void generateSamples(final IoSession session) {
    log.info("Generating {} lookups.",this.numLookups);

    
    String line = null;
    NetworkAddress fromAddress = null;
    try {
      fromAddress = NetworkAddress.fromASCII(this.config.getClientHost());
    } catch (UnsupportedEncodingException uee) {
      log.error(
          "Unable to parse local host name from configuration parameter.", uee);
      return;
    }

    int fromPort = this.config.getClientPort();
    NetworkAddress clientAddress = null;
    try {clientAddress = NetworkAddress.fromASCII(this.config.getClientHost());
    
    }catch(UnsupportedEncodingException uee){
      log.error("Unable to parse client hostname from configuration file.",uee);
      return;
    }
    LookupMessage message = null; 
    try {
      for(int i = 0; i < this.numLookups; ++i) {
        
        message = new LookupMessage();
        message.setDestinationFlag((byte)0);
        message.setGuid(GUID.fromASCII("123"));
        message.setRequestId(i);
        message.setSenderAddress(clientAddress);
        message.setSenderPort(fromPort);

        log.debug("Writing {} to {}", message, session);
        session.write(message);
        try {
          Thread.sleep(this.delay);
        } catch (InterruptedException ie) {
          // Ignored
        }

      }
    } catch (IOException ioe) {
      log.error("Exception occurred while generating lookups.", ioe);
    }
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) {
    log.error("Caught unhandled exception.", cause);
  }

  @Override
  public void messageReceived(IoSession session, Object message) {
    log.debug("[{}] Received {}", session, message);
  }
  
  @Override
  public void sessionCreated(IoSession session){
    log.info("[{}] Session created.",session);
  }
}
