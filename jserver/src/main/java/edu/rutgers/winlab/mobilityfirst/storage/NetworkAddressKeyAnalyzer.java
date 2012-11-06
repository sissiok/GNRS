/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.storage;

import org.ardverk.collection.ByteArrayKeyAnalyzer;
import org.ardverk.collection.KeyAnalyzer;

import edu.rutgers.winlab.mobilityfirst.structures.AddressType;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class NetworkAddressKeyAnalyzer implements KeyAnalyzer<NetworkAddress> {

  /**
   * Analyzer that does the actual work.
   */
  private final ByteArrayKeyAnalyzer byteAnalyzer;

  /**
   * Constructor kept private.
   * 
   * @param byteLength
   *          the length of the byte array.
   */
  private NetworkAddressKeyAnalyzer(final AddressType type) {
    super();
    this.byteAnalyzer = ByteArrayKeyAnalyzer.create(type.getMaxLength());
  }

  /**
   * Creates a new NetworkAddressKeyAnalyzer for addresses of the specified
   * length.
   * @param byteLength the length of the addresses in bytes.
   * @return some analyzer for addresses of the specified length.
   */
  public static NetworkAddressKeyAnalyzer create(final AddressType type) {
    return new NetworkAddressKeyAnalyzer(type);
  }

  @Override
  public int compare(NetworkAddress arg0, NetworkAddress arg1) {
    return this.byteAnalyzer.compare(arg0.getValue(), arg1.getValue());
  }

  @Override
  public int bitIndex(NetworkAddress arg0, NetworkAddress arg1) {
    return this.byteAnalyzer.bitIndex(arg0.getValue(), arg1.getValue());
  }

  @Override
  public boolean isBitSet(NetworkAddress arg0, int arg1) {
    return this.byteAnalyzer.isBitSet(arg0.getValue(), arg1);
  }

  @Override
  public boolean isPrefix(NetworkAddress arg0, NetworkAddress arg1) {
    return this.byteAnalyzer.isPrefix(arg0.getValue(), arg1.getValue());
  }

  @Override
  public int lengthInBits(NetworkAddress arg0) {
    if (arg0.getValue() == null) {
      return 0;
    }
    return this.byteAnalyzer.lengthInBits(arg0.getValue());
  }

}
