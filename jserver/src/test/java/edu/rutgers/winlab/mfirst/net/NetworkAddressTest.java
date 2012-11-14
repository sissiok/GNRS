/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Robert Moore
 */
public class NetworkAddressTest {

  public static final byte[] ADDRESS_BYTES_1 = new byte[] { 0x0, 0x1, 0x2, 0x3,
      0x4, 0x1 };
  public static final byte[] ADDRESS_BYTES_2 = new byte[] { 0x0, 0x1, 0x2, 0x3,
      0x4, 0x2 };
  public static final byte[] ADDRESS_BYTES_3 = new byte[] { 0x0, 0x1, 0x2, 0x3 };

  public static final byte[] ADDRESS_BYTES_4 = new byte[] { 0x0, 0x1, 0x2, 0x3,
      0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11,
      0x12, 0x13 };

  public static final String NA1_STRING = "NA(000102030401)";
  public static final String NA2_STRING = "NA(000102030402)";

  public NetworkAddress netAddr1;
  public NetworkAddress netAddr1Copy;
  public NetworkAddress netAddr2;
  public NetworkAddress netAddr3;
  public NetworkAddress netAddr4;

  @Before
  public void setupAddresses() {
    netAddr1 = new NetworkAddress(AddressType.INET_4_UDP, ADDRESS_BYTES_1);
    netAddr1Copy = new NetworkAddress(AddressType.INET_4_UDP, ADDRESS_BYTES_1);
    netAddr2 = new NetworkAddress(AddressType.INET_4_UDP, ADDRESS_BYTES_2);
    netAddr3 = new NetworkAddress(AddressType.INET_4_UDP, ADDRESS_BYTES_3);
    netAddr4 = new NetworkAddress(AddressType.GUID, ADDRESS_BYTES_4);
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.NetworkAddress}.
   */
  @Test
  public void testHashCode() {

    Assert.assertEquals(Arrays.hashCode(ADDRESS_BYTES_1), netAddr1.hashCode());
    Assert.assertEquals(netAddr1.hashCode(), netAddr1Copy.hashCode());
    Assert.assertEquals(Arrays.hashCode(ADDRESS_BYTES_2), netAddr2.hashCode());
    Assert.assertFalse(netAddr1.hashCode() == netAddr2.hashCode());

  }

  @Test
  public void testEquality() {
    Assert.assertTrue(netAddr1.equals(netAddr1Copy));
    Assert.assertTrue(netAddr1Copy.equals(netAddr1));

    Assert.assertFalse(netAddr1.equals(netAddr2));
    Assert.assertFalse(netAddr2.equals(netAddr1));
    Assert.assertFalse(netAddr1.equals(ADDRESS_BYTES_1));
    
    Assert.assertFalse(netAddr1.equals(netAddr4));
  }

  @Test
  public void testLength() {
    Assert.assertEquals(netAddr1.getLength(), ADDRESS_BYTES_1.length);
    Assert.assertEquals(netAddr3.getLength(), ADDRESS_BYTES_3.length);
  }

  @Test
  public void testValue() {

    NetworkAddress nullValue = new NetworkAddress(AddressType.INET_4_UDP, null);
    Assert.assertNull(nullValue.getValue());
    Assert.assertEquals(0, nullValue.getLength());
    nullValue.setValue(null);
    Assert.assertNull(nullValue.getValue());
    Assert.assertEquals(0, nullValue.getLength());

    Assert.assertTrue(AddressType.INET_4_UDP.equals(netAddr1.getType()));

    Assert.assertTrue(Arrays.equals(netAddr1.getValue(),
        netAddr1Copy.getValue()));

    Assert.assertFalse(Arrays.equals(netAddr1.getValue(), netAddr2.getValue()));

    Assert.assertTrue(Arrays.equals(ADDRESS_BYTES_3, netAddr3.getValue()));
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.net.NetworkAddress}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalConstrLength() {
    NetworkAddress netAddr = new NetworkAddress(AddressType.INET_4_UDP,
        new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 });
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.net.NetworkAddress#setValue()}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalSetValueLength() {
    NetworkAddress netAddr = new NetworkAddress(AddressType.INET_4_UDP,
        ADDRESS_BYTES_1);
    netAddr.setValue(new byte[65536]);
  }

  @Test
  public void testToString() {
    Assert.assertEquals(NA1_STRING, this.netAddr1.toString());
    Assert.assertEquals(NA2_STRING, this.netAddr2.toString());
    Assert.assertFalse(NA2_STRING.equals(this.netAddr1.toString()));
  }

}
