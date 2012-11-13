/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * GNRS GUID record for storage in the Berkeley DB store.
 * 
 * @author Robert Moore
 * 
 */
@Entity
public class BDBRecord {

  /**
   * The GUID and primary key of this record. There cannot be two records for
   * the same GUID in the same server.
   */
  @PrimaryKey
  public  BDBGUID guid;

  /**
   * Set of GUID bindings for this GUID.
   */
  public BDBGUIDBinding[] bindings;
  
  
}