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

  public String getBindAddress() {
    return bindAddress;
  }

  public void setBindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
  }

  public int getBindPort() {
    return bindPort;
  }

  public void setBindPort(int bindPort) {
    this.bindPort = bindPort;
  }

  public boolean isAscynchronousWrite() {
    return ascynchronousWrite;
  }

  public void setAscynchronousWrite(boolean ascynchronousWrite) {
    this.ascynchronousWrite = ascynchronousWrite;
  }
}
