/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import java.io.UnsupportedEncodingException;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

import edu.rutgers.winlab.mfirst.structures.GUID;

/**
 * BerkeleyDB-compatible version of a GUID.
 * 
 * @author Robert Moore
 * 
 */
@Persistent
public class BDBGUID {

  @KeyField(1)
  public String guid;

  public BDBGUID() {
    super();
  }

  public static BDBGUID fromGUID(final GUID guid) {
    BDBGUID newBDB = new BDBGUID();
    try {
      newBDB.guid = new String(guid.getBinaryForm(), "UTF-16BE");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
    return newBDB;
  }

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
