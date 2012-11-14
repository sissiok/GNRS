/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.mapping.ipv4udp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;

/**
 * @author Robert Moore
 */
public class IPv4UDPGUIDMapperTest {
  public GUID guid;
  public static final byte[] GUID_BYTE = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4,
      0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x12,
      0x13 };

  public NetworkAddress ipv4Addr;
  public NetworkAddress guidAddr;

  @Before
  public void setup() {
    this.guid = new GUID();
    this.guid.setBinaryForm(GUID_BYTE);

    try {
      this.ipv4Addr = IPv4UDPAddress.fromASCII("127.0.0.1:5001");
    } catch (UnsupportedEncodingException e) {
      fail("Unable to decode from ASCII.");
    }
    this.guidAddr = new NetworkAddress(AddressType.GUID, new byte[] {});

  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.mapping.ipv4udp.IPv4UDPGUIDMapper#IPv4UDPGUIDMapper(java.lang.String)}
   * .
   */
  @Test
  public void testBadHashingAlgorithm() {
    try {
      IPv4UDPGUIDMapper mapper = new IPv4UDPGUIDMapper(
          "src/test/resources/map-bad-ipv4.xml");
      Collection<NetworkAddress> nullList = mapper.getMapping(guid, 1,
          AddressType.INET_4_UDP);
      Assert.assertNull(nullList);
      // Assert.assertTrue(nullList.isEmpty());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.mapping.ipv4udp.IPv4UDPGUIDMapper#getMapping(edu.rutgers.winlab.mfirst.GUID, int, edu.rutgers.winlab.mfirst.net.AddressType[])}
   * .
   */
  @Test
  public void testGetMapping() {
    try {
      IPv4UDPGUIDMapper mapper = new IPv4UDPGUIDMapper(
          "src/test/resources/map-ipv4.xml");
      Collection<NetworkAddress> mapped = mapper.getMapping(guid, 1,
          AddressType.INET_4_UDP);
      Assert.assertNotNull(mapped);
      Iterator<NetworkAddress> iter = mapped.iterator();
      Assert.assertEquals(1, mapped.size());
      Assert.assertTrue(iter.hasNext());
      NetworkAddress netAddr = iter.next();
      Assert.assertFalse(iter.hasNext());
      Assert.assertTrue(this.ipv4Addr.equals(netAddr));
      
      mapped = mapper.getMapping(guid, 1);
      Assert.assertNotNull(mapped);
      iter = mapped.iterator();
      Assert.assertEquals(1, mapped.size());
      Assert.assertTrue(iter.hasNext());
      netAddr = iter.next();
      Assert.assertFalse(iter.hasNext());
      Assert.assertTrue(this.ipv4Addr.equals(netAddr));
      
      mapped = mapper.getMapping(guid, 1, null);
      Assert.assertNotNull(mapped);
      iter = mapped.iterator();
      Assert.assertEquals(1, mapped.size());
      Assert.assertTrue(iter.hasNext());
      netAddr = iter.next();
      Assert.assertFalse(iter.hasNext());
      Assert.assertTrue(this.ipv4Addr.equals(netAddr));
      
      mapped = mapper.getMapping(guid, 1, AddressType.GUID);
      Assert.assertNull(mapped);
      

      // Assert.assertTrue(nullList.isEmpty());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.mapping.ipv4udp.IPv4UDPGUIDMapper#getTypes()}
   * .
   */
  @Test
  public void testGetTypes() {
    try {
      IPv4UDPGUIDMapper mapper = new IPv4UDPGUIDMapper(
          "src/test/resources/map-ipv4.xml");
      EnumSet<AddressType> types = mapper.getTypes();
      Assert.assertEquals(1, types.size());
      Iterator<AddressType> iter = types.iterator();
      Assert.assertTrue(iter.hasNext());
      AddressType type = iter.next();
      Assert.assertFalse(iter.hasNext());
      Assert.assertEquals(AddressType.INET_4_UDP, type);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.mapping.ipv4udp.IPv4UDPGUIDMapper#getDefaultAddressType()}
   * .
   */
  @Test
  public void testGetDefaultAddressType() {
    try {
      IPv4UDPGUIDMapper mapper = new IPv4UDPGUIDMapper(
          "src/test/resources/map-ipv4.xml");
      AddressType type = mapper.getDefaultAddressType();
      Assert.assertEquals(AddressType.INET_4_UDP,type);
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
