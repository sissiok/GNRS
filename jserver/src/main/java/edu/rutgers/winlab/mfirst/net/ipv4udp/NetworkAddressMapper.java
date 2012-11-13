/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * Network Address -> hostname (String) mapping for GNRS servers.
 * 
 * @author Robert Moore
 * 
 */
public class NetworkAddressMapper {

  /**
   * Logging for this class.
   */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory
      .getLogger(NetworkAddressMapper.class);

  /**
   * Prefix trie used to find the closest match of a network address.
   */
  private final Trie<NetworkAddress, String> storageTrie;

  /**
   * Creates a new mapper for the specified NetworkAddress type.
   * 
   * @param type
   *          the AddressType for the mapper.
   */
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

    // FIXME: Issue #9
    // <https://bitbucket.org/romoore/gnrs/issue/9/ipv4-guid-mapping-should-rehash-then-find>

    return this.storageTrie.selectValue(na);
  }
}
