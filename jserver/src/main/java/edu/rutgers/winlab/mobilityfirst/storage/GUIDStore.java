/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import edu.rutgers.winlab.mobilityfirst.structures.GNRSRecord;
import edu.rutgers.winlab.mobilityfirst.structures.GUID;
import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;

/**
 * @author Robert Moore
 * 
 */
public class GUIDStore {

  private final ConcurrentHashMap<GUID, GNRSRecord> memoryMap = new ConcurrentHashMap<GUID, GNRSRecord>();

  public Future<GNRSRecord> getBinding(GUID guid) {
    return null;
  }

  public Future<Boolean> insertBinding(GUID guid, GUIDBinding binding) {
    return null;
  }

}
