/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import java.util.Collection;

import org.apache.mina.util.ConcurrentHashSet;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

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
  private Collection<GUIDBinding> bindings = new ConcurrentHashSet<GUIDBinding>();

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
   * @return the GUID for this record.
   */
  public GUID getGuid() {
    return this.guid;
  }

  /**
   * Adds a binding to this record, replacing a previous value if it is present.
   * 
   * @param binding
   *          the binding to add.
   */
  public void addBinding(final GUIDBinding binding) {
    // Remove the old binding value
    if (this.bindings.contains(binding)) {
      this.bindings.remove(binding);
    }
    this.bindings.add(binding);
  }

  /**
   * Gets the current bindings. The return value of this method may change from
   * one call to another as bindings are added/replaced/expired.
   * 
   * @return an array of current bindings for this record
   */
  public NetworkAddress[] getBindings() {
    NetworkAddress[] addresses = new NetworkAddress[this.bindings.size()];
    int i = 0;
    for (GUIDBinding b : this.bindings) {
      addresses[i] = b.getAddress();
      ++i;
    }
    return addresses;
  }

  /**
   * Removes all bindings from this record.
   */
  public void removeAll() {
    this.bindings.clear();
  }

}
