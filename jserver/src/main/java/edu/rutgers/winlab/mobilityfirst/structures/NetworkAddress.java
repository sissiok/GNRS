/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.structures;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author Robert Moore
 * 
 */
public class NetworkAddress {

  /**
   * Size of a network address in bytes. Currently 30.
   */
  public static final int SIZE_OF_NETWORK_ADDRESS = 30;

  /**
   * Raw (binary) form of this network address.
   */
  private byte[] bytes;

  /**
   * Converts the specified ASCII-encoded String to a Network Address value.
   * More specifically, the raw bytes of asString, when ASCII-encoded, are
   * stored in the bytes field. The resulting Network Address will be truncated
   * or padded with zeros as necessary.
   * 
   * @param s
   *          the String to convert.
   * @return a Network Address with the value of the String
   * @throws UnsupportedEncodingException
   *           if the String cannot be decoded to ASCII characters
   */
  public static NetworkAddress fromASCII(final String s)
      throws UnsupportedEncodingException {
    if (s == null || s.length() == 0) {
      return null;
    }

    // FIXME: Improve to avoid double-allocation with copy
    byte[] stringBytes = s.getBytes("US-ASCII");
    NetworkAddress address = new NetworkAddress();
    address.setBinaryForm(Arrays.copyOf(stringBytes, SIZE_OF_NETWORK_ADDRESS));
    return address;
  }

  /**
   * Creates a new NetworkAddress from the binary value of the integer. The 4
   * bytes of the integer value are copied into the high bytes (index 0-3) of
   * the network address.
   * 
   * @param i
   *          the integer to create an address from.
   * @return the created network address.
   */
  public static NetworkAddress fromInteger(final int i) {
    NetworkAddress address = new NetworkAddress();
    address.bytes = new byte[SIZE_OF_NETWORK_ADDRESS];
    address.bytes[0] = (byte) (i >> 24);
    address.bytes[1] = (byte) (i >> 16);
    address.bytes[2] = (byte) (i >> 8);
    address.bytes[3] = (byte) (i);

    return address;
  }

  /**
   * Returns the raw (binary) form of this network address as a byte array.
   * 
   * @return this network address as a byte array.
   */
  public byte[] getBinaryForm() {
    return this.bytes;
  }

  /**
   * Sets the new value of this network address from a byte array. The array
   * {@code bytes} must be exactly
   * {@link NetworkAddress#SIZE_OF_NETWORK_ADDRESS} bytes long.
   * 
   * @param bytes
   *          the new value of this network address.
   */
  public void setBinaryForm(byte[] bytes) {
    if (bytes == null || bytes.length != SIZE_OF_NETWORK_ADDRESS) {
      throw new IllegalArgumentException("NetworkAddress must be exactly "
          + SIZE_OF_NETWORK_ADDRESS + " bytes.");
    }
    this.bytes = Arrays.copyOf(bytes, SIZE_OF_NETWORK_ADDRESS);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(SIZE_OF_NETWORK_ADDRESS * 2 + 4);
    sb.append("NA(");

    for(byte b : this.bytes){
      sb.append(String.format("%02x",Byte.valueOf(b)));
    }
    sb.append(')');
    return sb.toString();
  }
  
  @Override
  public int hashCode(){
    return Arrays.hashCode(this.bytes);
  }
  
  @Override
  public boolean equals(Object o){
    if(o instanceof NetworkAddress){
      return this.equals((NetworkAddress)o);
    }
    return super.equals(o);
  }

  /**
   * Determines if this NetworkAddress equals another based on the values of their binary forms.
   * @param na another NetworkAddress
   * @return {@code true} if their binary forms are equal.
   */
  public boolean equals(final NetworkAddress na){
    return Arrays.equals(this.bytes,na.bytes);
  }

}
