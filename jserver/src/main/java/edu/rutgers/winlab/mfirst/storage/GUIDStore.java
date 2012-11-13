/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import edu.rutgers.winlab.mfirst.GUID;

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
  public GNRSRecord getBindings(final GUID guid);

  /**
   * Adds a GUID binding to this store.
   * 
   * @param guid
   *          the GUID to bind.
   * @param bindings
   *          the new bindings for the GUID.
   * @return {@code true} if the insert succeeds in all replicas
   */
  public boolean appendBindings(final GUID guid, final GUIDBinding... bindings);
  
  /**
   * Replaces all current bindings with the bindings provided.
   * @param guid the GUID to bind.
   * @param bindings the new bindings for the GUID 
   * @return {@code true} if the replace operation succeeds, else {@code false}. 
   */
  public boolean replaceBindings(final GUID guid, final GUIDBinding... bindings );

  /**
   * Must be called before the server starts accessing the storage class. Allows
   * separation of instantiation and initialization of the storage components.
   */
  public void doInit();

  /**
   * Must be called before the server exits so the storage class can perform any
   * necessary clean-up (closing file handles, writing back to disk, etc.).
   */
  public void doShutdown();

}
