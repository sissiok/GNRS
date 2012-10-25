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
public class NetworkAddress {

  /**
   * Size of a network address in bytes.
   */
  public static final int SIZE_OF_NETWORK_ADDRESS = 30;
  
  private byte[] bytes;

  public byte[] getBytes() {
    return bytes;
  }
  
  
  
}
