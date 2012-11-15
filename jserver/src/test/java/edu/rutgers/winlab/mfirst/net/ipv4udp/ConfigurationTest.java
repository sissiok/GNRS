/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Robert Moore
 *
 */
public class ConfigurationTest {

  public Configuration config;
  
  @Before
  public void createConfig(){
    this.config = new Configuration();
  }
  
  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration}.
   */
  @Test
  public void testBindAddress() {
    this.config.setBindAddress("127.0.0.1");
    Assert.assertEquals("127.0.0.1",this.config.getBindAddress());
    
    this.config.setBindAddress("1.2.3.4");
    Assert.assertEquals("1.2.3.4",this.config.getBindAddress());
    
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration}.
   */
  @Test
  public void testBindPort() {
    this.config.setBindPort(1234);
    Assert.assertEquals(1234,this.config.getBindPort());
    this.config.setBindPort(9999);
    Assert.assertEquals(9999,this.config.getBindPort());
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration}.
   */
  @Test
  public void testIsAscynchronousWrite() {
    this.config.setAsynchronousWrite(true);
    Assert.assertTrue(this.config.isAsynchronousWrite());
    this.config.setAsynchronousWrite(false);
  }

}
