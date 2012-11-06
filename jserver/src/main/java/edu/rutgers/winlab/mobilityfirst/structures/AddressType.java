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
  UNKNOWN((short) 0, (short) 0),

  INET_4_UDP((short) 1, (short) 6);

  private String[] STRINGS = { "UNKNOWN", "IPv4"};

  private short type;
  private short maxLength;

  private AddressType(final short type, final short maxLength) {
    this.type = type;
    this.maxLength = maxLength;
  }

  public static AddressType valueOf(final short s) {
    if (s == INET_4_UDP.type) {
      return INET_4_UDP;
    } 
    return UNKNOWN;
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
  public short getType() {
    return this.type;
  }

  /**
   * Returns the maximum length of this address type.
   * 
   * @return the maximum length of this address type.
   */
  public short getMaxLength() {
    return this.maxLength;
  }
}
