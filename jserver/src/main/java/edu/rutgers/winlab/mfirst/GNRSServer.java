/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.mapping.GUIDMapper;
import edu.rutgers.winlab.mfirst.mapping.ipv4udp.IPv4UDPGUIDMapper;
import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.MessageListener;
import edu.rutgers.winlab.mfirst.net.NetworkAccessObject;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;
import edu.rutgers.winlab.mfirst.net.ipv4udp.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPNAO;

import edu.rutgers.winlab.mfirst.storage.GUIDHasher;
import edu.rutgers.winlab.mfirst.storage.GUIDStore;
import edu.rutgers.winlab.mfirst.storage.MessageDigestHasher;
import edu.rutgers.winlab.mfirst.storage.NetworkAddressMapper;
import edu.rutgers.winlab.mfirst.storage.SimpleGUIDStore;
import edu.rutgers.winlab.mfirst.structures.GNRSRecord;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * Java implementation of a GNRS server.
 * 
 * 
 * @author Robert Moore
 * 
 */
public class GNRSServer implements MessageListener {

  /**
   * Default port value for GNRS servers.
   */
  public static final short DEFAULT_PORT = 5001;

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
   * Object for the server to wait/notify on.
   */
  private final Object messageLock = new Object();

  /**
   * Whether or not to collect statistics about performance.
   */
  private final boolean collectStatistics;

  /**
   * Timer for printing statistics.
   */
  private final Timer statsTimer;

  /**
   * Number of lookups performed since last stats output.
   */
  static AtomicInteger numLookups = new AtomicInteger(0);

  /**
   * Total number of nanoseconds spent processing messages since last stats
   * report.
   */
  static AtomicLong messageLifetime = new AtomicLong(0);

  /**
   * Thread pool for distributing tasks.
   */
  private final ExecutorService workers;

  /**
   * Networking interface for the server.
   */
  private NetworkAccessObject networkAccess;

  /**
   * Mapping provider for converting a GUID to a set of Network Address values.
   */
  public GUIDMapper guidMapper;

  /**
   * GUID binding storage object.
   */
  private final GUIDStore store = new SimpleGUIDStore();

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

    if (!this.createMapper(this.config.getNetworkType())) {
      log.error("Unable to create GUID mapper of type {}",
          this.config.getNetworkType());
      throw new IllegalArgumentException("Unable to create GUID mapper object.");
    }

    // Configure extra threads to handle message processing
    int numThreads = this.config.getNumWorkerThreads();
    if (numThreads < 1) {
      numThreads = 1;
    }

    log.info("Using threadpool of {} threads.", Integer.valueOf(numThreads));
    this.workers = Executors.newFixedThreadPool(numThreads);

