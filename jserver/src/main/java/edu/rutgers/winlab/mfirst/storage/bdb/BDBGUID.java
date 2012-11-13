/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import java.io.UnsupportedEncodingException;

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
   * String-encoded form of the GUID value. Encoded as a UTF-16 (Big Endian)
   * String.
   */
  @KeyField(1)
  public String guid;

  /**
   * Creates a new, empty GUID.
   */
  public BDBGUID() {
    super();
  }

  /**
   * Creates a BDB GUID value from a GUID.
   * 
   * @param guid
   *          the GUID to construct the new BDBGUID from.
   * @return a new BDBGUID representing the GUID, or {@code null} if UTF-16 is
   *         not supported.
   */
  public static BDBGUID fromGUID(final GUID guid) {
    BDBGUID newBDB = new BDBGUID();
    try {
      newBDB.guid = new String(guid.getBinaryForm(), "UTF-16BE");
    } catch (UnsupportedEncodingException e) {
      return null;
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
    GUID newGuid = new GUID();
    try {
      newGuid.setBinaryForm(this.guid.getBytes("UTF-16BE"));
    } catch (UnsupportedEncodingException e) {
      return null;
    }
    return newGuid;
  }

}
