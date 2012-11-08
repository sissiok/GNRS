/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.GNRSServer;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class IPv4UDPAddress extends NetworkAddress {
  /**
   * Creates a new IPv4+UDP NetworkAddress from the specified value field.
   * 
   * @param value
   *          contains the binary form of the IPv4+UDP address (4-byte IP
   *          address, 2-byte UDP port)
   */
  public IPv4UDPAddress(byte[] value) {
    super(AddressType.INET_4_UDP, value);
  }

  /**
   * Extension of NetworkAddress that provides IPv4/UDP-specific functionality.
   */
  private static final Logger log = LoggerFactory
      .getLogger(IPv4UDPAddress.class);

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

    byte[] newValue = new byte[AddressType.INET_4_UDP.getMaxLength()];
    System.arraycopy(inet.getAddress(), 0, newValue, 0, 4);
    newValue[newValue.length - 2] = (byte) (port >> 8);
    newValue[newValue.length - 1] = (byte) port;

    return new NetworkAddress(AddressType.INET_4_UDP, newValue);
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

    byte[] newValue = new byte[AddressType.INET_4_UDP.getMaxLength()];
    newValue[0] = (byte) (i >> 24);
    newValue[1] = (byte) (i >> 16);
    newValue[2] = (byte) (i >> 8);
    newValue[3] = (byte) (i);

    return new NetworkAddress(AddressType.INET_4_UDP, newValue);
  }

  /**
   * Converts this NetworkAddress into an InetSocketAddress.
   * 
   * @param addr
   *          the network to convert
   * 
   * @return an InetSocketAddress representing the same IP and port combination
   *         as the NetworkAddress, or {@code null} if an error occurs during
   *         conversion.
   */
  public static InetSocketAddress toSocketAddr(final NetworkAddress addr) {
    byte[] value = addr.getValue();
    if (value == null) {
      log.error("Unable to create InetSocketAddress from null bytes.");
      return null;
    }
    try {
      // Last two bytes are the port
      int port = ((value[value.length - 2] << 8) | value[value.length - 1]) & 0xFFFF;
      // First 4 bytes are the IP address
      return new InetSocketAddress(InetAddress.getByAddress(Arrays.copyOf(
          value, 4)), port);

    } catch (UnknownHostException e) {
      log.error("Could not create InetSocketAddress from NetworkAddress.", e);
      return null;
    }
  }
}
