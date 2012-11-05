/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.structures;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Representation of a set of GUID bindings for GNRS servers.
 * 
 * @author Robert Moore
 * 
 */
public class GNRSRecord {
  /**
   * The GUID of this record.
   */
  private final GUID guid;
  /**
   * The set of bindings for the GUID.
   */
  private Collection<GUIDBinding> bindings = new ConcurrentLinkedQueue<GUIDBinding>();

  /**
   * Creates a new empty record for the specified GUID value.
   * 
   * @param guid
   *          the GUID to bind.
   */
  public GNRSRecord(final GUID guid) {
    super();
    this.guid = guid;
  }

  /**
   * Gets the GUID of this record.
   * 
   * @return
   */
  public GUID getGuid() {
    return this.guid;
  }  
}
