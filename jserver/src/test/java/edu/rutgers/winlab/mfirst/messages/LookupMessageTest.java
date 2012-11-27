/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;

/**
 * @author Robert Moore
 *
 */
public class LookupMessageTest {
  public GUID guid1;
  public GUID nullGuid;
  public NetworkAddress addr1;
  public NetworkAddress addr2;
  
  public static final byte[] GUID_BYTE1 = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4,
    0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x12,
    0x13 };
  
  public static final String STRING_TEST = "LKP #4294967295 (GUID(000102030405060708090a0b0c0d0e0f10111213))";
  public static final String STRING_NULL = "LKP #0 (null)";
  
  @Before
  public void prepTest() {
    this.guid1 = new GUID();
    this.guid1.setBinaryForm(GUID_BYTE1);

    this.nullGuid = new GUID();
    this.nullGuid.setBinaryForm(null);

    try {
      this.addr1 = IPv4UDPAddress.fromASCII("127.0.0.1:55");
      this.addr2 = IPv4UDPAddress.fromASCII("127.0.0.1:56");
    } catch (UnsupportedEncodingException e) {
      fail("No text encoding available?");
    }
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.messages.LookupMessage}.
   */
  @Test
  public void testFields() {
    LookupMessage msg = new LookupMessage();
    Assert.assertNull(msg.getGuid());
    Assert.assertNull(msg.getOriginAddress());
    Assert.assertEquals(0, msg.getRequestId());
    Assert.assertEquals(4, msg.getPayloadLength());
    Assert.assertEquals(16,msg.getMessageLength());
    Assert.assertEquals(0,msg.getVersion());
    
    msg.setGuid(guid1);
    
    msg.setOriginAddress(addr1);
    msg.setRequestId(0xFFFFFFFF);
    msg.setVersion((byte) 0x10);
    
    Assert.assertTrue(this.guid1.equals(msg.getGuid()));
    
    Assert.assertTrue(this.addr1.equals(msg.getOriginAddress()));
    Assert.assertEquals(0xFFFFFFFFl,msg.getRequestId());
    Assert.assertEquals((byte)0x10, msg.getVersion());
    Assert.assertEquals(24, msg.getPayloadLength());
    Assert.assertEquals(42,msg.getMessageLength());
    
    msg.setGuid(nullGuid);
    Assert.assertTrue(this.nullGuid.equals(msg.getGuid()));
    Assert.assertTrue(msg.getGuid().equals(this.nullGuid));
    Assert.assertEquals(4, msg.getPayloadLength());
    Assert.assertEquals(22,msg.getMessageLength());
    
    msg.setGuid(null);
    Assert.assertNull(msg.getGuid());
    Assert.assertEquals(4, msg.getPayloadLength());
    Assert.assertEquals(22,msg.getMessageLength());
    
  }

  @Test
  public void testToString(){
      LookupMessage msg = new LookupMessage();
      Assert.assertEquals(STRING_NULL,msg.toString());
      
      msg.setGuid(guid1);
      
      msg.setOriginAddress(addr1);
      msg.setRequestId(0xFFFFFFFF);
      msg.setVersion((byte) 0x10);
      
      Assert.assertEquals(STRING_TEST,msg.toString());
  }
  

}