    if (!this.createNAO(this.config.getNetworkType())) {
      log.error("Unable to create network access of type {}",
          this.config.getNetworkType());
      throw new IllegalArgumentException(
          "Unable to create network access object.");
    }

  }

  /**
   * Starts any necessary threads.
   */
  public boolean startup() {

    if (this.collectStatistics) {
      this.statsTimer.scheduleAtFixedRate(new StatsTask(this), 1000, 1000);
    }
    return true;
  }

  /**
   * Configures the server to use a specific type of networking.
   * 
   * @param networkType
   *          the network type value to use
   * @return {@code true} if configuration succeeds, else {@code false}.
   */
  // TODO: Add new network types here.
  private boolean createNAO(final String networkType) {
    // IPv4 + UDP
    if ("ipv4udp".equalsIgnoreCase(networkType)) {
      try {
        this.networkAccess = new IPv4UDPNAO(
            this.config.getNetworkConfiguration());
      } catch (IOException ioe) {
        log.error("Unable to create IPv4/UDP network access.", ioe);
        return false;
      }
      this.networkAccess.addMessageListener(this);
    } else {
      log.error("Unrecognized networking type: {}", networkType);
      return false;
    }

    return true;
  }

  private boolean createMapper(final String networkType) {
    // IPv4 + UDP
    if ("ipv4udp".equalsIgnoreCase(networkType)) {
      try {
        this.guidMapper = new IPv4UDPGUIDMapper(
            this.config.getMappingConfiguration());
      } catch (IOException ioe) {
        log.error("Unable to create IPv4/UDP GUID mapper.", ioe);
        return false;
      }
    } else {
      log.error("Unrecognized networking type: {}", networkType);
      return false;
    }

    return true;
  }

  /**
   * Terminates the server in a graceful way.
   */
  public void shutdown() {

    if (this.collectStatistics) {
      this.statsTimer.cancel();
    }
    this.workers.shutdown();
  }

  /**
   * Returns the set of bindings for a GUID value.
   * 
   * @param guid
   *          the GUID value to get bindings for.
   * @return the current binding values.
   */
  public NetworkAddress[] getBindings(final GUID guid) {

    GNRSRecord record = this.store.getBinding(guid);
    if (record == null) {
      return null;
    }

    return record.getBindings();
  }

  public boolean insertBindings(final GUID guid, final NetworkAddress[] bindings) {
    for (NetworkAddress a : bindings) {
      // TODO: Handle the future.
      GUIDBinding b = new GUIDBinding();
      b.setAddress(a);
      b.setTtl(0);
      b.setWeight(0);
      this.store.insertBinding(guid, b);
    }
    return true;
  }

  /**
   * Convenience accessor method for tasks to retrieve the correct mapping for a
   * GUID.
   * 
   * @param guid
   *          the GUID to map.
   * @param numAddresses
   *          the number of addresses of each type to map (number of replicas).
   * @return a Collection containing the appropriate network mappings, or
   *         {@code null} if none could be created.
   */
  public Collection<NetworkAddress> getMappings(final GUID guid,
      final AddressType... types) {
    return this.guidMapper
        .getMapping(guid, this.config.getNumReplicas(), types);
  }

  /**
   * Timer task that reports server-related statistics when called.
   * 
   * @author Robert Moore
   * 
   */
  private static final class StatsTask extends TimerTask {
    /**
     * Logging for this task.
     */
    private static final Logger log = LoggerFactory.getLogger(StatsTask.class);
    /**
     * The server to report statistics about.
     */
    private final GNRSServer server;
    /**
     * The last time statistics were generated.
     */
    private long lastTimestamp = System.currentTimeMillis();

    /**
     * Creates a new task for the specified server.
     * 
     * @param server
     *          the GNRS server to report statistics about.
     */
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
          Float.valueOf(lookupsPerSecond), Float.valueOf(numSeconds),
          Float.valueOf(averageLifetimeUsec)));
    }
  }

  /**
   * Convenience method for tasks to send a message.
   * 
   * @param params
   *          network paramters for the NAO.
   * @param message
   *          the message to send.
   * @see NetworkAccessObject#sendMessage(SessionParameters, AbstractMessage)
   */
  public void sendMessage(final SessionParameters params,
      final AbstractMessage message) {
    this.networkAccess.sendMessage(params, message);
  }

  /**
   * Convenience method for tasks to check if a NetworkAddress references the
   * local server or not.
   * 
   * @param na
   *          the address to check
   * @return {@code true} if the address identifies this server, else
   *         {@code false}.
   * @see NetworkAccessObject#isLocal(NetworkAddress)
   */
  public boolean isLocalAddress(final NetworkAddress na) {
    return this.networkAccess.isLocal(na);
  }

  public NetworkAddress getOriginAddress() {
    return this.networkAccess.getOriginAddress();
  }

  @Override
  public void messageReceived(SessionParameters parameters, AbstractMessage msg) {

    if (msg instanceof InsertMessage) {
      this.workers
          .submit(new InsertTask(this, parameters, (InsertMessage) msg));

    } else if (msg instanceof LookupMessage) {
      this.workers
          .submit(new LookupTask(this, parameters, (LookupMessage) msg));
    }
    // Unrecognized or invalid message received
    else {
      log.warn("Unrecognized message: {}", msg);
      this.networkAccess.endSession(parameters);
    }
    // Notify the main thread that work can be done.
    synchronized (this.messageLock) {
      this.messageLock.notifyAll();
    }

  }

}
