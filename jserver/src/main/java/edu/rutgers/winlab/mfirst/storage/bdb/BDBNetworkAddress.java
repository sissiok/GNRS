/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import com.sleepycat.persist.model.Persistent;

import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * BerkeleyDB-compatible version of a NetworkAddress
 * 
 * @author Robert Moore
 */
@Persistent
public class BDBNetworkAddress {

  /**
   * Network address type value
   */
  private int type = -1;

  /**
   * Binary value of the address
   */
  private byte[] value;

  /**
   * Converts the provided NetworkAddress to a BDBNetworkAddress.
   * 
   * @param netAddr
   *          the source NetworkAddress.
   * @return a new BDBNetworkAddress representing the NetworkAddress.
   */
  public static BDBNetworkAddress fromNetworkAddress(
      final NetworkAddress netAddr) {
    BDBNetworkAddress newAddr = null;
    if (netAddr != null) {
      newAddr = new BDBNetworkAddress();
      if (netAddr.getType() != null) {
        newAddr.type = netAddr.getType().value();
      }
      newAddr.value = netAddr.getValue();
    }

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

  /**
   * Returns the type value of this network address.
   * 
   * @return the type of this network address.
   */
  public int getType() {
    return this.type;
  }

  /**
   * Sets the type of this network address.
   * 
   * @param type
   *          the type of this network address.
   */
  public void setType(final int type) {
    this.type = type;
  }

  /**
   * Gets the value of this network address.
   * 
   * @return the value of this network address.
   */
  public byte[] getValue() {
    return this.value;
  }

  /**
   * Sets the value of this network address.
   * 
   * @param value
   *          the value of this network address.
   */
  public void setValue(final byte[] value) {
    this.value = value;
  }
}
