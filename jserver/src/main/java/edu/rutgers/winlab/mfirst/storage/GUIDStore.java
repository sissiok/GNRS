/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import edu.rutgers.winlab.mfirst.structures.GNRSRecord;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * Basic interface for storing and retrieving GUID bindings.
 * 
 * @author Robert Moore
 * 
 */
public interface GUIDStore {

  /**
   * Retrieves any current binding for the GUID value.
   * 
   * @param guid
   *          the GUID value.
   * @return any current binding this server has.
   */
  public GNRSRecord getBinding(final GUID guid);

  /**
   * Adds a GUID binding to this store.
   * 
   * @param guid
   *          the GUID to bind.
   * @param binding
   *          the new (or replacement) binding for the GUID.
   * @return {@code true} if the insert succeeds in all replicas
   */
  public Boolean insertBinding(final GUID guid, final GUIDBinding binding);

}
