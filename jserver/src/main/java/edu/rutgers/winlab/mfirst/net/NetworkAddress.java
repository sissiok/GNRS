/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network Address class for GNRS. Represents a network endpoint for
 * communication with or identification of other hosts or content in the
 * network.
 * 
 * @author Robert Moore
 * 
 */
public class NetworkAddress {

  /**
   * Logging for this class.
   */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory
      .getLogger(NetworkAddress.class);

  /**
   * The type of address.
   */
  protected AddressType type;

  /**
   * Raw (binary) form of this network address. Depends on the type of address.
   */
  protected byte[] value;

  /**
   * Creates a new NetworkAddress object with the specified type and value
   * 
   * @param type
   *          the type of the address
   * @param value
   *          the binary value of the address
   */
  public NetworkAddress(final AddressType type, final byte[] value) {
    super();
    if (value != null && value.length > type.getMaxLength()) {
      throw new IllegalArgumentException(
          "NetworkAddress value length is greater than max allowed by " + type);
    }
    this.setType(type);
    this.setValue(value);

  }

  /**
   * Returns the raw (binary) form of this network address as a byte array.
   * 
   * @return this network address as a byte array.
   */
  public byte[] getValue() {
    return this.value;
  }

  /**
   * Sets the new value of this network address from a byte array.
   * 
   * @param bytes
   *          the new value of this network address.
   */
  public final void setValue(byte[] bytes) {
    if (bytes != null && bytes.length > 0xFFFF) {
      throw new IllegalArgumentException(
          "NetworkAddress value exceeds maximum length of 65535 bytes.");
    }
    this.value = bytes;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(this.getLength() * 2 + 4);
    sb.append("NA(");

    for (byte b : this.value) {
      sb.append(String.format("%02x", Byte.valueOf(b)));
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.value);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof NetworkAddress) {
      return this.equalsNA((NetworkAddress) o);
    }
    return super.equals(o);
  }

  /**
   * Determines if this NetworkAddress equals another based on their type and
   * value.
   * 
   * @param address
   *          another NetworkAddress
   * @return {@code true} if they are equal.
   */
  public boolean equalsNA(final NetworkAddress address) {
    return this.type.equals(address.type) && Arrays.equals(this.value, address.value);
  }

  /**
   * Returns the type of address this represents.
   * 
   * @return the address type.
   */
  public AddressType getType() {
    return this.type;
  }

  /**
   * Sets the type for this address.
   * 
   * @param type
   *          the new type.
   */
  public final void setType(AddressType type) {
    this.type = type;
  }

  /**
   * Returns the length of the value of this address.
   * 
   * @return the length of this address in bytes.
   */
  public int getLength() {
    return this.value == null ? 0 : this.value.length;
  }

}
