/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.client;

import java.io.File;
import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * @author Robert Moore
 * 
 */
public class TraceClient extends IoHandlerAdapter {
  
  static final Logger log = LoggerFactory.getLogger(TraceClient.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length < 3) {
      printUsageInfo();
      return;
    }

    XStream x = new XStream();
    System.out.println(x.toXML(new Configuration()));
    
    Configuration config = (Configuration) x.fromXML(new File(args[0]));
    log.debug("Loaded configuration file \"{}\".",args[0]);

    File traceFile = new File(args[1]);
    
    log.debug("Loaded trace file \"{}\".",traceFile);

    int delay = Integer.parseInt(args[2]);

    TraceClient client = new TraceClient(config, traceFile, delay);

    log.debug("Configured trace client.");
    client.connect();
    log.debug("Finished main thread.");
  }

  public static void printUsageInfo() {
    System.out.println("Usage: <Config File> <Trace File> <Delay Value>");
  }

  private NioDatagramConnector connector;
  private final Configuration config;
  private final File traceFile;
  private final int delay;

  public TraceClient(final Configuration config, final File traceFile,
      final int delay) {
    super();

    this.config = config;
    this.traceFile = traceFile;
    this.delay = delay;

    this.connector = new NioDatagramConnector();
    this.connector.setHandler(this);
    DatagramSessionConfig sessionConfig = this.connector.getSessionConfig();
    sessionConfig.setReuseAddress(true);

  }

  public boolean connect() {
    log.debug("Creating connect future.");
    ConnectFuture connectFuture = this.connector.connect(new InetSocketAddress(
        this.config.getServerHost(), this.config.getServerPort()));
    
    connectFuture.awaitUninterruptibly();
    
    connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
      @Override
      public void operationComplete(ConnectFuture future) {
        if(future.isConnected()){
          TraceClient.log.info("Connected to {}", future.getSession());
          TraceClient.this.runTrace();
        }
      }

    });
    
    log.debug("Future listener will handle connection event and start trace.");
    
    return true;
  }
  
  void runTrace(){
    log.info("Starting trace from {}.",this.traceFile);
    
  }

}
