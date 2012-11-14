/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;

/**
 * @author Robert Moore
 */
public class BDBNetworkAddressTest {

  @Test
  public void test() {
    try {
      
      
    BDBNetworkAddress addr = new BDBNetworkAddress();
    Assert.assertEquals(-1,addr.getType());
    Assert.assertNull(addr.getValue());
    byte[] bytes = new byte[] {0x0, 0x1, 0x2};
    addr.setValue(bytes);
    
    
    Assert.assertTrue(Arrays.equals(bytes,addr.getValue()));
    
    addr.setType(0);
    Assert.assertEquals(0,addr.getType());
    
    
    addr = BDBNetworkAddress.fromNetworkAddress(null);
    Assert.assertNull(addr);
    
    NetworkAddress netAddr = IPv4UDPAddress.fromASCII("127.0.0.1:92");
    addr = BDBNetworkAddress.fromNetworkAddress(netAddr);
    NetworkAddress converted = addr.toNetworkAddress();
    Assert.assertTrue(netAddr.equals(converted));
    Assert.assertTrue(converted.equals(netAddr));
    }catch(UnsupportedEncodingException uee){
      fail("Unable to decode ASCII.");
    }
  }
}
