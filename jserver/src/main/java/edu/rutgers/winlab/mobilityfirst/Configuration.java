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
   * Maximum number of queued messages in any of the 3 types (insert, update,
   * lookup). The total possible number of queued messages in the server is 3
   * times this value.
   */
  private int maxQueueSize = 4096;
  
  /**
   * Number of worker threads to utilize.
   */
  private int numWorkerThreads = 1;

  /**
   * Return the server's port for listening for UDP messages.
   * @return the server's listen port.
   */
  public int getListenPort() {
    return this.listenPort;
  }

  /**
   * Set the server's listen port.
   * @param listenPort the listen port.
   */
  public void setListenPort(int listenPort) {
    this.listenPort = listenPort;
  }

  /**
   * Get the IP address to bind to. Useful in a multi-homed server.
   * @return the IP address the server should bind to.
   */
  public String getBindIp() {
    return this.bindIp;
  }

  /**
   * Set the IP address that the server should bind to.
   * @param bindIp the address the server should bind to.
   */
  public void setBindIp(String bindIp) {
    this.bindIp = bindIp;
  }

  /**
   * Return the maximum size of each message queue.
   * @return the maximum size of each message queue.
   */
  public int getMaxQueueSize() {
    return this.maxQueueSize;
  }

  /**
   * Sets the maximum size for each message queue.
   * @param maxQueueSize the maximum size for each message queue.
   */
  public void setMaxQueueSize(int maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
  }

  /**
   * Get the number of worker threads this server should utilize.
   * @return the number of worker threads to use.
   */
  public int getNumWorkerThreads() {
    return this.numWorkerThreads;
  }

  /**
   * Sets the number of worker threads to use.
   * @param numWorkerThreads
   */
  public void setNumWorkerThreads(int numWorkerThreads) {
    this.numWorkerThreads = numWorkerThreads;
  }

}
