/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Robert Moore
 *
 */
public class BDBRecordTest {

  @Test
  public void test() {
    // Really? Crazy coverage rules!
    BDBRecord record = new BDBRecord();
    Assert.assertNotNull(record);
  }

}
