/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import java.util.concurrent.ConcurrentHashMap;

import edu.rutgers.winlab.mfirst.structures.GNRSRecord;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * A simple in-memory GUID storage engine with no persistence after the application terminates.
 * 
 * <p>The implementation is completely threadsafe in the sense that it will not throw a {@code ConcurrentModificationException} if
 * it is access simultaneously by multiple threads.  However, the bindings it stores may NOT be consistent for multi-threaded
 * insert/get operations.</p>
 * 
 * @author Robert Moore
 * 
 */
public class SimpleGUIDStore implements GUIDStore {

  /**
   * Backing map.
   */
  private final ConcurrentHashMap<GUID, GNRSRecord> storageMap = new ConcurrentHashMap<GUID, GNRSRecord>();

  @Override
  public GNRSRecord getBinding(GUID guid) {
    return this.storageMap.get(guid);
  }

  @Override
  public Boolean insertBinding(GUID guid, GUIDBinding binding) {
    GNRSRecord currRecord = this.storageMap.get(guid);
    if (currRecord == null) {
      currRecord = new GNRSRecord(guid);
    }

    currRecord.addBinding(binding);
    return Boolean.TRUE;
  }

}
