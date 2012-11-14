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
    
    AddressType nullType = AddressType.valueOf(-1);
    Assert.assertNull(nullType);
  }

 

}
