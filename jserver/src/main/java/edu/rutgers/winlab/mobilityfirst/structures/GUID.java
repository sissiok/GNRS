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
public class GUID {

  /**
   * Number of bytes in a GUID value.
   */
  public static final int SIZE_OF_GUID = 20;

  private byte[] guid;

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
    guid.setGuid(Arrays.copyOf(stringBytes, SIZE_OF_GUID));
    return guid;
  }

  public byte[] getGuid() {
    return this.guid;
  }

  public void setGuid(byte[] guid) {
    this.guid = guid;
  }
}
