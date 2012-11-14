/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Robert Moore
 *
 */
public class MessageTypeTest {

  public static final String INSERT_PARSE = "I";
  public static final String INSERT_ACK_PARSE = "A";
  public static final String LOOKUP_PARSE = "Q";
  public static final String LOOKUP_RESP_PARSE = "R";
  public static final String UPDATE_PARSE = "U";
  public static final String UPDATE_ACK_PARSE = "V";
  
  @Test
  public void testToString() {
   Assert.assertEquals("INSERT",MessageType.INSERT.toString());
   Assert.assertEquals("INSERT_RESPONSE",MessageType.INSERT_RESPONSE.toString());
   Assert.assertEquals("LOOKUP",MessageType.LOOKUP.toString());
   Assert.assertEquals("LOOKUP_RESPONSE",MessageType.LOOKUP_RESPONSE.toString());
   Assert.assertEquals("UPDATE",MessageType.UPDATE.toString());
   Assert.assertEquals("UPDATE_RESPONSE",MessageType.UPDATE_RESPONSE.toString());
   Assert.assertEquals("UNKNOWN",MessageType.UNKNOWN.toString());
   
  }
  @Test
  public void testFromString(){
    Assert.assertEquals(MessageType.INSERT,MessageType.parseType(INSERT_PARSE));
    Assert.assertEquals(MessageType.INSERT_RESPONSE,MessageType.parseType(INSERT_ACK_PARSE));
    Assert.assertEquals(MessageType.LOOKUP,MessageType.parseType(LOOKUP_PARSE));
    Assert.assertEquals(MessageType.LOOKUP_RESPONSE,MessageType.parseType(LOOKUP_RESP_PARSE));
    Assert.assertEquals(MessageType.UPDATE,MessageType.parseType(UPDATE_PARSE));
    Assert.assertEquals(MessageType.UPDATE_RESPONSE,MessageType.parseType(UPDATE_ACK_PARSE));
    Assert.assertEquals(MessageType.UNKNOWN,MessageType.parseType("123"));
  }

  @Test
  public void testValues(){
    Assert.assertEquals(MessageType.INSERT.value(),(byte)0x00);
    Assert.assertEquals(MessageType.INSERT_RESPONSE.value(),(byte)0x80);
    Assert.assertEquals(MessageType.LOOKUP.value(),(byte)0x01);
    Assert.assertEquals(MessageType.LOOKUP_RESPONSE.value(),(byte)0x81);
    Assert.assertEquals(MessageType.UPDATE.value(),(byte)0x02);
    Assert.assertEquals(MessageType.UPDATE_RESPONSE.value(),(byte)0x82);
    Assert.assertEquals(MessageType.UNKNOWN.value(),(byte)0xFF);
  }
}
