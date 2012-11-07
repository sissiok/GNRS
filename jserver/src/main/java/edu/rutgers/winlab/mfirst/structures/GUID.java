/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.structures;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author Robert Moore
 * 
 */
public class GUID {

  /**
   * Number of bytes in a GUID value.
   */
  public static final int SIZE_OF_GUID = 20;

  /**
   * Binary representation of the GUID.
   */
  private byte[] bytes;

  /**
   * Converts the specified ASCII-encoded String to a GUID. More specifically,
   * the raw bytes of asString, when ASCII-encoded, are stored in the GUID
   * field. The resulting GUID will be truncated or padded with zeros as
   * necessary.
   * 
   * @param s
   *          the String to convert.
   * @return a GUID with the value of the String
   * @throws UnsupportedEncodingException
   *           if the String cannot be decoded to ASCII characters
   */
  public static GUID fromASCII(String s) throws UnsupportedEncodingException {
    if (s == null || s.length() == 0) {
      return null;
    }

    // FIXME: Improve to avoid double allocation with copy
    byte[] stringBytes = s.getBytes("US-ASCII");
    GUID guid = new GUID();
    guid.setBinaryForm(Arrays.copyOf(stringBytes, SIZE_OF_GUID));
    return guid;
  }

  /**
   * Creates a new GUID from an integer. The 4 bytes of the integer are placed
   * in the first 4 bytes of the GUID value. The remaining bytes are padded with
   * 0's.
   * 
   * @param i
   *          the integer value.
   * @return a GUID with the integer in its high (first) 4 bytes.
   */
  public static GUID fromInt(final int i) {
    GUID g = new GUID();
    g.bytes = new byte[SIZE_OF_GUID];
    g.bytes[0] = (byte) (i >> 24);
    g.bytes[1] = (byte) (i >> 16);
    g.bytes[2] = (byte) (i >> 8);
    g.bytes[3] = (byte) (i);
    return g;
  }

  /**
   * Gets this GUID as a byte array.
   * 
   * @return this GUID in binary form.
   */
  public byte[] getBinaryForm() {
    return this.bytes;
  }

  /**
   * Sets the binary form of this GUID from a byte array.
   * 
   * @param guid
   *          the new value of this GUID.
   */
  public void setBinaryForm(byte[] guid) {
    this.bytes = guid;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(SIZE_OF_GUID * 2 + 6);
    sb.append("GUID(");
    for (byte b : this.bytes) {
      sb.append(String.format("%02x", Byte.valueOf(b)));
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof GUID) {
      return this.equals((GUID) o);
    }
    return super.equals(o);
  }

  /**
   * Determines if this GUID and another are equal based on their binary
   * representations.
   * 
   * @param g
   *          another GUID.
   * @return {@code true} if the other GUID'd binary value is equal to this
   *         GUID's.
   */
  public boolean equals(final GUID g) {
    return Arrays.equals(this.bytes, g.bytes);
  }
}
