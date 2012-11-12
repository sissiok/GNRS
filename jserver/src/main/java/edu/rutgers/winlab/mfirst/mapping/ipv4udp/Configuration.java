/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.mapping.ipv4udp;

/**
 * Configuration object for IPv4+UDP GUID mapper.
 * 
 * @author Robert Moore
 * 
 */
public class Configuration {
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
   * Gets the hash algorithm name.
   * 
   * @return the hash algorithm name.
   */
  public String getHashAlgorithm() {
    return this.hashAlgorithm;
  }

  /**
   * Sets the hash algorithm name.
   * 
   * @param hashAlgorithm
   *          the new algorithm name.
   */
  public void setHashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  /**
   * Gets the name of the file that contains the IP prefixes (BGP table).
   * 
   * @return the name of the IP prefix file.
   */
  public String getPrefixFile() {
    return this.prefixFile;
  }

  /**
   * Sets the name of the file that contains the IP prefixes.
   * 
   * @param prefixFile
   *          the new IP prefix filename.
   */
  public void setPrefixFile(String prefixFile) {
    this.prefixFile = prefixFile;
  }

  /**
   * Gets the name of the Autonomous System (AS) bindings file. The file
   * contains the AS number and network address value for the GNRS server in
   * that system.
   * 
   * @return the name of the AS bindings file.
   */
  public String getAsBindingFile() {
    return this.asBindingFile;
  }

  /**
   * Sets the name of the Autonomous System (AS) bindings file. The file
   * contains the AS number and network address value for the GNRS server in
   * that system.
   * 
   * @param asBindingFile
   *          the name of the AS bindings file.
   */
  public void setAsBindingFile(String asBindingFile) {
    this.asBindingFile = asBindingFile;
  }
}
