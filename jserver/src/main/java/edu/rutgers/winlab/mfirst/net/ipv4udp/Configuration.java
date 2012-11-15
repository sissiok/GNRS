/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

/**
 * Conifguration settings for the IPv4/UDP networking implementation.
 * 
 * <p>
 * Specific network configuration settings for a GNRS networking interface that
 * utilizes IPv4 and UDP for network communication. The major components are the
 * IP prefixes file (for BGP-like route announcements) and the AS bindings file,
 * which provides routable addresses for the GNRS servers within each Autonomous
 * System (AS).
 * </p>
 * 
 * @author Robert Moore
 * 
 */
public class Configuration {

  /**
   * IP address for the interface on which to listen for incoming messages.
   */
  private String bindAddress = "127.0.0.1";

  /**
   * UDP port on which to listen for incoming connections.
   */
  private int bindPort = 5001;

  /**
   * Flag to indicate whether or not writes should be asynchronous
   * (non-blocking).
   */
  private boolean asynchronousWrite = false;

  /**
   * The address to which the server should bind.
   * 
   * @return the address to bind on, or "" if the server should bind to any
   *         available interface.
   */
  public String getBindAddress() {
    return this.bindAddress;
  }

  /**
   * Sets the binding address for the server.
   * 
   * @param bindAddress
   *          the new binding address.
   */
  public void setBindAddress(final String bindAddress) {
    this.bindAddress = bindAddress;
  }

  /**
   * Gets the bound port for the server.
   * 
   * @return the UDP port to listen for messages.
   */
  public int getBindPort() {
    return this.bindPort;
  }

  /**
   * Sets the binding port for the server.
   * 
   * @param bindPort
   *          the new binding port.
   */
  public void setBindPort(final int bindPort) {
    this.bindPort = bindPort;
  }

  /**
   * Flag to configure (a)synchronous writes to the network interface.
   * 
   * @return {@code true} if network writes are asynchronous (non-blocking).
   */
  public boolean isAsynchronousWrite() {
    return this.asynchronousWrite;
  }

  /**
   * Sets the flag for (a)synchronous writes to the network.
   * 
   * @param asynchronousWrite
   *          {@code true} if network writes should be asynchronous
   *          (non-blocking), or {@code false} for synchronous (blocking)
   *          writes.
   */
  public void setAsynchronousWrite(final boolean asynchronousWrite) {
    this.asynchronousWrite = asynchronousWrite;
  }
}
