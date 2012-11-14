/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
public class LookupResponseMessageTest {

  public GUID guid1;
  public GUID nullGuid;
  public NetworkAddress addr1;
  public NetworkAddress addr2;
  public NetworkAddress addr3;
  public NetworkAddress[] bind1;

  public static final byte[] GUID_BYTE1 = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4,
      0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x12,
      0x13 };

  public static final String STRING_INIT = "LKR #0/null {}";
  public static final String STRING_TEST = "LKR #4294967295/SUCCESS {127.0.0.1:55, 127.0.0.1:56, 127.0.0.1:57}";

  @Before
  public void prepTest() {
    this.guid1 = new GUID();
    this.guid1.setBinaryForm(GUID_BYTE1);

    this.nullGuid = new GUID();
    this.nullGuid.setBinaryForm(null);

    try {
      this.addr1 = IPv4UDPAddress.fromASCII("127.0.0.1:55");
      this.addr2 = IPv4UDPAddress.fromASCII("127.0.0.1:56");
      this.addr3 = IPv4UDPAddress.fromASCII("127.0.0.1:57");
    } catch (UnsupportedEncodingException e) {
      fail("No text encoding available?");
    }

    this.bind1 = new NetworkAddress[] { this.addr1, this.addr2, this.addr3 };
  }
  
  @Test
  public void test() {
    LookupResponseMessage msg = new LookupResponseMessage();
    
    Assert.assertNull(msg.getBindings());
    Assert.assertNull(msg.getOriginAddress());    
    Assert.assertEquals(0,msg.getRequestId());
    Assert.assertEquals(MessageType.LOOKUP_RESPONSE,msg.getType());
    Assert.assertEquals(20,msg.getMessageLength());
    Assert.assertEquals(8,msg.getPayloadLength());
    Assert.assertEquals(4,msg.getResponsePayloadLength());
    Assert.assertEquals(0,msg.getBindingsLength());
    Assert.assertEquals(0, msg.getNumBindings());
    
    msg.setBindings(bind1);
    msg.setOriginAddress(addr1);
    msg.setRequestId(0xFFFFFFFF);
    msg.setResponseCode(ResponseCode.SUCCESS);
    msg.setVersion((byte)0x10);
    msg.setType(MessageType.UNKNOWN);
    
    Assert.assertTrue(Arrays.equals(bind1,msg.getBindings()));
    Assert.assertTrue(addr1.equals(msg.getOriginAddress()));
    Assert.assertEquals(0xFFFFFFFFl, msg.getRequestId());
    Assert.assertEquals(MessageType.UNKNOWN,msg.getType());
    Assert.assertEquals(ResponseCode.SUCCESS,msg.getResponseCode());
    Assert.assertEquals(56,msg.getMessageLength());
    Assert.assertEquals(38,msg.getPayloadLength());
    Assert.assertEquals(34,msg.getResponsePayloadLength());
    Assert.assertEquals(30,msg.getBindingsLength());
    Assert.assertEquals(3, msg.getNumBindings());
    
    msg.setBindings(new NetworkAddress[]{});
    Assert.assertEquals(0,msg.getBindingsLength());
    Assert.assertEquals(0, msg.getNumBindings());
    
    msg.setResponseCode(ResponseCode.FAILED);
    Assert.assertEquals(ResponseCode.FAILED, msg.getResponseCode());
    }
  
  @Test
  public void testToString(){
    LookupResponseMessage msg = new LookupResponseMessage();
    
    Assert.assertEquals(STRING_INIT,msg.toString());
    
    msg.setBindings(bind1);
    msg.setOriginAddress(addr1);
    msg.setRequestId(0xFFFFFFFF);
    msg.setResponseCode(ResponseCode.SUCCESS);
    msg.setVersion((byte)0x10);
    msg.setType(MessageType.UNKNOWN);
    
    Assert.assertEquals(STRING_TEST,msg.toString());
  }

}
