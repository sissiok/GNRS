/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mobilityfirst.messages.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mobilityfirst.messages.InsertMessage;
import edu.rutgers.winlab.mobilityfirst.messages.LookupMessage;
import edu.rutgers.winlab.mobilityfirst.messages.UpdateMessage;

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
  private final ConcurrentLinkedQueue<InsertMessage> insertMessages = new ConcurrentLinkedQueue<InsertMessage>();

  /**
   * Queue for lookup messages that have arrived but not yet been processed.
   */
  private final ConcurrentLinkedQueue<LookupMessage> lookupMessages = new ConcurrentLinkedQueue<LookupMessage>();

  /**
   * Queue for update messages that have arrived but not yet been processed.
   */
  private final ConcurrentLinkedQueue<UpdateMessage> updateMessages = new ConcurrentLinkedQueue<UpdateMessage>();

  /**
   * Thread pool for distributing tasks.
   */
  private final ExecutorService workers;

  /**
   * Creates a new GNRS server with the specified configuration. The server will
   * not start running until the {@code #start()} method is invoked.
   * 
   * @param config
   *          the configuration to use.
   */
  public GNRSServer(final Configuration config) throws IOException {
    this.config = config;

    if (this.config.getNumWorkerThreads() > 0) {
      this.workers = Executors.newFixedThreadPool(this.config
          .getNumWorkerThreads());
    } else {
      this.workers = Executors.newSingleThreadExecutor();
    }

    NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
    acceptor.setHandler(new MessageHandler(this));

    DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(true)));

    DatagramSessionConfig sessionConfig = acceptor.getSessionConfig();
    sessionConfig.setReuseAddress(true);

    acceptor.bind(new InetSocketAddress(this.config.getListenPort()));

    log.info("Server listening on port {}.",
        Integer.valueOf(this.config.getListenPort()));
  }

  /**
   * Terminates the server in a graceful way.
   */
  public void shutdown() {
    this.keepRunning = false;
  }

  public void messageArrived(final IoSession session, final Object message) {
    if (message instanceof InsertMessage) {
      if (!this.insertMessages.offer((InsertMessage) message)) {
        log.warn("Unable to insert {} into the message queue.", message);
        // Close after all write requests have finished
        // TODO: Capture the CloseFuture here and have it processed by a thread
        session.close(false);
      } else {
        log.debug("Inserted {} into the message queue.", message);
      }
    } else if (message instanceof UpdateMessage) {
      if (!this.updateMessages.offer((UpdateMessage) message)) {
        log.warn("Unable to insert {} into the message queue.", message);
        // Close after all write requests have finished
        // TODO: Capture the CloseFuture here and have it processed by a thread
        session.close(false);
      } else {
        log.debug("Inserted {} into the message queue.", message);
      }
    } else if (message instanceof LookupMessage) {
      if (!this.lookupMessages.offer((LookupMessage) message)) {
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
    // Wake up this thread if it was sleeping
    this.interrupt();
  }

  /**
   * Handle messages by priority (Update, Insert, Lookup). Handing them off to a
   * worker if necessary.
   */
  @Override
  public void run() {
    while (this.keepRunning) {
      // Check for update messages first. Want to avoid expiration if possible.
      while (!this.updateMessages.isEmpty()) {

      }

      /*
       * Next handle any new insert messages. These should be forwarded/inserted
       * before lookups.
       */
      while (!this.insertMessages.isEmpty()) {

      }

      /*
       * Finally let's handle any lookup messages. Now that we're probably
       * "caught up" on our new state, we can let others know what it is.
       */
      while (!this.lookupMessages.isEmpty()) {

      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException ie) {
        // Busy work
      }
    }
  }

}
