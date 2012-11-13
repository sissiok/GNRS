/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import java.io.File;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.GNRSServer;
import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.storage.GNRSRecord;
import edu.rutgers.winlab.mfirst.storage.GUIDBinding;
import edu.rutgers.winlab.mfirst.storage.GUIDStore;

/**
 * Implementation of a GUID store using BerkeleyDB for both in-memory caching
 * and on-disk persistent storage.
 * 
 * @author Robert Moore
 * 
 */
public class BerkeleyDBStore implements GUIDStore {

  /**
   * TimerTask for reporting BerkeleyDB-related statistics.
   * 
   * @author Robert Moore
   * 
   */
  private static final class StatsTask extends TimerTask {
    /**
     * Logging for reporting statistics.
     */
    private static final Logger LOG_STATS = LoggerFactory.getLogger(StatsTask.class);

    /**
     * BerkeleyDB environment.
     */
    private final Environment env;

    /**
     * Previous cache miss value.
     */
    private long lastCacheMiss = 0l;

    /**
     * Creates a new task for the provided BerkeleyDB environment.
     * 
     * @param dbEnv
     *          the BDB environment.
     */
    public StatsTask(final Environment dbEnv) {
      super();
      this.env = dbEnv;
    }

    @Override
    public void run() {
      long newCacheMiss = this.env.getStats(null).getNCacheMiss();
      LOG_STATS.info("BDB Cache Misses: {}",
          (Long.valueOf(newCacheMiss - this.lastCacheMiss)));
      this.lastCacheMiss = newCacheMiss;
    }
  }

  /**
   * Logging for this class.
   */
  private static final Logger LOG = LoggerFactory
      .getLogger(BerkeleyDBStore.class);

  /**
   * Configuration parameters for this GUID store.
   */
  private final Configuration config;

  /**
   * Berkeley DB environment configuration.
   */
  private final EnvironmentConfig bdbEnvConfig;

  /**
   * Berkkeley DB environment for this instance.
   */
  private final Environment bdbEnvironment;

  /**
   * Berkeley DB store configuration.
   */
  private final StoreConfig bdbStoreConfig;

  /**
   * The Berkeley DB store object.
   */
  private final EntityStore bdbStore;

  /**
   * Reference back to the server instance.
   */
  private final GNRSServer server;

  /**
   * Timer task for recording BerkeleyDB-related statistics.
   */
  private StatsTask statsTask;

  /**
   * Primary index (GUID) for interacting with the BDB store.
   */
  private PrimaryIndex<BDBGUID, BDBRecord> primaryIndex;

  /**
   * Creates a new GUID Store using a Berkeley DB to back it.
   * 
   * @param configFileName
   *          the name of the configuration file for this GUID store.
   * @param server
   *          reference to the server instance.
   * @throws DatabaseException
   *           if an exception occurs while opening the database.
   */
  public BerkeleyDBStore(final String configFileName, final GNRSServer server)
      throws DatabaseException {
    super();
    XStream x = new XStream();
    this.config = (Configuration) x.fromXML(new File(configFileName));
    File bdbEnvDir = new File(this.config.getPathToFiles());
    if (!bdbEnvDir.exists()) {
      if (bdbEnvDir.isFile()) {
        LOG.error(
            "Unable to create BerkeleyDB environment directory. File exists with same name: {}",
            configFileName);
        throw new IllegalArgumentException(
            String
                .format(
                    "Unable to create BerkeleyDB environment directory. File exists with same name: %s",
                    configFileName));
      }
      if (!bdbEnvDir.mkdirs()) {
        LOG.error("Unable to create some directories for BerkeleyDB environment.");
        throw new IllegalArgumentException(
            "Unable to create some directories for BerkeleyDB environment.");
      }
      if (!bdbEnvDir.canWrite()) {
        throw new IllegalArgumentException(
            "Unable write to directory for BerkeleyDB environment.");
      }
    }

    this.server = server;

    this.bdbEnvConfig = new EnvironmentConfig();
    this.bdbEnvConfig.setAllowCreate(true);
    try {
      this.bdbEnvConfig.setCacheSize(this.config.getCacheSizeMiB() * 1048576);
    } catch (IllegalArgumentException iae) {
      LOG.error(
          String
              .format(
                  "Unable to allocate cache of size %,d. Using 10% of available memory instead.",
                  Integer.valueOf(this.config.getCacheSizeMiB())), iae);
    }
    // Open/create the DB environment
    this.bdbEnvironment = new Environment(
        new File(this.config.getPathToFiles()), this.bdbEnvConfig);

    this.bdbStoreConfig = new StoreConfig();
    this.bdbStoreConfig.setAllowCreate(true);

    // Open/create the actual data store
    this.bdbStore = new EntityStore(this.bdbEnvironment, "GNRS GUIDStore",
        this.bdbStoreConfig);

    // Retrieve the primary index
    this.primaryIndex = this.bdbStore.getPrimaryIndex(BDBGUID.class,
        BDBRecord.class);

  }

