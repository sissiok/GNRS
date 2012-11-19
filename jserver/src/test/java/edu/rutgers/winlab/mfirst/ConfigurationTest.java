/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Robert Moore
 */
public class ConfigurationTest {

  /**
   * A string for testing.
   */
  public static final String TEST_STRING = "/path/to/file.xml";

  /**
   * General test case for getters/setters in the Configuration class.
   */
  @Test
  public void test() {
    final Configuration config = new Configuration();

    config.setCollectStatistics(true);
    Assert.assertTrue(config.isCollectStatistics());
    config.setCollectStatistics(false);
    Assert.assertFalse(config.isCollectStatistics());

    config.setMappingConfiguration(TEST_STRING);
    Assert.assertEquals(TEST_STRING, config.getMappingConfiguration());

    config.setNetworkConfiguration(TEST_STRING);
    Assert.assertEquals(TEST_STRING, config.getNetworkConfiguration());

    config.setNetworkType(TEST_STRING);
    Assert.assertEquals(TEST_STRING, config.getNetworkType());
    
    config.setStoreConfiguration(TEST_STRING);
    Assert.assertEquals(TEST_STRING, config.getStoreConfiguration());
    
    config.setNumReplicas(5);
    Assert.assertEquals(5, config.getNumReplicas());
    config.setNumReplicas(-1);
    Assert.assertEquals(-1, config.getNumReplicas());
    
    config.setNumWorkerThreads(5);
    Assert.assertEquals(5,config.getNumWorkerThreads());
    config.setNumWorkerThreads(0);
    Assert.assertEquals(0, config.getNumWorkerThreads());
    
    config.setStoreType(TEST_STRING);
    Assert.assertEquals(TEST_STRING, config.getStoreType());
    
    config.setNumAttempts(5);
    Assert.assertEquals(5,config.getNumAttempts());
    
    config.setTimeoutMillis(3210l);
    Assert.assertEquals(3210l,config.getTimeoutMillis());
    
  }
}
