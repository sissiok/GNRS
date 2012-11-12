/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import com.thoughtworks.xstream.XStream;

/**
 * @author Robert Moore
 * 
 */
public class Configuration {

  /**
   * Location of on-disk files for Berkeley DB storage. Defaults to a
   * subdirectory of
   * 
   */
  private String pathToFiles = "bdb";

  /**
   * Size of the in-memory cache used for Berkeley DB, in mebibytes (2<sup>20</sup> bytes).
   */
  private int cacheSizeMiB = 64;

  public String getPathToFiles() {
    return pathToFiles;
  }

  public void setPathToFiles(String pathToFiles) {
    this.pathToFiles = pathToFiles;
  }

  public int getCacheSizeMiB() {
    return cacheSizeMiB;
  }

  public void setCacheSizeMiB(int cacheSizeMB) {
    this.cacheSizeMiB = cacheSizeMB;
  }

  public static void main(String[] args) {
    XStream x = new XStream();
    System.out.println(x.toXML(new Configuration()));
  }
}
