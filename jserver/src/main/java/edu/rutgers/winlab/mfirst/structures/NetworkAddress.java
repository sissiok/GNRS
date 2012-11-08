/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.structures;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.GNRSServer;

/**
 * Network Address structure based
 * 
 * @author Robert Moore
 * 
 */
public class NetworkAddress {

  /**
   * Logging for this class.
   */
  private static final Logger log = LoggerFactory
      .getLogger(NetworkAddress.class);

  /**
   * The type of address.
   */
  protected AddressType type;

  

  /**
   * Raw (binary) form of this network address. Depends on the type of address.
   */
  private byte[] value;
  
  public NetworkAddress(){
    super();
  }
  
  public NetworkAddress(final AddressType type, final byte[] value){
    super();
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
  public void setValue(byte[] bytes) {
    if(bytes != null && bytes.length > 0xFFFF){
      throw new IllegalArgumentException("NetworkAddress value exceeds maximum length of 65535 bytes.");
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
      return this.equals((NetworkAddress) o);
    }
    return super.equals(o);
  }

  /**
   * Determines if this NetworkAddress equals another based on their type and
   * value.
   * 
   * @param na
   *          another NetworkAddress
   * @return {@code true} if they are equal.
   */
  public boolean equals(final NetworkAddress na) {
    return this.type.equals(na.type) && Arrays.equals(this.value, na.value);
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
  public void setType(AddressType type) {
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
