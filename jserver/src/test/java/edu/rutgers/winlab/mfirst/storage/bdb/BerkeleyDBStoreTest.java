/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sun.util.LocaleServiceProviderPool.LocalizedObjectGetter;

import edu.rutgers.winlab.mfirst.Configuration;
import edu.rutgers.winlab.mfirst.GNRSServer;
import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;
import edu.rutgers.winlab.mfirst.storage.GNRSRecord;
import edu.rutgers.winlab.mfirst.storage.GUIDBinding;

/**
 * @author Robert Moore
 */
public class BerkeleyDBStoreTest {

  public GUID guid1;

  public GUIDBinding bind1;
  public GUIDBinding bind2;
  public GUIDBinding bind3;

  public NetworkAddress addr1;
  public NetworkAddress addr2;
  public NetworkAddress addr3;

  public static final byte[] GUID1_BYTE = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4,
      0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x12,
      0x13 };

  public BerkeleyDBStore store;

  @AfterClass
  public static void cleanDBDirs() {
    try {
      delete(new File("target/test-classes/bdb"));
    } catch (IOException e) {
      System.out.println("Unable to remove DB directory.");
    }
  }

  private static void delete(final File f) throws IOException {

    if (f.isDirectory()) {
      for (File c : f.listFiles())
        delete(c);
    }
    if (!f.delete())
      throw new FileNotFoundException("Failed to delete file: " + f);

  }

  @Before
  public void createDB() {
    try {

      this.guid1 = new GUID();
      this.guid1.setBinaryForm(GUID1_BYTE);

      this.addr1 = IPv4UDPAddress.fromASCII("127.0.0.1:6463");
      this.addr2 = IPv4UDPAddress.fromASCII("128.1.1.34:112");
      this.addr3 = new NetworkAddress(AddressType.GUID, GUID1_BYTE);

      this.bind1 = new GUIDBinding();
      this.bind1.setAddress(this.addr1);
      this.bind1.setTtl(5);
      this.bind1.setWeight(3);

      this.bind2 = new GUIDBinding();
      this.bind2.setAddress(this.addr2);
      this.bind2.setTtl(15);
      this.bind2.setWeight(500);

      this.bind3 = new GUIDBinding();
      this.bind3.setAddress(this.addr3);
      this.bind3.setTtl(0);
      this.bind3.setWeight(1);

      this.store = new BerkeleyDBStore("src/test/resources/berkeleydb.xml",
          null);
      this.store.doInit();

    } catch (UnsupportedEncodingException uee) {
      fail("Unable to decode ASCII.");
    }
  }

  @After
  public void clearStore() {
    if (this.store != null) {
      this.store.enableClear();
      try {
        this.store.clearStore();
        this.store.doShutdown();
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  @Test
  public void testEmptyStore() {
    Assert.assertNotNull(this.store);

    GNRSRecord record = this.store.getBindings(this.guid1);
    Assert.assertNotNull(record);
    Assert.assertTrue(this.guid1.equals(record.getGuid()));
    NetworkAddress[] addresses = record.getBindings();
    Assert.assertNotNull(addresses);
    Assert.assertEquals(0, addresses.length);

  }

  @Test
  public void testSingleRetrieval() {

    this.store.appendBindings(this.guid1, this.bind1);

    GNRSRecord record = this.store.getBindings(this.guid1);
    Assert.assertNotNull(record);
    Assert.assertTrue(this.guid1.equals(record.getGuid()));
    NetworkAddress[] addresses = record.getBindings();
    Assert.assertNotNull(addresses);
    Assert.assertEquals(1, addresses.length);
    Assert.assertTrue(this.addr1.equals(addresses[0]));

  }

  @Test
  public void testMultiRetrieval() {

    this.store.appendBindings(this.guid1, this.bind1);
    this.store.appendBindings(this.guid1, this.bind2, this.bind3);

    GNRSRecord record = this.store.getBindings(this.guid1);
    Assert.assertNotNull(record);
    Assert.assertTrue(this.guid1.equals(record.getGuid()));
    NetworkAddress[] addresses = record.getBindings();
    Assert.assertNotNull(addresses);
    Assert.assertEquals(3, addresses.length);
    LinkedList<NetworkAddress> addrList = new LinkedList<NetworkAddress>();
    for (NetworkAddress addx : addresses) {
      addrList.add(addx);
    }
    Assert.assertTrue(addrList.contains(this.addr1));
    Assert.assertTrue(addrList.contains(this.addr2));
    Assert.assertTrue(addrList.contains(this.addr3));

  }

  @Test
  public void testReplace() {

    this.store.appendBindings(this.guid1, this.bind1);
    this.store.replaceBindings(this.guid1, this.bind2, this.bind3);
    GNRSRecord record = this.store.getBindings(this.guid1);
    Assert.assertNotNull(record);
    Assert.assertTrue(this.guid1.equals(record.getGuid()));
    NetworkAddress[] addresses = record.getBindings();
    Assert.assertNotNull(addresses);
    Assert.assertEquals(2, addresses.length);
    LinkedList<NetworkAddress> addrList = new LinkedList<NetworkAddress>();
    for (NetworkAddress addx : addresses) {
      addrList.add(addx);
    }

    Assert.assertTrue(addrList.contains(this.addr2));
    Assert.assertTrue(addrList.contains(this.addr3));

  }

  @Test
  public void testDelete() {

    this.store.appendBindings(this.guid1, this.bind1);
    this.store.appendBindings(this.guid1, this.bind2, this.bind3);

    this.store.replaceBindings(this.guid1);

    GNRSRecord record = this.store.getBindings(this.guid1);
    Assert.assertNotNull(record);
    Assert.assertTrue(this.guid1.equals(record.getGuid()));
    NetworkAddress[] addresses = record.getBindings();
    Assert.assertNotNull(addresses);
    Assert.assertEquals(0, addresses.length);

    this.store.appendBindings(this.guid1, this.bind1);
    this.store.appendBindings(this.guid1, this.bind2, this.bind3);

    this.store.replaceBindings(this.guid1, null);
    record = this.store.getBindings(this.guid1);
    Assert.assertNotNull(record);
    Assert.assertTrue(this.guid1.equals(record.getGuid()));
    addresses = record.getBindings();
    Assert.assertNotNull(addresses);
    Assert.assertEquals(0, addresses.length);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadEnvironment() {
    // This needs to fail, shouldn't be able to write to /var/proc
    this.store = new BerkeleyDBStore("src/test/resources/berkeleydb-bad.xml",
        null);

  }

  @Test(expected=IllegalArgumentException.class)
  public void testDBLocationExists() {
    // Delete the DB directory
    this.clearStore();
    cleanDBDirs();

    // Create a file of the same name
    File inTheWay = new File("target/test-classes/bdb");
    try {
      inTheWay.createNewFile();
      PrintWriter writer = new PrintWriter(new FileWriter(inTheWay));
      writer
          .println("This file is used for GNRS unit testing and may be safely deleted.");
      writer.flush();
      writer.close();
      
      this.store = new BerkeleyDBStore("src/test/resources/berkeleydb.xml",
          null);

    } catch (IOException e) {
      fail("Unable to create temporary file.");
    } finally {
      try {
        delete(inTheWay);
      } catch (IOException ioe) {
        fail("Unable to remove temporary file.");
      }
      System.out.println(3);
    }

  }

  @Test
  public void testStatTimer() {
    try {
      edu.rutgers.winlab.mfirst.Configuration srvConfig = new Configuration();
      srvConfig.setCollectStatistics(true);
      srvConfig.setMappingConfiguration("src/test/resources/map-ipv4.xml");
      srvConfig.setNetworkConfiguration("src/test/resources/net-ipv4.xml");
      srvConfig.setNetworkType("ipv4udp");
      srvConfig.setNumReplicas(1);
      srvConfig.setNumWorkerThreads(1);
      srvConfig.setStoreType("simple");

      GNRSServer server = new GNRSServer(srvConfig);
      server.startup();

      this.clearStore();
      this.store = new BerkeleyDBStore("src/test/resources/berkeleydb.xml",
          server);
      this.store.doInit();
      try {
        Thread.sleep(12000);
      } catch (InterruptedException ie) {
        // Ignored?
      }

      server.shutdown();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void testFailClear() {
    Assert.assertFalse(this.store.clearStore());
  }
}
