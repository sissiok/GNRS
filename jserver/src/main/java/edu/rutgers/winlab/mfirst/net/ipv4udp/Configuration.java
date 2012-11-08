/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

/**
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
  private boolean ascynchronousWrite = false;

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
  public void setBindAddress(String bindAddress) {
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
  public void setBindPort(int bindPort) {
    this.bindPort = bindPort;
  }

  /**
   * Flag to configure (a)synchronous writes to the network interface.
   * 
   * @return {@code true} if network writes are asynchronous (non-blocking).
   */
  public boolean isAscynchronousWrite() {
    return this.ascynchronousWrite;
  }

  /**
   * Sets the flag for (a)synchronous writes to the network.
   * 
   * @param ascynchronousWrite
   *          {@code true} if network writes should be asynchronous
   *          (non-blocking), or {@code false} for synchronous (blocking)
   *          writes.
   */
  public void setAscynchronousWrite(boolean ascynchronousWrite) {
    this.ascynchronousWrite = ascynchronousWrite;
  }
}
