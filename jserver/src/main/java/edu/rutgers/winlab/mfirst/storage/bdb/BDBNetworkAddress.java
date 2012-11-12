/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import com.sleepycat.persist.model.Persistent;

import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * BerkeleyDB-compatible version of a NetworkAddress
 * 
 * @author Robert Moore
 * 
 */
@Persistent
public class BDBNetworkAddress {

  /**
   * Network address type value
   */
  public int type;

  /**
   * Binary value of the address
   */
  public byte[] value;

  /**
   * Converts the provided NetworkAddress to a BDBNetworkAddress.
   * 
   * @param na
   *          the source NetworkAddress.
   * @return a new BDBNetworkAddress representing the NetworkAddress.
   */
  public static BDBNetworkAddress fromNetworkAddress(final NetworkAddress na) {
    BDBNetworkAddress newAddr = new BDBNetworkAddress();
    newAddr.type = na.getType().value();
    newAddr.value = na.getValue();
    return newAddr;
  }

  /**
   * Converts this BDBNetworkAddress to a NetworkAddress.
   * 
   * @return a NetworkAddress with the same value as this BDBNetworkAddress.
   */
  public NetworkAddress toNetworkAddress() {
    return new NetworkAddress(AddressType.valueOf(this.type), this.value);
  }
}
