/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

/**
 * Configuration class for GNRS Server.
 * 
 * @author Robert Moore
 * 
 */
public class Configuration {

  /**
   * Minimum number of worker threads to utilize.
   */
  private int numWorkerThreads = 1;

  /**
   * Whether or not the server should collect and output statistics to the log.
   */
  private boolean collectStatistics = false;

  

  /**
   * The number of replicas to use when inserting or retrieving bindings from
   * the network.
   */
  private int numReplicas = 5;

  /**
   * The type of networking to use. This value determines the
   * NetworkAccessObject (NAO) the server will utilize.
   */
  private String networkType = "ipv4udp";

  /**
   * Network access object configuration file. Should be changed together with
   * networkType.
   */
  private String networkConfiguration = "net-ipv4.xml";

  /**
   * GUID mapping object configuration file. Should be changed together with
   * networkType.
   */
  private String mappingConfiguration = "map-ipv4.xml";

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

  public int getNumReplicas() {
    return numReplicas;
  }

  public void setNumReplicas(int numReplicas) {
    this.numReplicas = numReplicas;
  }

  public String getNetworkType() {
    return networkType;
  }

  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  public String getNetworkConfiguration() {
    return networkConfiguration;
  }

  public void setNetworkConfiguration(String networkConfiguration) {
    this.networkConfiguration = networkConfiguration;
  }

  public String getMappingConfiguration() {
    return mappingConfiguration;
  }

  public void setMappingConfiguration(String mappingConfiguration) {
    this.mappingConfiguration = mappingConfiguration;
  }

}
