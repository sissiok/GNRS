/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.rutgers.winlab.mfirst.structures.GNRSRecord;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * Simple GUID storage class.
 * 
 * @author Robert Moore
 * 
 */
public class GUIDStore {

  /**
   * In-memory GUID binding store.
   */
  final ConcurrentHashMap<GUID, GNRSRecord> memoryMap = new ConcurrentHashMap<GUID, GNRSRecord>();

  /**
   * Retrieves any current binding for the GUID value.
   * 
   * @param guid
   *          the GUID value.
   * @return any current binding this server has.
   */
  public GNRSRecord getBinding(final GUID guid) {

    // TODO: Contact remote servers
    // TODO: Expiration based on TTL

    return this.memoryMap.get(guid);
  }

  /**
   * Adds a GUID binding to this store.
   * 
   * @param guid
   *          the GUID to bind.
   * @param binding
   *          the new (or replacement) binding for the GUID.
   * @return {@code true} if the insert succeeds in all replicas
   */
  public Future<Boolean> insertBinding(final GUID guid,
      final GUIDBinding binding) {
    GNRSRecord current = this.memoryMap.get(guid);
    if (current == null) {
      current = new GNRSRecord(guid);
      this.memoryMap.put(guid, current);
    }
    current.addBinding(binding);
    return new Future<Boolean>() {

      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public Boolean get() throws InterruptedException, ExecutionException {
        return Boolean.TRUE;
      }

      @Override
      public Boolean get(long timeout, TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
        return Boolean.TRUE;
      }

      @Override
      public boolean isCancelled() {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean isDone() {
        return true;
      }
    };
  }

}
