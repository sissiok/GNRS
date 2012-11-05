/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.storage;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;

import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 *
 */
public class NetworkAddressMapper {
  private final Trie<NetworkAddress, String> storageTrie = new PatriciaTrie<NetworkAddress, String>(NetworkAddressKeyAnalyzer.create());
  
  public String put(final NetworkAddress na, final String hostname){
    String previous = this.storageTrie.remove(na);
    this.storageTrie.put(na, hostname);
    return previous;
  }
  
  public String get(final NetworkAddress na){
    return this.storageTrie.selectValue(na);
  }
}
