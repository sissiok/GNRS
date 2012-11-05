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
   * Size of a network address in bytes.
   */
  public static final int SIZE_OF_NETWORK_ADDRESS = 30;

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
  public static NetworkAddress fromASCII(String s) throws UnsupportedEncodingException {
    if (s == null || s.length() == 0) {
      return null;
    }

    
    // FIXME: Improve to avoid double-allocation with copy
    byte[] stringBytes = s.getBytes("US-ASCII");
    NetworkAddress address = new NetworkAddress();
    address.setBinaryForm(Arrays.copyOf(stringBytes, SIZE_OF_NETWORK_ADDRESS));
    return address;
  }

  public byte[] getBinaryForm() {
    return bytes;
  }

  public void setBinaryForm(byte[] bytes) {
    if (bytes == null || bytes.length != SIZE_OF_NETWORK_ADDRESS) {
      throw new IllegalArgumentException("NetworkAddress must be exactly "
          + SIZE_OF_NETWORK_ADDRESS + " bytes.");
    }
    this.bytes = bytes;
  }

}
