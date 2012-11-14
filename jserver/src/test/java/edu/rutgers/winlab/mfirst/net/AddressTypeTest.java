/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Robert Moore
 *
 */
public class AddressTypeTest {

  /**
   * String for IPv4/UDP network address type.
   */
  public static final String IPV4_UDP_STRING = "IPv4+UDP";
  /**
   * String for GUID type.
   */
  public static final String GUID_STRING = "GUID";
  
  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.AddressType}.
   */
  @Test
  public void test() {
    AddressType t1 = AddressType.INET_4_UDP;
    Assert.assertEquals(t1.value(), 0);
    Assert.assertEquals(t1.getMaxLength(),6);
    Assert.assertEquals(IPV4_UDP_STRING,t1.toString());
    
    
    AddressType t2 = AddressType.valueOf(0);
    Assert.assertEquals(t2.value(), 0);
    Assert.assertEquals(t2.getMaxLength(),6);
    Assert.assertEquals(IPV4_UDP_STRING,t2.toString());
    
    AddressType t3 = AddressType.GUID;
    Assert.assertEquals(t3.value(), 1);
    Assert.assertEquals(t3.getMaxLength(),20);
    Assert.assertEquals(GUID_STRING,t3.toString());
    
    AddressType t4 = AddressType.valueOf(1);
    Assert.assertEquals(t4.value(), 1);
    Assert.assertEquals(t4.getMaxLength(),20);
    Assert.assertEquals(GUID_STRING,t4.toString());
    
    Assert.assertTrue(t1.equals(t2));
    Assert.assertTrue(t2.equals(t1));
    Assert.assertTrue(t3.equals(t4));
    Assert.assertTrue(t4.equals(t3));
    Assert.assertFalse(t1.equals(t4));
    Assert.assertFalse(t3.equals(t2));
    
    
    AddressType nullType = AddressType.valueOf(-1);
    Assert.assertNull(nullType);
  }

 

}
