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
public class ResponseCodeTest {

  @Test
  public void testValue() {
    Assert.assertEquals(0,ResponseCode.SUCCESS.value());
    Assert.assertEquals(1,ResponseCode.FAILED.value());
  }
  @Test
  public void testValueOf(){
    ResponseCode code = ResponseCode.valueOf(0);
    Assert.assertEquals(ResponseCode.SUCCESS, code);
    code = ResponseCode.valueOf(1);
    Assert.assertEquals(ResponseCode.FAILED, code);
    code = ResponseCode.valueOf(0xFF);
    Assert.assertEquals(ResponseCode.FAILED, code);
  }
  
  @Test
  public void testToString(){
    Assert.assertEquals("SUCCESS", ResponseCode.SUCCESS.toString());
    Assert.assertEquals("FAILED", ResponseCode.FAILED.toString());
  }

}
