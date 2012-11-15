/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * @author Robert Moore
 *
 */
public class IPv4UDPAddressTest {

  public static final String VALUE_STRING = "1.2.3.4:16";
  public static final int VALUE_INT = 0x01020304;
  public static final String INT_AS_STRING = "1.2.3.4:0";
  public static final String BYTES_AS_STRING = "1.2.3.4:16";
  public static final byte[] VALUE_BYTES = new byte[] {0x1, 0x2, 0x3, 0x4, 0x0, 0x10};
  
  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress#toString()}.
   */
  @Test
  public void testToString() {
    try {
    IPv4UDPAddress addr = IPv4UDPAddress.fromASCII(VALUE_STRING);
    Assert.assertEquals(VALUE_STRING,addr.toString());
    addr = IPv4UDPAddress.fromInteger(VALUE_INT);
    Assert.assertEquals(INT_AS_STRING, addr.toString());
    }catch(UnsupportedEncodingException uee){
      fail("Unable to decode from ASCII.");
    }
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress#IPv4UDPAddress(byte[])}.
   */
  @Test
  public void testIPv4UDPAddress() {
   
    try {
      IPv4UDPAddress addr = new IPv4UDPAddress(VALUE_BYTES);
      Assert.assertEquals(BYTES_AS_STRING,addr.toString());
      IPv4UDPAddress addr2 = IPv4UDPAddress.fromASCII(VALUE_STRING);
      
      Assert.assertTrue(addr.equals(addr2));
      Assert.assertTrue(addr2.equals(addr));
    } catch (UnsupportedEncodingException e) {
     fail("Unable to decode ASCII");
    }
    
  }

  @Test
  public void testFromASCII(){
    try {
    IPv4UDPAddress addr1 = IPv4UDPAddress.fromASCII(null);
    Assert.assertNull(addr1);
    addr1 = IPv4UDPAddress.fromASCII("");
    Assert.assertNull(addr1);
    addr1 = IPv4UDPAddress.fromASCII("1.2.3.4");
    Assert.assertEquals("1.2.3.4:5001",addr1.toString());
    }catch(UnsupportedEncodingException uee){
      fail("Unable to decode ASCII"); 
    }
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress#fromInteger(int)}.
   */
  @Test
  public void testFromInteger() {
    IPv4UDPAddress addr1 = IPv4UDPAddress.fromInteger(VALUE_INT);
    Assert.assertEquals(INT_AS_STRING,addr1.toString());
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress#toSocketAddr(edu.rutgers.winlab.mfirst.net.NetworkAddress)}.
   */
  @Test
  public void testToSocketAddr() {
   try {
     IPv4UDPAddress addr1 = IPv4UDPAddress.fromASCII(VALUE_STRING);
     InetSocketAddress addx = IPv4UDPAddress.toSocketAddr(addr1);
     IPv4UDPAddress addr2 = IPv4UDPAddress.fromInetSocketAddress(addx);
     Assert.assertTrue(addr1.equals(addr2));
     Assert.assertTrue(addr2.equals(addr1));
     
     addx = IPv4UDPAddress.toSocketAddr(null);
     Assert.assertNull(addx);
     
     NetworkAddress netAddr = new NetworkAddress(null,null);
     addx = IPv4UDPAddress.toSocketAddr(netAddr);
     Assert.assertNull(addx);
   }catch(UnsupportedEncodingException uee){
     fail("Cannot decode ASCII");
   }
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress#fromInetSocketAddress(java.net.InetSocketAddress)}.
   */
  @Test
  public void testFromInetSocketAddress() {
    try {
    InetSocketAddress sock = new InetSocketAddress("1.2.3.4", 16);
    IPv4UDPAddress addr1 = IPv4UDPAddress.fromInetSocketAddress(sock);
    IPv4UDPAddress addr2 = IPv4UDPAddress.fromASCII(VALUE_STRING);
    Assert.assertTrue(addr1.equals(addr2));
    Assert.assertTrue(addr2.equals(addr1));
    }catch(UnsupportedEncodingException uee){
      fail("Unable to decode ASCII");
    }
  }

}
