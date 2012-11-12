/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;


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
   * Size of the in-memory cache used for Berkeley DB, in mebibytes
   * (2<sup>20</sup> bytes).
   */
  private int cacheSizeMiB = 64;

  /**
   * The path to where the DB files should be stored.
   * 
   * @return the location of the DB files.
   */
  public String getPathToFiles() {
    return this.pathToFiles;
  }

  /**
   * Sets the path to where the DB files should be stored.
   * 
   * @param pathToFiles
   *          filesystem path to where the DB files should be stored.
   * 
   */
  public void setPathToFiles(String pathToFiles) {
    this.pathToFiles = pathToFiles;
  }

  /**
   * Returns the size of the cache, in mebibytes.
   * @return the size of the cache, in mebibytes.
   */
  public int getCacheSizeMiB() {
    return this.cacheSizeMiB;
  }

  /**
   * Sets the size of the DB cache.
   * @param cacheSizeMB the new size of the cache, in mebibytes.
   */
  public void setCacheSizeMiB(int cacheSizeMB) {
    this.cacheSizeMiB = cacheSizeMB;
  }
}
