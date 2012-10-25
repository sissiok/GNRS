/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.structures;

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

  public byte[] getGuid() {
    return this.guid;
  }

  public void setGuid(byte[] guid) {
    this.guid = guid;
  }
}
