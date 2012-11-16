/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
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
 */
public class GeneratingClient extends IoHandlerAdapter implements Runnable {

  /**
   * Logging facility for this class.
   */
  private static final Logger LOG = LoggerFactory
      .getLogger(GeneratingClient.class);

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

    final XStream xStream = new XStream();

    final Configuration config = (Configuration) xStream.fromXML(new File(
        args[0]));
    LOG.debug("Loaded configuration file \"{}\".", args[0]);

    final int delay = Integer.parseInt(args[2]);
    final int numClients = Integer.parseInt(args[3]);
    final int numLookups = Integer.parseInt(args[1]);

    final GeneratingClient[] clients = new GeneratingClient[numClients];
    for (int i = 0; i < clients.length; ++i) {
      clients[i] = new GeneratingClient(config, delay, numLookups);
    }

    final Thread[] threads = new Thread[numClients];

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
  private final transient NioDatagramConnector connector;

  private final transient NioDatagramAcceptor acceptor;

  /**
   * Configuration for this client.
   */
  private final transient Configuration config;

  /**
   * How long to wait between messages, in microseconds.
   */
  private final transient long delay;
  /**
   * Total number of lookup messages to generate.
   */
  private final transient int numLookups;

  /**
   * Total number of successes.
   */
  private final transient AtomicInteger numSuccess = new AtomicInteger(0);

  /**
   * Total number of responses with bindings.
   */
  private final transient AtomicInteger numHits = new AtomicInteger(0);

  /**
   * Total number of failures.
   */
  private final transient AtomicInteger numFailures = new AtomicInteger(0);

  private final Map<Integer, Long> sendTimes = new ConcurrentHashMap<Integer, Long>();

  private final Queue<Long> rtts = new ConcurrentLinkedQueue<Long>();

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

    this.acceptor = new NioDatagramAcceptor();
    this.acceptor.setHandler(this);
    DatagramSessionConfig sessionConfig = this.acceptor.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);
    DefaultIoFilterChainBuilder chain = this.acceptor.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory()));

    this.connector = new NioDatagramConnector();
    this.connector.setHandler(this);
    sessionConfig = this.connector.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);
    chain = this.connector.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory()));

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
    boolean retValue = true;
    try {
      this.acceptor.bind(new InetSocketAddress(this.config.getClientPort()));

      LOG.debug("Creating connect future.");
      final ConnectFuture connectFuture = this.connector
          .connect(new InetSocketAddress(this.config.getServerHost(),
              this.config.getServerPort()));

      // FIXME: Must call awaitUninterruptably. This is a known issue in MINA
      // <https://issues.apache.org/jira/browse/DIRMINA-911>
      connectFuture.awaitUninterruptibly();

      connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
        @Override
        public void operationComplete(final ConnectFuture future) {
          if (future.isConnected()) {
            GeneratingClient.this.perform(future.getSession());
          }
        }

      });

      LOG.debug("Future listener will handle connection event and start trace.");

    } catch (IOException e) {
      LOG.error("Unable to bind to local port.", e);
      retValue = false;
    }
    return retValue;
  }

  /**
   * Sends the requests to the server.
   * 
   * @param session
   *          the session on which to send the messages.
   */
  public void perform(final IoSession session) {
    LOG.info("Connected to {}", session);
    this.generateLookups(session);
    try {
      Thread.sleep(1000);
    } catch (final InterruptedException ie) {
      // Ignored
    }
    int length = this.rtts.size();
    ArrayList<Long> rttList = new ArrayList<Long>(length);
    rttList.addAll(this.rtts);

    Collections.sort(rttList);

    Long median = rttList.get(length / 2);

    LOG.info(String.format("Min: %,dus | Med: %,dus | Max: %,dus", rttList.get(0)/1000,
        median/1000, rttList.get(length - 1)/1000));

    final int succ = GeneratingClient.this.numSuccess.get();
    final int total = succ + GeneratingClient.this.numFailures.get();
    final float success = ((succ * 1f) / total) * 100;
    final float loss = ((GeneratingClient.this.numLookups - total * 1f) / GeneratingClient.this.numLookups) * 100;
    final float hits = ((GeneratingClient.this.numHits.get() * 1f) / total) * 100;
    LOG.info(String.format(
        "Total: %,d  |  Success: %,.2f%%  |  Hits: %,.2f%%  |  Loss: %,.2f%%)",
        Integer.valueOf(total), Float.valueOf(success), Float.valueOf(hits),
        Float.valueOf(loss)));
    session.close(true);
    this.acceptor.dispose(true);
    this.connector.dispose(true);
  }

  /**
   * Creates a stream of lookup messages to send to the server.
   * 
   * @param session
   *          the session on which to send the messages.
   */
  private void generateLookups(final IoSession session) {
    LOG.info("Generating {} lookups.", Integer.valueOf(this.numLookups));

    try {
      final NetworkAddress clientAddress = IPv4UDPAddress.fromASCII(this.config
          .getClientHost() + ":" + this.config.getClientPort());

      LookupMessage message;
      long nextSend = System.nanoTime();
      long lastSend = 0l;
      final Random rand = new Random(this.config.getRandomSeed());

      for (int i = 0; i < this.numLookups; ++i) {

        message = new LookupMessage();
        message.setRecursive(true);

        message.setGuid(GUID.fromInt(('0' + rand.nextInt(10)) << 24));
        message.setRequestId(i);
        message.setOriginAddress(clientAddress);
        lastSend = System.nanoTime();
        final WriteFuture future = session.write(message);
        this.sendTimes.put(Integer.valueOf(i), Long.valueOf(lastSend));

        future.awaitUninterruptibly();

        nextSend = lastSend + (this.delay * 1000);
        final long waitTime = getNanoSleep(nextSend - System.nanoTime());

        if (waitTime > 0) {
          LockSupport.parkNanos(waitTime);
        }
      }
    } catch (final UnsupportedEncodingException uee) {
      LOG.error(
          "Unable to parse local host name from configuration parameter.", uee);
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
    final long halfPrecision = SLEEP_PRECISION / 4;
    final long roundHalf = desiredSleep / (halfPrecision);
    return roundHalf * (halfPrecision) - SLEEP_PRECISION;
  }

  @Override
  public void exceptionCaught(final IoSession session, final Throwable cause) {
    LOG.error("Caught unhandled exception.", cause);
  }

  @Override
  public void messageReceived(final IoSession session, final Object message) {
    final long rcvTime = System.nanoTime();
    LOG.info("Received {} on {}", message, session);
    if (message instanceof LookupResponseMessage) {
      this.handleResponse((LookupResponseMessage) message, rcvTime);
    }
  }

  /**
   * Handles a response message from the server.
   * 
   * @param msg
   *          the response message.
   */
  public void handleResponse(final LookupResponseMessage msg,final long recvNanos) {

    Long startNanos = this.sendTimes.get(Integer.valueOf((int)msg.getRequestId()));
    this.rtts.add(Long.valueOf(recvNanos - startNanos.longValue()));
    
    if (ResponseCode.SUCCESS.equals(msg.getResponseCode())) {
      this.numSuccess.incrementAndGet();
      if (msg.getBindings() != null && msg.getBindings().length > 0) {
        this.numHits.incrementAndGet();
      }
    } else {
      this.numFailures.incrementAndGet();
    }
  }

  @Override
  public void sessionCreated(final IoSession session) {
    LOG.info("[{}] Session created.", session);
  }
}
