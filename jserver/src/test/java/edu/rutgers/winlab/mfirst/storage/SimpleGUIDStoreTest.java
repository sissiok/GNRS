/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

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
public class SimpleGUIDStoreTest {

  public SimpleGUIDStore store;

  @Before
  public void prepStore() {
    this.store = new SimpleGUIDStore();
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.storage.SimpleGUIDStore#appendBindings(edu.rutgers.winlab.mfirst.GUID, edu.rutgers.winlab.mfirst.storage.GUIDBinding[])}
   * .
   */
  @Test
  public void testAppendBindings() {
    try {
      NetworkAddress addr1 = new NetworkAddress(AddressType.INET_4_UDP,
          new byte[] { 0, 0, 0, 0, 0, 0 });
      NetworkAddress addr2 = IPv4UDPAddress.fromInteger(7);

      GUID guid1 = GUID.fromASCII("1234");
      GUID guid2 = GUID.fromASCII("2345");

      this.store.appendBindings(guid1, (GUIDBinding[])null);

      GUIDBinding bind1 = new GUIDBinding();
      bind1.setAddress(addr1);
      bind1.setTtl(0l);
      bind1.setWeight(0);

      GUIDBinding bind2 = new GUIDBinding();
      bind2.setAddress(addr2);
      bind2.setWeight(1);
      bind2.setTtl(System.currentTimeMillis());

      Assert.assertTrue(this.store.appendBindings(guid1, bind1));

      GNRSRecord record = this.store.getBindings(guid1);
      Assert.assertTrue(guid1.equals(record.getGuid()));
      NetworkAddress[] bound1 = record.getBindings();
      Assert.assertTrue(bound1.length == 1);
      Assert.assertTrue(addr1.equals(bound1[0]));

      Assert.assertTrue(this.store.appendBindings(guid1, bind1, bind2));
      Assert.assertTrue(this.store.appendBindings(guid2, bind2, bind1));

      record = this.store.getBindings(guid1);
      Assert.assertTrue(guid1.equals(record.getGuid()));
      bound1 = record.getBindings();
      Assert.assertTrue(bound1.length == 2);
      Assert.assertTrue( (addr1.equals(bound1[0]) && addr2.equals(bound1[1] ))
          || (addr2.equals(bound1[0]) && addr1.equals(bound1[1])));
      Assert.assertTrue(addr2.equals(bound1[1]));

      record = this.store.getBindings(guid2);
      Assert.assertTrue(guid2.equals(record.getGuid()));
      NetworkAddress[] bound2 = record.getBindings();
      Assert.assertTrue(bound2.length == 2);
      Assert.assertTrue((addr1.equals(bound1[0]) && addr2.equals(bound1[1]))
          || (addr2.equals(bound1[0]) && addr1.equals(bound1[1])));

    } catch (UnsupportedEncodingException e) {
      Assert.fail();
    }
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.storage.SimpleGUIDStore#replaceBindings(edu.rutgers.winlab.mfirst.GUID, edu.rutgers.winlab.mfirst.storage.GUIDBinding[])}
   * .
   */
  @Test
  public void testReplaceBindings() {
    try {
      NetworkAddress addr1 = new NetworkAddress(AddressType.INET_4_UDP,
          new byte[] { 0, 0, 0, 0, 0, 0 });
      NetworkAddress addr2 = IPv4UDPAddress.fromInteger(7);

      GUID guid1 = GUID.fromASCII("1234");
      GUID guid2 = GUID.fromASCII("2345");

      GUIDBinding bind1 = new GUIDBinding();
      bind1.setAddress(addr1);
      bind1.setTtl(0l);
      bind1.setWeight(0);

      GUIDBinding bind2 = new GUIDBinding();
      bind2.setAddress(addr2);
      bind2.setWeight(1);
      bind2.setTtl(System.currentTimeMillis());
      
      GNRSRecord record1 = this.store.getBindings(guid1);
      Assert.assertNull(record1);
      
      Assert.assertTrue(this.store.replaceBindings(guid1));
      Assert.assertTrue(this.store.replaceBindings(guid1,(GUIDBinding[])null));
      
      Assert.assertTrue(this.store.replaceBindings(guid1, bind1));
      GNRSRecord record = this.store.getBindings(guid1);
      Assert.assertTrue(guid1.equals(record.getGuid()));
      NetworkAddress[] bound1 = record.getBindings();
      Assert.assertTrue(bound1.length == 1);
      Assert.assertTrue(addr1.equals(bound1[0]));
      
      Assert.assertTrue(this.store.replaceBindings(guid1,bind2, bind1));
      record = this.store.getBindings(guid1);
      Assert.assertTrue(guid1.equals(record.getGuid()));
      bound1 = record.getBindings();
      Assert.assertTrue(bound1.length == 2);
      Assert.assertTrue((addr1.equals(bound1[0]) && addr2.equals(bound1[1] ))
          || (addr2.equals(bound1[0]) && addr1.equals(bound1[1])));
      
    } catch (UnsupportedEncodingException e) {
      Assert.fail();
    }
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.storage.SimpleGUIDStore#doInit()}.
   */
  @Test
  public void testDoInit() {
    this.store.doInit();
    Assert.assertTrue(true);
  }

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.storage.SimpleGUIDStore#doShutdown()}.
   */
  @Test
  public void testDoShutdown() {
    this.store.doShutdown();
    Assert.assertTrue(true);
  }

}
