/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.structures;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Robert Moore
 *
 */
public class GNRSRecord {
  private final GUID guid;
  private Collection<GUIDBinding> bindings = new ConcurrentLinkedQueue<GUIDBinding>();
  
  public GNRSRecord(final GUID guid){
    super();
    this.guid = guid;
  }
  
  public GUID getGuid() {
    return guid;
  }
 
}
