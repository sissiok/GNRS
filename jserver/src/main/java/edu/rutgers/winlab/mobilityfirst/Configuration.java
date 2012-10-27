/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

/**
 * Configuration class for GNRS Server.
 * 
 * @author Robert Moore
 * 
 */
public class Configuration {

  /**
   * UDP port to listen for incoming packets.
   */
  private int listenPort = 5001;

  /**
   * The IP address to bind to, if only a specific interface is desired.
   */
  private String bindIp = "";
  /**
   * Minimum number of worker threads to utilize.
   */
  private int numWorkerThreads = 1;

  /**
   * Whether or not the server should collect and output statistics to the log.
   */
  private boolean collectStatistics = false;

  /**
   * Return the server's port for listening for UDP messages.
   * 
   * @return the server's listen port.
   */
  public int getListenPort() {
    return this.listenPort;
  }

  /**
   * Set the server's listen port.
   * 
   * @param listenPort
   *          the listen port.
   */
  public void setListenPort(int listenPort) {
    this.listenPort = listenPort;
  }

  /**
   * Get the IP address to bind to. Useful in a multi-homed server.
   * 
   * @return the IP address the server should bind to.
   */
  public String getBindIp() {
    return this.bindIp;
  }

  /**
   * Set the IP address that the server should bind to.
   * 
   * @param bindIp
   *          the address the server should bind to.
   */
  public void setBindIp(String bindIp) {
    this.bindIp = bindIp;
  }

  public int getNumWorkerThreads() {
    return this.numWorkerThreads;
  }

  public void setNumWorkerThreads(int minWorkerThreads) {
    this.numWorkerThreads = minWorkerThreads;
  }

  public boolean isCollectStatistics() {
    return this.collectStatistics;
  }

  public void setCollectStatistics(boolean collectStatistics) {
    this.collectStatistics = collectStatistics;
  }

}
