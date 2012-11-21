/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.storage.GUIDBinding;

/**
 * A very simple cache using an in-memory LRU structure.
 * 
 * @author Robert Moore
 *
 */
public class SimpleCache {
  
  /**
   * The actual cache used.
   */
  private final transient LRUCache<GUID, Collection<GUIDBinding>> cache;
  
  /**
   * Creates a new cache for the specified number of GUID entries.
   * @param size the size of the cache.
   */
  public SimpleCache(final int size){
    super();
    this.cache = new LRUCache<GUID, Collection<GUIDBinding>>(size);
  }
  
  /**
   * Puts a set of bindings into this cache.  Replaces any previous bindings in the cache.
   * @param guid the GUID to bind.
   * @param bindings the new bindings.
   */
  public void put(final GUID guid, final GUIDBinding... bindings){
    final Set<GUIDBinding> bindSet = new HashSet<GUIDBinding>();
    for(GUIDBinding b : bindings){
      bindSet.add(b);
    }
    this.cache.put(guid,bindSet);
  }
  
  /**
   * Puts a set of bindings into this cache.  Replaces any previuos bindingsi n the cache.
   *  The bindings will have the server default values for expiration and weight.
   * @param guid the GUID to bind.
   * @param addresses the network addresses to use in the bindings.
   */
  public void put(final GUID guid, final NetworkAddress... addresses){
    final Set<GUIDBinding> bindSet = new HashSet<GUIDBinding>();
    for(NetworkAddress addx : addresses){
      final GUIDBinding binding = new GUIDBinding();
      binding.setAddress(addx);
      bindSet.add(binding);
    }
    this.cache.put(guid,bindSet);
  }
  
  /**
   * Get the bindings for a GUID from the cache.
   * @param guid the GUID to retrieve
   * @return the bindings (if any) for the GUID stored in the cache.
   */
  public Collection<GUIDBinding> get(final GUID guid){
    return this.cache.get(guid);
  }
}
