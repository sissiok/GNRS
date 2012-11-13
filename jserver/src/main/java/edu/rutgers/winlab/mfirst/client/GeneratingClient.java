/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.client;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;

/**
 * A simple GNRS client that generates lots of lookup messages.
 * 
 * @author Robert Moore
 * 
 */
public class GeneratingClient extends IoHandlerAdapter implements Runnable {

  /**
   * Logging facility for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(GeneratingClient.class);

  /**
   * Rough estimate of how precise the system nanosecond timer is.
   */
  // Linux supports a 50 microsecond precision
  private static final long SLEEP_PRECISION = System.getProperty("os.name")
      .equalsIgnoreCase("linux") ? 50000 : 1000000;

  /**
   * @param args
   * @throws InterruptedException
   *           if interrupted while waiting for the clients to finish.
   */
  public static void main(final String[] args) throws InterruptedException {
    if (args.length < 4) {
      printUsageInfo();
      return;
    }

    XStream x = new XStream();

    Configuration config = (Configuration) x.fromXML(new File(args[0]));
    LOG.debug("Loaded configuration file \"{}\".", args[0]);

    int delay = Integer.parseInt(args[2]);
    int numClients = Integer.parseInt(args[3]);
    int numLookups = Integer.parseInt(args[1]);

    GeneratingClient[] clients = new GeneratingClient[numClients];
    for (int i = 0; i < clients.length; ++i) {
      clients[i] = new GeneratingClient(config, delay, numLookups);
    }

    Thread[] threads = new Thread[numClients];

    for (int i = 0; i < clients.length; ++i) {
      threads[i] = new Thread(clients[i]);
    }

    LOG.info("Created {} clients.", Integer.valueOf(numClients));

    for (int i = 0; i < clients.length; ++i) {
      threads[i].start();
    }

    for (int i = 0; i < clients.length; ++i) {
      threads[i].join();
    }
  }

  /**
   * Prints out how to invoke this client from the command line.
   */
  public static void printUsageInfo() {
    System.out
        .println("Usage: <Config File> <Num Request> <Request Delay> <Num Clients>");
  }

  /**
   * Connector to communicate with the server.
   */
  NioDatagramConnector connector;

  /**
   * Configuration for this client.
   */
  private final Configuration config;

  /**
   * How long to wait between messages, in microseconds.
   */
  private final int delay;
  /**
   * Total number of lookup messages to generate.
   */
  final int numLookups;

  /**
   * Total number of successes.
   */
  final AtomicInteger numSuccess = new AtomicInteger(0);
  
  /**
   * Total number of responses with bindings.
   */
  final AtomicInteger numHits = new AtomicInteger(0);

  /**
   * Total number of failures.
   */
  final AtomicInteger numFailures = new AtomicInteger(0);

  /**
   * Creates a new GeneratingClient with the configuration and delay values
   * provided.
   * 
   * @param config
   *          configuration for the client.
   * @param delay
   *          how long to wait (in microseconds) between messages.
   * @param numLookups
   *          total number of messages to send.
   */
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

