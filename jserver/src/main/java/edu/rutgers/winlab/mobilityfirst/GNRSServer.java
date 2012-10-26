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
import java.util.concurrent.TimeUnit;

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
public class GNRSServer extends Thread {

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
      server.start();
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
  private final Configuration config;

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
  volatile int numLookups = 0;

  /**
   * Thread pool for distributing tasks.
   */
  // private final ExecutorService workers;

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

    NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
    acceptor.setHandler(new MessageHandler(this));

    DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
    // For encoding/decoding our messages
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(true)));
    // Configure extra threads to handle message processing
    int minThreads = this.config.getMinWorkerThreads();
    int maxThreads = this.config.getMaxWorkerThreads();
    long threadIdleTime = this.config.getThreadIdleTime();
    if (minThreads < 1) {
      minThreads = 1;
    }
    if (maxThreads < minThreads) {
      maxThreads = minThreads;
    }
    if (threadIdleTime < 1) {
      threadIdleTime = 1000;
    }
    if (log.isDebugEnabled()) {
      log.debug(String.format(
          "Using threadpool of (%d, %d) threads. Lifetime is %,dms.",
          Integer.valueOf(minThreads), Integer.valueOf(maxThreads),
          Long.valueOf(threadIdleTime)));
    }
    chain.addLast("executor", new ExecutorFilter(minThreads, maxThreads,
        threadIdleTime, TimeUnit.MILLISECONDS));

    DatagramSessionConfig sessionConfig = acceptor.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);

    acceptor.bind(new InetSocketAddress(this.config.getListenPort()));

    if (this.collectStatistics) {
      this.statsTimer.scheduleAtFixedRate(new StatsTask(this), 1000, 1000);
    }

    log.info("Server listening on port {}.",
        Integer.valueOf(this.config.getListenPort()));
  }

  /**
   * Terminates the server in a graceful way.
   */
  public void shutdown() {
    this.keepRunning = false;
    if (this.collectStatistics) {
      this.statsTimer.cancel();
    }
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

      if (!this.insertMessages.offer(container)) {
        log.warn("Unable to insert {} into the message queue.", message);
        // Close after all write requests have finished
        // TODO: Capture the CloseFuture here and have it processed by a thread
        session.close(false);
      } else {
        log.debug("Inserted {} into the message queue.", message);
      }
    } else if (message instanceof LookupMessage) {
      if (!this.lookupMessages.offer(container)) {
        log.warn("Unable to insert {} into the message queue.", message);
        // Close after all write requests have finished
        // TODO: Capture the CloseFuture here and have it processed by a thread
        session.close(false);
      } else {
        log.debug("Inserted {} into the message queue.", message);
      }
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

  /**
   * Handle messages by priority (Update, Insert, Lookup). Handing them off to a
   * worker if necessary.
   */
  @Override
  public void run() {
    boolean handledOne = false;
    while (this.keepRunning) {
      handledOne = false;
      /*
       * Next handle any new insert messages. These should be forwarded/inserted
       * before lookups.
       */
      log.debug("Checking for insert messages.");
      while (!this.insertMessages.isEmpty()) {
        // FIXME: Perform real insertion. This is just testing the network
        // protocols
        log.debug("Getting the next insert message.");
        MessageContainer container = this.insertMessages.poll();
        if (container == null) {
          break;
        }
        InsertMessage msg = (InsertMessage) container.message;
        InsertAckMessage response = new InsertAckMessage();
        response.setRequestId(msg.getRequestId());
        response.setResponseCode(ResponseCode.SUCCESS);

        try {
          response.setSenderAddress(NetworkAddress.fromASCII(this.config
              .getBindIp()));
        } catch (UnsupportedEncodingException e) {
          log.error("Unable to parse bind IP for the server. Please check the configuration file.");
          continue;
        }
        response.setSenderPort(this.config.getListenPort() & 0xFFFFFFFFl);
        if (!container.session.isClosing()) {
          log.debug("[{}] Writing {}", container.session, response);
          container.session.write(response);
        } else {
          log.warn("[{}] Unable to write {} to closing session.",
              container.session, response);
        }
      }

      /*
       * Finally let's handle any lookup messages. Now that we're probably
       * "caught up" on our new state, we can let others know what it is.
       */
      log.debug("Checking for lookup messages.");
      while (!this.lookupMessages.isEmpty()) {
        // FIXME: Just a simple hack here to test the protocols
        log.debug("Getting the next lookup message.");
        MessageContainer container = this.lookupMessages.poll();

        ++this.numLookups;

        if (container == null) {
          break;
        }
        LookupMessage message = (LookupMessage) container.message;
        LookupResponseMessage response = new LookupResponseMessage();
        response.setRequestId(message.getRequestId());
        response.setResponseCode(ResponseCode.ERROR);
        try {
          response.setSenderAddress(NetworkAddress.fromASCII(this.config
              .getBindIp()));
        } catch (UnsupportedEncodingException e) {
          log.error("Unable to parse bind IP for the server. Please check the configuration file.");
          continue;
        }
        response.setSenderPort(this.config.getListenPort());
        log.debug("[{}] Writing {}", container.session, response);
        container.session.write(response);
      }
      if (!handledOne) {
        try {
          synchronized (this.messageLock) {
            this.messageLock.wait();
          }
        } catch (InterruptedException ie) {
          // Busy work
        }
      }
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

      int numLookups = this.server.numLookups;
      long now = System.currentTimeMillis();
      // FIXME: Probably gonna lose a few here. AtomicInteger?
      this.server.numLookups = 0;

      long timeDiff = now - this.lastTimestamp;
      this.lastTimestamp = now;
      float numSeconds = timeDiff / 1000f;
      float lookupsPerSecond = numLookups / numSeconds;
      log.info(String.format("Lookups: %.3f per second (%.2f s)",
          lookupsPerSecond, numSeconds));
    }
  }

}