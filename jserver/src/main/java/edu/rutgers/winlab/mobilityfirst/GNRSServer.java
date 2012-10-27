/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mobilityfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mobilityfirst.messages.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mobilityfirst.messages.InsertAckMessage;
import edu.rutgers.winlab.mobilityfirst.messages.InsertMessage;
import edu.rutgers.winlab.mobilityfirst.messages.LookupMessage;
import edu.rutgers.winlab.mobilityfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mobilityfirst.messages.ResponseCode;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class GNRSServer {

  /**
   * Logging facility for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(GNRSServer.class);

  /**
   * @param args
   *          <Configuration File>
   */
  public static void main(String[] args) {
    log.debug("------------------------");
    log.debug("GNRS Server starting up.");
    log.debug("------------------------");

    if (args.length < 1) {
      log.error("Missing 1 or more command-line arguments.");
      printUsageInfo();
      return;
    }
    XStream x = new XStream();
    log.trace("Loading configuration file \"{}\".", args[0]);
    Configuration config = (Configuration) x.fromXML(new File(args[0]));
    log.debug("Finished parsing configuration file.");
    try {

      // Create the server
      final GNRSServer server = new GNRSServer(config);
      /*
       * The server bound its port and is listening, but isn't yet started.
       * Messages can arrive, but will just be queued until start() is called.
       */

      // Add a hook to capture interrupts and shut down gracefully
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          server.shutdown();
        }
      });

      log.debug("GNRS server object successfully created.");
      server.startup();
      log.trace("GNRS server thread started.");
    } catch (IOException ioe) {
      log.error("Unable to start server.", ioe);
      return;
    }
  }

  /**
   * Prints out a helpful message to the command line. Let the user know how to
   * invoke.
   */
  public static void printUsageInfo() {
    System.out.println("Parameters: <Config file>");
  }

  /*
   * Class stuff below here.
   */

  /**
   * Configuration file for the server.
   */
  final Configuration config;

  /**
   * Flag to shut down the server.
   */
  private boolean keepRunning = true;

  /**
   * Queue for insert messages that have arrived but not yet been processed.
   */
  private final ConcurrentLinkedQueue<MessageContainer> insertMessages = new ConcurrentLinkedQueue<MessageContainer>();

  /**
   * Queue for lookup messages that have arrived but not yet been processed.
   */
  private final ConcurrentLinkedQueue<MessageContainer> lookupMessages = new ConcurrentLinkedQueue<MessageContainer>();

  /**
   * Object for the server to wait/notify on.
   */
  private final Object messageLock = new Object();

  /**
   * Whether or not to collect statistics about performance.
   */
  private final boolean collectStatistics;

  private final Timer statsTimer;

  /**
   * Number of lookups performed since last stats output.
   */
  static AtomicInteger numLookups = new AtomicInteger(0);

  /**
   * Total number of nanoseconds spent processing messages since last stats report.
   */
  static AtomicLong messageLifetime = new AtomicLong(0);

  /**
   * Thread pool for distributing tasks.
   */
  private final ExecutorService workers;

  private NioDatagramAcceptor acceptor;

  /**
   * Creates a new GNRS server with the specified configuration. The server will
   * not start running until the {@code #start()} method is invoked.
   * 
   * @param config
   *          the configuration to use.
   * @throws IOException
   *           if an IOException occurs during server set-up.
   */
  public GNRSServer(final Configuration config) throws IOException {
    super();
    this.config = config;
    this.collectStatistics = this.config.isCollectStatistics();

    if (this.collectStatistics) {
      this.statsTimer = new Timer();
    } else {
      this.statsTimer = null;
    }

    // if (this.config.getNumWorkerThreads() > 0) {
    // this.workers = Executors.newFixedThreadPool(this.config
    // .getNumWorkerThreads());
    // } else {
    // this.workers = Executors.newSingleThreadExecutor();
    // }

    this.acceptor = new NioDatagramAcceptor();
    this.acceptor.setHandler(new MessageHandler(this));

    DefaultIoFilterChainBuilder chain = this.acceptor.getFilterChain();
    // For encoding/decoding our messages
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(true)));
    // Configure extra threads to handle message processing
    int numThreads = this.config.getNumWorkerThreads();
    if (numThreads < 1) {
      numThreads = 1;
    }

    log.debug("Using threadpool of {} threads.", Integer.valueOf(numThreads));
    this.workers = Executors.newFixedThreadPool(numThreads);

    DatagramSessionConfig sessionConfig = this.acceptor.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);

    this.acceptor.bind(new InetSocketAddress(this.config.getListenPort()));

    log.info("Server listening on port {}.",
        Integer.valueOf(this.config.getListenPort()));
  }

  public void startup() {
    if (this.collectStatistics) {
      this.statsTimer.scheduleAtFixedRate(new StatsTask(this), 1000, 1000);
    }
  }

  /**
   * Terminates the server in a graceful way.
   */
  public void shutdown() {
    this.keepRunning = false;
    if (this.collectStatistics) {
      this.statsTimer.cancel();
    }
    this.workers.shutdown();
  }

  /**
   * Processes messages that have arrived at the server.
   * 
   * @param session
   *          the session that the message arrived on.
   * @param message
   *          the message that arrived. Should be a subclass of
   *          {@link AbstractMessage}.
   */
  public void messageArrived(final IoSession session, final Object message) {
    log.debug("[{}] Received message {}", session, message);
    MessageContainer container = new MessageContainer();
    container.session = session;
    container.message = (AbstractMessage) message;
    if (message instanceof InsertMessage) {
      this.workers.submit(new InsertTask(this, container));

    } else if (message instanceof LookupMessage) {
      this.workers.submit(new LookupTask(this, container));
    }
    // Unrecognized or invalid message received
    else {
      log.warn("Unrecognized message: {}", message);
      // Close immediately, don't wait for outstanding write requests.
      session.close(true);
    }
    // Notify the main thread that work can be done.
    synchronized (this.messageLock) {
      this.messageLock.notifyAll();
    }
  }

  private static final class StatsTask extends TimerTask {
    private static final Logger log = LoggerFactory.getLogger(StatsTask.class);
    private final GNRSServer server;
    private long lastTimestamp = System.currentTimeMillis();

    public StatsTask(final GNRSServer server) {
      super();
      this.server = server;
    }

    @Override
    public void run() {
      long totalNanos = GNRSServer.messageLifetime.getAndSet(0l);
      int numLookups = GNRSServer.numLookups.getAndSet(0);
      long now = System.currentTimeMillis();
     

      long timeDiff = now - this.lastTimestamp;
      this.lastTimestamp = now;
      float numSeconds = timeDiff / 1000f;
      float lookupsPerSecond = numLookups / numSeconds;
      float averageLifetimeUsec = numLookups == 0 ? 0
          : ((totalNanos / (float) numLookups) / 1000);
      log.info(String.format(
          "\nLookups: %.3f per second (%.2f s)\nAverage Lifetime: %,.0fus",
          lookupsPerSecond, numSeconds, averageLifetimeUsec));
    }
  }

}
