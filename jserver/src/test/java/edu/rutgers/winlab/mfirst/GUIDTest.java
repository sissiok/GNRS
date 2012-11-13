/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Robert Moore
 */
public class GUIDTest {

  private GUID guid;

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.GUID#hashCode()}.
   */
  @Test
  public void testHashCode() {
    GUID guid = new GUID();
    byte[] testBytes = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7,
        0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x12, 0x13 };
    guid.setBinaryForm(testBytes);

    Assert.assertTrue(Arrays.hashCode(testBytes) == guid.hashCode());
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.GUID#fromASCII(java.lang.String)}.
   */
  @Test
  public void testFromASCII() {
    String stringValue = "12345678901234567890";

    try {
      GUID guid = GUID.fromASCII(stringValue);
      byte[] asciiBytes = stringValue.getBytes("US-ASCII");
      GUID guid2 = new GUID();
      guid2.setBinaryForm(Arrays.copyOf(asciiBytes, GUID.SIZE_OF_GUID));
      Assert.assertTrue(Arrays.equals(guid.getBinaryForm(),
          guid2.getBinaryForm()));
      
      guid2 = GUID.fromASCII(null);
      Assert.assertNull(guid2.getBinaryForm());
      
      guid2 = GUID.fromASCII("");
      Assert.assertNull(guid2.getBinaryForm());
    } catch (UnsupportedEncodingException e) {
      fail("Unable to encode ASCII characters.");
    }

  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.GUID#fromInt(int)}.
   */
  @Test
  public void testFromInt() {
    int testInt = 0x1A2B3C4D;
    GUID guid = GUID.fromInt(testInt);
    GUID guid2 = new GUID();
    byte[] bytes = new byte[] { 0x1a, 0x2b, 0x3c, 0x4d, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0 };
    guid2.setBinaryForm(bytes);
    Assert
        .assertTrue(Arrays.equals(guid.getBinaryForm(), guid2.getBinaryForm()));
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.GUID#getBinaryForm()}.
   */
  @Test
  public void testGetBinaryForm() {
    byte[] bytes = new byte[] { 0x1a, 0x2b, 0x3c, 0x4d, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0 };
    byte[] bytes2 = Arrays.copyOf(bytes, bytes.length);
    GUID guid = new GUID();
    guid.setBinaryForm(bytes);
    Assert.assertTrue(Arrays.equals(bytes2, guid.getBinaryForm()));

    bytes = new byte[] { 0, 0, 0, 0x1a, 0x2b, 0x3c, 0x4d, 0, 0, 0, 0, 0, 0, 0,
        0 };
    bytes2 = Arrays.copyOf(bytes, bytes.length);
    guid.setBinaryForm(bytes);

    Assert.assertTrue(Arrays.equals(bytes2, guid.getBinaryForm()));
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.GUID#toString()}.
   */
  @Test
  public void testToString() {
    byte[] bytes = new byte[] { 0x1a, 0x2b, 0x3c, 0x4d, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0 };
    byte[] bytes2 = Arrays.copyOf(bytes, bytes.length);
    GUID guid = new GUID();
    guid.setBinaryForm(bytes);
    String asString = "GUID(1a2b3c4d00000000000000000000000000000000)";
    Assert.assertEquals(asString, guid.toString());
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.GUID#equals(java.lang.Object)} and
   * {@link edu.rutgers.winlab.mfirst.GUID#equalsGUID(edu.rutgers.winlab.mfirst.GUID)}
   * .
   */
  @Test
  public void testEquals() {
    try {
      String notEqual = "Hello, world!";
      GUID guid = GUID.fromASCII("Hello, world!");
      Assert.assertFalse(guid.equals(notEqual));
      Assert.assertFalse(guid.equals(Integer.valueOf(5)));
      
      GUID guid2 = GUID.fromASCII(notEqual);
      Assert.assertTrue(guid.equals(guid2));
      
      guid2 = GUID.fromInt(5);
      Assert.assertFalse(guid.equals(guid2));
      
    } catch (UnsupportedEncodingException uee) {
      fail("Unable to encode US-ASCII characters.");
    }
  }
}
