/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.structures.GUID;

/**
 * Simple interface to be implemented by classes that can hash a GUID value to a
 * set of network addresses.
 * 
 * @author Robert Moore
 * 
 */
public interface GUIDHasher {

  /**
   * Hashes the provided GUID value to produce a collection of
   * {@code numAddresses} network address values.
   * 
   * @param guid
   *          the GUID value to hash.
   * @param type
   *          the type of address to generate
   * @param numAddresses
   *          the number of addresses to generate.
   * @return the set of generated addresses
   * @throws NoSuchAlgorithmException
   *           if the required hashing algorithm is not supported on the host
   *           platform.
   */
  public Collection<NetworkAddress> hash(final GUID guid,
      final AddressType type, final int numAddresses)
      throws NoSuchAlgorithmException;
}
