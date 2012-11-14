/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.mapping.ipv4udp;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Robert Moore
 *
 */
public class ConfigurationTest {

  public static final String TEST_STRING1 = "test/string";
  public static final String TEST_STRING2 = "hash1";
  public static final String TEST_STRING3 = "test/map.file";
  
  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.mapping.ipv4udp.Configuration}.
   */
  @Test
  public void test() {
    
    Configuration config = new Configuration();
    config.setAsBindingFile(TEST_STRING1);
    Assert.assertEquals(TEST_STRING1,config.getAsBindingFile());
    
    config.setHashAlgorithm(TEST_STRING2);
    Assert.assertEquals(TEST_STRING2,config.getHashAlgorithm());
    
    config.setPrefixFile(TEST_STRING3);
    Assert.assertEquals(TEST_STRING3, config.getPrefixFile());
  }

}
