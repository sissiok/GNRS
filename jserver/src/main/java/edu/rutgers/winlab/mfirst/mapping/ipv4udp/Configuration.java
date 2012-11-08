/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.mapping.ipv4udp;

/**
 * @author Robert Moore
 *
 */
public class Configuration {
  /**
   * The hashing algorithm to use when converting a GUID to a Network Address
   * for insert or retrieval in the GNRS.
   */
  private String hashAlgorithm = "MD5";
  
//TODO: This is a short-term solution. Need something more flexible.
 /**
  * Filename for network address prefix->AS bindings.
  */
 private String prefixFile = "src/main/resources/prefixes.ipv4";

 // TODO: This is a short-term solution. Need something more flexible.
 /**
  * Filename for AS network binding values.
  */
 private String asBindingFile = "src/main/resources/as-binding.ipv4";

public String getHashAlgorithm() {
  return hashAlgorithm;
}

public void setHashAlgorithm(String hashAlgorithm) {
  this.hashAlgorithm = hashAlgorithm;
}

public String getPrefixFile() {
  return prefixFile;
}

public void setPrefixFile(String prefixFile) {
  this.prefixFile = prefixFile;
}

public String getAsBindingFile() {
  return asBindingFile;
}

public void setAsBindingFile(String asBindingFile) {
  this.asBindingFile = asBindingFile;
}
}
