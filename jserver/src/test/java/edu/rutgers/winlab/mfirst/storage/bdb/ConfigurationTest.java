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
public class ConfigurationTest {

  public static final String TEST_STRING = "/path/to/files";
  
  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.storage.bdb.Configuration}.
   */
  @Test
  public void test() {
    Configuration config = new Configuration();
    config.setCacheSizeMiB(10);
    config.setPathToFiles(TEST_STRING);
    
    Assert.assertEquals(10,config.getCacheSizeMiB());
    Assert.assertEquals(TEST_STRING,config.getPathToFiles());
  }

}
