/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

/**
 * Configuration class for GNRS Server.
 * 
 * @author Robert Moore
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
   * Type of GUID storage to use.
   */
  private String storeType = "simple";

  /**
   * Configuration filename for the GUID storage object.
   */
  private String storeConfiguration = "berkeleydb.xml";

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
  public void setNumWorkerThreads(final int minWorkerThreads) {
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
  public void setCollectStatistics(final boolean collectStatistics) {
    this.collectStatistics = collectStatistics;
  }

  /**
   * The number of replica GNRS servers to utilize.
   * 
   * @return the current number of replicas.
   */
  public int getNumReplicas() {
    return this.numReplicas;
  }

  /**
   * Sets the number of replica GNRS servers to utilize.
   * 
   * @param numReplicas
   *          the new number of replicas.
   */
  public void setNumReplicas(final int numReplicas) {
    this.numReplicas = numReplicas;
  }

  /**
   * Gets the network type for the server.
   * 
   * @return the network type value.
   */
  public String getNetworkType() {
    return this.networkType;
  }

  /**
   * Sets the network type for the server.
   * <p>
   * Currently-supported types:
   * </p>
   * <table>
   * <thead>
   * <th style="text-align: left">Value</th>
   * <th style="text-align: left">Description</th> </thead>
   * <tr>
   * <td><code>ipv4udp</code></td>
   * <td>Internet Protocol version 4 with UDP.</td>
   * </tr>
   * </table>
   * 
   * @param networkType
   *          the new network type value.
   */
  public void setNetworkType(final String networkType) {
    this.networkType = networkType;
  }

  /**
   * Get the name of the network configuration file.
   * 
   * @return the network configuration filename.
   */
  public String getNetworkConfiguration() {
    return this.networkConfiguration;
  }

  /**
   * Sets the network configuration filename.
   * 
   * @param netConfig
   *          the new network configuration filename.
   */
  public void setNetworkConfiguration(final String netConfig) {
    this.networkConfiguration = netConfig;
  }

  /**
   * Gets the GUID mapping configuration filename.
   * 
   * @return the GUID mapping configuration filename.
   */
  public String getMappingConfiguration() {
    return this.mappingConfiguration;
  }

  /**
   * Sets the GUID mapping configuration filename.
   * 
   * @param mapConfig
   *          the new filename.
   */
  public void setMappingConfiguration(final String mapConfig) {
    this.mappingConfiguration = mapConfig;
  }

  /**
   * Gets the GUID store type value.
   * 
   * @return the type of GUID store to use.
   */
  public String getStoreType() {
    return this.storeType;
  }

  /**
   * Sets the GUID store type value.
   * 
   * @param storeType
   *          the new type of GUID store to use.
   */
  public void setStoreType(final String storeType) {
    this.storeType = storeType;
  }

  /**
   * Gets the filename of the GUID store configuration.
   * 
   * @return the GUID store configuration filename.
   */
  public String getStoreConfiguration() {
    return this.storeConfiguration;
  }

  /**
   * Sets the GUID store configuration filename.
   * 
   * @param storeConfig
   *          the GUID store configuration filename.
   */
  public void setStoreConfiguration(final String storeConfig) {
    this.storeConfiguration = storeConfig;
  }

}
