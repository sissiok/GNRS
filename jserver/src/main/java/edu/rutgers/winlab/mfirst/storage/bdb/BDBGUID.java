/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

import edu.rutgers.winlab.mfirst.GUID;

/**
 * BerkeleyDB-compatible version of a GUID.
 * 
 * @author Robert Moore
 * 
 */
@Persistent
public class BDBGUID {
  
  /**
   * Error logger.
   */
  private static final transient Logger LOG = LoggerFactory.getLogger(BDBGUID.class);

  /**
   * String-encoded form of the GUID value. Encoded as a UTF-16 (Big Endian)
   * String.
   */
  @KeyField(1)
  public String guid;

//  /**
//   * Creates a new, empty GUID.
//   */
//  public BDBGUID() {
//    super();
//  }

  /**
   * Creates a BDB GUID value from a GUID.
   * 
   * @param guid
   *          the GUID to construct the new BDBGUID from.
   * @return a new BDBGUID representing the GUID, or {@code null} if UTF-16 is
   *         not supported.
   */
  public static BDBGUID fromGUID(final GUID guid) {
    BDBGUID newBDB = null;
    try {
      final String asString = new String(guid.getBinaryForm(), "UTF-16BE");
      newBDB = new BDBGUID();
      newBDB.guid = asString;
    } catch (final UnsupportedEncodingException e) {
      LOG.error("Unable to create string from GUID bytes.", e);
    }
    return newBDB;
  }

  /**
   * Returns a GUID with the same value as this BDBGUID.
   * 
   * @return the GUID with the same value, or {@code null} if UTF-16 is not
   *         supported.
   */
  public GUID toGUID() {
    GUID newGuid = null;
    try {
      final byte[] bytes = this.guid.getBytes("UTF-16BE");
      newGuid = new GUID();
      newGuid.setBinaryForm(bytes);
    } catch (final UnsupportedEncodingException e) {
      LOG.error("Unable to create GUID from String form.",e);
    }
    return newGuid;
  }

  /**
   * Returns the String form of this GUID.
   * @return this GUID as a String.
   */
  public String getGuid() {
    return this.guid;
  }

  /**
   * Sets the String form of this GUID.
   * @param guid the new GUID.
   */
  public void setGuid(final String guid) {
    this.guid = guid;
  }
}
