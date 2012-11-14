/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;
import edu.rutgers.winlab.mfirst.storage.GUIDBinding;

/**
 * @author Robert Moore
 *
 */
public class BDBGUIDBindingTest {

  @Test
  public void test(){
    try {
      NetworkAddress netAddr = IPv4UDPAddress.fromASCII("127.0.0.1:22");
      GUIDBinding realBind = new GUIDBinding();
      realBind.setAddress(netAddr);
      realBind.setTtl(5);
      realBind.setWeight(3);
      BDBGUIDBinding binding = new BDBGUIDBinding();
      Assert.assertNull(binding.address);
      Assert.assertEquals(0,binding.weight);
      Assert.assertEquals(0,binding.ttl);
      binding = BDBGUIDBinding.fromGUIDBinding(realBind);
      
      GUIDBinding converted = binding.toGUIDBinding();
      Assert.assertTrue(realBind.equals(converted));
      
      realBind = new GUIDBinding();
      binding = BDBGUIDBinding.fromGUIDBinding(realBind);
      converted = binding.toGUIDBinding();
      Assert.assertTrue(realBind.equals(converted));
      
      realBind = new GUIDBinding();
      realBind.setAddress(new NetworkAddress(null,null));
      binding = BDBGUIDBinding.fromGUIDBinding(realBind);
      converted = binding.toGUIDBinding();
      Assert.assertTrue(realBind.equals(converted));
      
    } catch (UnsupportedEncodingException e) {
      fail("Unable to decode ASCII.");
    }
    
    
    
  }
}