    LOG.info(String.format("Assuming timer precision of %,dns.",
        Long.valueOf(SLEEP_PRECISION)));

  }

  @Override
  public void run() {
    this.connect();
  }

  /**
   * Sets up the networking connection to the server.
   * 
   * @return {@code true} if everything goes well, else {@code false}.
   */
  public boolean connect() {
    LOG.debug("Creating connect future.");
    ConnectFuture connectFuture = this.connector.connect(new InetSocketAddress(
        this.config.getServerHost(), this.config.getServerPort()));

    // FIXME: Must call awaitUninterruptably. This is a known issue in MINA
    // <https://issues.apache.org/jira/browse/DIRMINA-911>
    connectFuture.awaitUninterruptibly();

    connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
      @Override
      public void operationComplete(ConnectFuture future) {
        if (future.isConnected()) {
          GeneratingClient.LOG.info("Connected to {}", future.getSession());
          GeneratingClient.this.generateLookups(future.getSession());
          try {
            Thread.sleep(5000);
          } catch (InterruptedException ie) {
            // Ignored
          }
          int succ = GeneratingClient.this.numSuccess.get();
          int total = succ + GeneratingClient.this.numFailures.get();
          float success = ((succ * 1f) / total) * 100;
          float loss = ((GeneratingClient.this.numLookups - total * 1f) / GeneratingClient.this.numLookups) * 100;
          float hits = ((GeneratingClient.this.numHits.get()*1f)/total)*100;
          LOG.info(String.format(
              "Total: %,d  |  Success: %,.2f%%  |  Hits: %,.2f%%  |  Loss: %,.2f%%)",
              Integer.valueOf(total), Float.valueOf(success), Float.valueOf(hits),
              Float.valueOf(loss)));
          future.getSession().close(true);
          GeneratingClient.this.connector.dispose(true);
        }
      }

    });

    LOG.debug("Future listener will handle connection event and start trace.");

    return true;
  }

  /**
   * Creates a stream of lookup messages to send to the server.
   * 
   * @param session
   *          the session on which to send the messages.
   */
  void generateLookups(final IoSession session) {
    LOG.info("Generating {} lookups.", Integer.valueOf(this.numLookups));

    try {
      IPv4UDPAddress.fromASCII(this.config.getClientHost());
    } catch (UnsupportedEncodingException uee) {
      LOG.error(
          "Unable to parse local host name from configuration parameter.", uee);
      return;
    }

    this.config.getClientPort();
    NetworkAddress clientAddress = null;
    try {
      clientAddress = IPv4UDPAddress.fromASCII(this.config.getClientHost()
          + ":" + this.config.getClientPort());

    } catch (UnsupportedEncodingException uee) {
      LOG.error("Unable to parse client hostname from configuration file.", uee);
      return;
    }
    LookupMessage message = null;
    long nextSend = System.nanoTime();
    long lastSend = 0l;
    final Random r = new Random(System.currentTimeMillis());

    for (int i = 0; i < this.numLookups; ++i) {

      message = new LookupMessage();

      message.setGuid(GUID.fromInt(('0' + r.nextInt(10)) << 24));
      message.setRequestId(i);
      message.setOriginAddress(clientAddress);
      lastSend = System.nanoTime();
      WriteFuture future = session.write(message);

      future.awaitUninterruptibly();

      nextSend = lastSend + (this.delay * 1000);
      long waitTime = getNanoSleep(nextSend - System.nanoTime());

      if (waitTime > 0) {
        LockSupport.parkNanos(waitTime);
      }
    }

  }

  /**
   * Determine how long to call on Lock.parkNanos(long) based on estimation of
   * the system sleep precision.
   * 
   * @param desiredSleep
   *          how long we would actually like to sleep.
   * @return the actual sleep time to use.
   */
  public static long getNanoSleep(final long desiredSleep) {
    long halfPrecision = SLEEP_PRECISION / 4;
    long roundHalf = desiredSleep / (halfPrecision);
    return roundHalf * (halfPrecision) - SLEEP_PRECISION;
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) {
    LOG.error("Caught unhandled exception.", cause);
  }

  @Override
  public void messageReceived(IoSession session, Object message) {
    if (message instanceof LookupResponseMessage) {
      this.handleResponse((LookupResponseMessage) message);
    }
  }

  /**
   * Handles a response message from the server.
   * 
   * @param msg
   *          the response message.
   */
  public void handleResponse(final LookupResponseMessage msg) {
    
    if (ResponseCode.SUCCESS.equals(msg.getResponseCode())) {
      this.numSuccess.incrementAndGet();
      if(msg.getBindings() != null && msg.getBindings().length> 0){
        this.numHits.incrementAndGet();
      }
    } else {
      this.numFailures.incrementAndGet();
    }
  }

  @Override
  public void sessionCreated(IoSession session) {
    LOG.info("[{}] Session created.", session);
  }
}
