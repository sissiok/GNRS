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
public class InsertResponseMessageTest {

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.messages.InsertResponseMessage}.
   */
  @Test
  public void test() {
    InsertResponseMessage msg = new InsertResponseMessage();
    Assert.assertEquals(4,msg.getPayloadLength());
    Assert.assertEquals(0,msg.getResponsePayloadLength());
    Assert.assertEquals(16,msg.getMessageLength());
    Assert.assertEquals("INR #0/null",msg.toString());
    
    
    msg.setResponseCode(ResponseCode.FAILED);
    Assert.assertTrue(ResponseCode.FAILED.equals(msg.getResponseCode()));
    msg.setResponseCode(ResponseCode.SUCCESS);
    Assert.assertTrue(ResponseCode.SUCCESS.equals(msg.getResponseCode()));
  }

 
}
