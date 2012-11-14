/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;

/**
 * @author Robert Moore
 */
public class GUIDBindingTest {

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.storage.GUIDBinding#hashCode()}.
   */
  @Test
  public void testHashCode() {
    try {
      GUIDBinding bind1 = new GUIDBinding();
      NetworkAddress addr1 = IPv4UDPAddress.fromASCII("1.2.3.4:4444");
      bind1.setAddress(addr1);
      bind1.setTtl(5);
      bind1.setWeight(2);

      GUIDBinding bind2 = new GUIDBinding();
      NetworkAddress addr2 = IPv4UDPAddress.fromASCII("5.4.3.2:9191");
      bind2.setAddress(addr2);
      bind2.setTtl(4);
      bind2.setWeight(8);

      Assert.assertTrue(addr1.equals(bind1.getAddress()));
      Assert.assertEquals(bind1.getTtl(), 5);
      Assert.assertEquals(bind1.getWeight(), 2);

      Assert.assertFalse(bind1.equals("1234"));
      Assert.assertFalse(bind1.equals(bind2));
      Assert.assertEquals(bind1.hashCode(), Arrays.hashCode(addr1.getValue()));

    } catch (UnsupportedEncodingException e) {
      Assert.fail("Unable to encode network addresses.");
    }

  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.storage.GUIDBinding#toString()}.
   */
  @Test
  public void testToString() {
    try {
      GUIDBinding bind1 = new GUIDBinding();
      NetworkAddress addr1 = IPv4UDPAddress.fromASCII("1.2.3.4:4444");
      bind1.setAddress(addr1);
      bind1.setTtl(5);
      bind1.setWeight(2);

      GUIDBinding bind2 = new GUIDBinding();
      NetworkAddress addr2 = IPv4UDPAddress.fromASCII("5.4.3.2:9191");
      bind2.setAddress(addr2);
      bind2.setTtl(4);
      bind2.setWeight(8);
      
      Assert.assertTrue("Bind (1.2.3.4.17.92:4444, 5, 2)".equals(bind1.toString()));
      Assert.assertTrue("Bind (5.4.3.2.35.231:9191, 4, 8)".equals(bind2.toString()));
      
    } catch (UnsupportedEncodingException e) {
      Assert.fail("Unable to encode network addresses.");
    }
  }

}
