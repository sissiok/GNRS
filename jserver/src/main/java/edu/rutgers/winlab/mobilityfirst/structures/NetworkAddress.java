/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.structures;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mobilityfirst.GNRSServer;

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
  private AddressType type;

  /**
   * The length of the address (in bytes).
   */
  private short length;

  /**
   * Raw (binary) form of this network address. Depends on the type of address.
   */
  private byte[] value;

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
  public static NetworkAddress ipv4FromASCII(final String s)
      throws UnsupportedEncodingException {
    if (s == null || s.length() == 0) {
      return null;
    }

    String[] components = s.split(":");

    InetAddress inet;
    try {
      inet = InetAddress.getByName(components[0]);
    } catch (UnknownHostException e) {
      log.error("Unable to parse IPv4 address.", e);
      return null;
    }

    short port = GNRSServer.DEFAULT_PORT;
    if (components.length > 1) {
      port = Short.parseShort(components[1]);
    }

    NetworkAddress address = new NetworkAddress();
    address.value = new byte[AddressType.INET_4_UDP.getMaxLength()];
    System.arraycopy(inet.getAddress(), 0, address.value, 0, 4);
    address.value[address.value.length - 2] = (byte) (port >> 8);
    address.value[address.value.length - 1] = (byte) port;
    address.type = AddressType.INET_4_UDP;
    address.length = (short) address.value.length;
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
  public static NetworkAddress ipv4FromInteger(final int i) {
    NetworkAddress address = new NetworkAddress();
    address.value = new byte[AddressType.INET_4_UDP.getMaxLength()];
    address.value[0] = (byte) (i >> 24);
    address.value[1] = (byte) (i >> 16);
    address.value[2] = (byte) (i >> 8);
    address.value[3] = (byte) (i);
    address.length = (short)address.value.length;
    address.type = AddressType.INET_4_UDP;

    return address;
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
    this.value = bytes;
    this.length = (short) (this.value == null ? 0 : this.value.length);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(this.length * 2 + 4);
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
  public short getLength() {
    return this.length;
  }

}
