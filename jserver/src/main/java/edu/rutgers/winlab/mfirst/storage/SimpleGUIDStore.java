/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import java.util.concurrent.ConcurrentHashMap;

import edu.rutgers.winlab.mfirst.GUID;

/**
 * A simple in-memory GUID storage engine with no persistence after the
 * application terminates.
 * <p>
 * The implementation is completely threadsafe in the sense that it will not
 * throw a {@code ConcurrentModificationException} if it is access
 * simultaneously by multiple threads. However, the bindings it stores may NOT
 * be consistent for multi-threaded insert/get operations.
 * </p>
 * 
 * @author Robert Moore
 */
public class SimpleGUIDStore implements GUIDStore {

  /**
   * Backing map.
   */
  private final ConcurrentHashMap<GUID, GNRSRecord> storageMap = new ConcurrentHashMap<GUID, GNRSRecord>();

  @Override
  public GNRSRecord getBindings(final GUID guid) {
    return this.storageMap.get(guid);
  }

  @Override
  public boolean appendBindings(final GUID guid, final GUIDBinding... bindings) {
    GNRSRecord currRecord = this.storageMap.get(guid);
    if (currRecord == null) {
   // First-time insert, so synch
      synchronized (this.storageMap) {
        // Extra work, but only once per record
        currRecord = this.storageMap.get(guid);
        if (currRecord == null) {
          currRecord = new GNRSRecord(guid);
          this.storageMap.put(guid, currRecord);
        }
      }
    }

    if (bindings != null) {
      for (final GUIDBinding b : bindings) {
        currRecord.addBinding(b);
      }
    }

    return true;
  }

  @Override
  public void doInit() {
    // Nothing to do
  }

  @Override
  public void doShutdown() {
    this.storageMap.clear();
  }

  @Override
  public boolean replaceBindings(final GUID guid, final GUIDBinding... bindings) {
    GNRSRecord currRecord = this.storageMap.get(guid);
    if (currRecord == null) {
      // First-time insert, so synch
      synchronized (this.storageMap) {
        // Extra work, but only once per record
        currRecord = this.storageMap.get(guid);
        if (currRecord == null) {
          currRecord = new GNRSRecord(guid);
          this.storageMap.put(guid, currRecord);
        }
      }
    }
    currRecord.removeAll();

    if (bindings != null) {
      for (final GUIDBinding b : bindings) {
        currRecord.addBinding(b);
      }
    }

    return true;
  }

}
