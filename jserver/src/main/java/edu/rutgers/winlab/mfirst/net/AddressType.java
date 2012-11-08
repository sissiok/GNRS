/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

/**
 * Enum representing the different types of network address values possible.
 * Contains both the {@code type} field and the maximum value of the
 * {@code length} field for each type.
 * 
 * @author Robert Moore
 * 
 */
public enum AddressType {
  /**
   * Repersents an Internet Protocol (IP) version 4 address plus a UDP port.
   */
  INET_4_UDP(0, 6);

  /**
   * Strings for toString() method.
   */
  private static final String[] STRINGS = { "IPv4+UDP" };

  /**
   * Unsigned short representing the type.
   */
  private int type;
  /**
   * Maximum length of the value, specific to each type.
   */
  private int maxLength;

  /**
   * Private constructor.
   * 
   * @param type
   *          the type of address.
   * @param maxLength
   *          the maximum length of an address value.
   */
  private AddressType(final int type, final int maxLength) {
    this.type = type;
    this.maxLength = maxLength;
  }

  /**
   * Converts the provided unsigned short into an AddressType or {@code null} if
   * the type is not unrecognized.
   * 
   * @param s
   *          the unsigned short representing the type.
   * @return an AddressType for the value, or {@code null} if none can be found
   *         that matches.
   */
  public static AddressType valueOf(final int s) {
    if (s == INET_4_UDP.type) {
      return INET_4_UDP;
    }
    return null;
  }

  @Override
  public String toString() {
    return AddressType.STRINGS[this.type];
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
