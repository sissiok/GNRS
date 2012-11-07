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
public enum AddressType {
  INET_4_UDP(0,  6);

  private String[] STRINGS = { "IPv4"};

  private int type;
  private int maxLength;

  private AddressType(final int type, final int maxLength) {
    this.type = type;
    this.maxLength = maxLength;
  }

  public static AddressType valueOf(final int s) {
    if (s == INET_4_UDP.type) {
      return INET_4_UDP;
    } 
    return null;
  }

  @Override
  public String toString() {
    return STRINGS[this.type];
  }

  /**
   * Returns this address type value as a short.
   * 
   * @return the short value of this type.
   */
  public int value() {
    return this.type;
  }

  /**
   * Returns the maximum length of this address type.
   * 
   * @return the maximum length of this address type.
   */
  public int getMaxLength() {
    return this.maxLength;
  }
}
