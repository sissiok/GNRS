/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;

import edu.rutgers.winlab.mfirst.structures.AddressType;
import edu.rutgers.winlab.mfirst.structures.NetworkAddress;

/**
 * Network Address -> hostname (String) mapping for GNRS servers.
 * 
 * @author Robert Moore
 * 
 */
public class NetworkAddressMapper {
  /**
   * Prefix trie used to find the closest match of a network address.
   */
  private final Trie<NetworkAddress, String> storageTrie;

  public NetworkAddressMapper(final AddressType type) {
    super();
    this.storageTrie = new PatriciaTrie<NetworkAddress, String>(
        NetworkAddressKeyAnalyzer.create(type));
  }

  /**
   * Inserts a binding into this mapper for the specified network address.
   * 
   * @param na
   *          the network address to map.
   * @param hostname
   *          the mapping for the address.
   * @return the previous mapped value, or {@code null} if there was none.
   */
  public String put(final NetworkAddress na, final String hostname) {
    String previous = this.storageTrie.remove(na);
    this.storageTrie.put(na, hostname);
    return previous;
  }

  /**
   * Returns the closest mapped value for the network address. Network address
   * "distance" is based on the XOR value of the binary form of the network
   * address.
   * 
   * @param na
   *          the address to retrieve the binding for.
   * @return the closest mapped value for the network address or {@code null} if
   *         none exists.
   */
  public String get(final NetworkAddress na) {
    if (this.storageTrie.isEmpty()) {
      return null;
    }
    return this.storageTrie.selectValue(na);
  }
}
