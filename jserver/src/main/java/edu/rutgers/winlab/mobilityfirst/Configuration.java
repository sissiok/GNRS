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
   * The hashing algorithm to use when converting a GUID to a Network Address
   * for insert or retrieval in the GNRS.
   */
  private String hashAlgorithm = "MD5";

  // TODO: This is a short-term solution. Need something more flexible.
  /**
   * Filename for network address prefix->AS bindings.
   */
  private String prefixFile = "src/main/resources/prefixes.ipv4";

  // TODO: This is a short-term solution. Need something more flexible.
  /**
   * Filename for AS network binding values.
   */
  private String asBindingFile = "src/main/resources/as-binding.ipv4";

  /**
   * The number of replicas to use when inserting or retrieving bindings from
   * the network.
   */
  private int numReplicas = 5;

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

  /**
   * Gets the current number of worker threads used.
   * 
   * @return the number of worker threads in use.
   */
  public int getNumWorkerThreads() {
    return this.numWorkerThreads;
  }

  /**
   * Sets the number of worker threads to use.
   * 
   * @param minWorkerThreads
   *          the new number of worker threads.
   */
  public void setNumWorkerThreads(int minWorkerThreads) {
    this.numWorkerThreads = minWorkerThreads;
  }

  /**
   * Flag to indicate if statistics are being collected.
   * 
   * @return {@code true} if they are being collected, else {@code false}.
   */
  public boolean isCollectStatistics() {
    return this.collectStatistics;
  }

  /**
   * Sets statistics collection on or off.
   * 
   * @param collectStatistics
   *          {@code true} to collect statistics.
   */
  public void setCollectStatistics(boolean collectStatistics) {
    this.collectStatistics = collectStatistics;
  }

  /**
   * Gets the name of the hashing algorithm in use for GUID -> server address
   * bindings.
   * 
   * @return the name of the hashing algorithm.
   */
  public String getHashAlgorithm() {
    return this.hashAlgorithm;
  }

  /**
   * Sets the hashing algorithm to use when mapping GUID values to server
   * addresses.
   * 
   * @param hashAlgorithm
   *          the hashing algorithm to use.
   */
  public void setHashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  /**
   * Gets the name of the network address prefix file loaded at startup.
   * 
   * @return the name of the prefix file used.
   */
  public String getPrefixFile() {
    return this.prefixFile;
  }

  /**
   * Sets the network address prefix filename.
   * 
   * @param prefixFile
   *          the new prefix filename.
   */
  public void setPrefixFile(String prefixFile) {
    this.prefixFile = prefixFile;
  }

  /**
   * Returns the name of the Autonomous System (AS) network address bindings
   * file.
   * 
   * @return the filename of the AS bindings.
   */
  public String getASBindingFile() {
    return this.asBindingFile;
  }

  /**
   * Sets the AS network binding file name.
   * 
   * @param asBindingFile
   *          the new filename.
   */
  public void setASBindingFile(String asBindingFile) {
    this.asBindingFile = asBindingFile;
  }

  public int getNumReplicas() {
    return numReplicas;
  }

  public void setNumReplicas(int numReplicas) {
    this.numReplicas = numReplicas;
  }

}
