/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.GUID;

/**
 * @author Robert Moore
 *
 */
public class BDBGUIDTest {

  @Test
  public void test() {
    
    
   BDBGUID guid = new BDBGUID();
   guid.setGuid("1234");
   Assert.assertEquals("1234",guid.getGuid());
   GUID realGUID = new GUID();
   realGUID.setBinaryForm(new byte[]{0x0,0x1,0x2,0x3,0x4,0x5,0x6,0x7,0x8,0x9,0xa,0xb,0xc,0xd,0xe,0xf,0x10,0x11,0x12,0x13});
   BDBGUID converted = BDBGUID.fromGUID(realGUID);
   Assert.assertTrue(converted.toGUID().equals(realGUID));
   
  }

}