  @Override
  public GNRSRecord getBindings(GUID guid) {
    BDBRecord record = this.primaryIndex.get(BDBGUID.fromGUID(guid));
    GNRSRecord returnedRecord = new GNRSRecord(guid);
    if (record != null) {
      for (BDBGUIDBinding binding : record.bindings) {
        returnedRecord.addBinding(binding.toGUIDBinding());
      }
    }
    return returnedRecord;
  }

  @Override
  public boolean appendBindings(GUID guid, GUIDBinding... bindings) {
    // Nothing to append, nothing to do
    if (bindings == null) {
      return true;
    }
    BDBGUID theGuid = BDBGUID.fromGUID(guid);
    // Retrieve the current value
    BDBRecord record = this.primaryIndex.get(theGuid);
    BDBGUIDBinding[] newBindings = null;

    // If the current value exists, extend it
    if (record != null) {

      // No bindings? This isn't good!
      if (record.bindings != null) {
        newBindings = new BDBGUIDBinding[record.bindings.length
            + bindings.length];
        System.arraycopy(record.bindings, 0, newBindings, 0,
            record.bindings.length);
      }

    }

    // If there wasn't a current binding, start fresh
    if (newBindings == null) {
      newBindings = new BDBGUIDBinding[bindings.length];
    }

    int offset = newBindings.length - bindings.length;

    for (int i = 0; i < bindings.length; ++i, ++offset) {
      newBindings[offset] = BDBGUIDBinding.fromGUIDBinding(bindings[i]);
    }

    // System.arraycopy(bindings, 0, newBindings, offset, bindings.length);

    // Wasn't even a record before, so make one.
    if (record == null) {
      record = new BDBRecord();
      record.guid = theGuid;
    }
    record.bindings = newBindings;
    try {
      this.primaryIndex.put(record);
    } catch (DatabaseException dbe) {
      LOG.error("Unable to append binding in Berkeley DB store.", dbe);
      return false;
    }
    return true;
  }

  @Override
  public void doInit() {
    if (this.server != null) {
      if (this.server.getStatsTimer() != null) {
        this.statsTask = new StatsTask(this.bdbEnvironment);
        this.server.getStatsTimer().scheduleAtFixedRate(this.statsTask, 10000l,
            10000l);
      }
    }

  }

  @Override
  public void doShutdown() {
    if (this.statsTask != null) {
      this.statsTask.cancel();
    }

    // TODO: Close database
    try {
      if (this.bdbStore != null) {
        this.bdbStore.close();
      }

    } catch (DatabaseException dbe) {
      LOG.error("Unable to cleanly close Berkeley DB store.", dbe);
    }

    // Clean-up and close the environment
    try {
      if (this.bdbEnvironment != null) {
        this.bdbEnvironment.cleanLog(); // Clean the log before closing
        this.bdbEnvironment.close();
      }
    } catch (DatabaseException dbe) {
      LOG.error("Unable to cleanly close Berkeley DB environment.", dbe);

    }
  }

  @Override
  public boolean replaceBindings(GUID guid, GUIDBinding... bindings) {

    // Nothing to replace with, so just delete
    if (bindings == null) {
      this.primaryIndex.delete(BDBGUID.fromGUID(guid));
      return true;
    }

    BDBGUIDBinding[] newBindings = new BDBGUIDBinding[bindings.length];
    for (int i = 0; i < bindings.length; ++i) {
      newBindings[i] = BDBGUIDBinding.fromGUIDBinding(bindings[i]);
    }

    BDBRecord record = new BDBRecord();
    record.guid = BDBGUID.fromGUID(guid);
    record.bindings = newBindings;

    try {
      this.primaryIndex.put(record);
    } catch (DatabaseException dbe) {
      LOG.error("Unable to append binding in Berkeley DB store.", dbe);
      return false;
    }
    return true;
  }

}
