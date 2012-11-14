/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;

/**
 * @author Robert Moore
 *
 */
public class InsertTaskTest {
  public GNRSServer server;
  
  @Before
  public void prepServer() {
    try {
      Configuration config = new Configuration();
      config.setMappingConfiguration("src/test/resources/map-ipv4.xml");
      config.setNetworkConfiguration("src/test/resources/net-ipv4.xml");
      config.setStoreConfiguration("src/test/resources/berkeleydb.xml");

      this.server = new GNRSServer(config);
    } catch (IOException e) {
      fail("Unable to create server");
    }
  }
  
  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.InsertTask#InsertTask(edu.rutgers.winlab.mfirst.GNRSServer, edu.rutgers.winlab.mfirst.net.SessionParameters, edu.rutgers.winlab.mfirst.messages.InsertMessage)}.
   */
  @Test
  public void testInsertTask() {
    try {
      InsertMessage msg = new InsertMessage();
      msg.setGuid(GUID.fromInt(5));
      msg.setOriginAddress(IPv4UDPAddress.fromASCII("127.0.0.1:9999"));
      msg.setBindings(new NetworkAddress[]{IPv4UDPAddress.fromASCII("127.0.0.1:1234")});
      msg.setRequestId(1);
      msg.setVersion((byte)0);
      InsertTask task = new InsertTask(server,null, msg);
     
      
    } catch (IOException e) {
      e.printStackTrace();
     fail(e.getMessage());
    }catch(Exception e1){
      e1.printStackTrace();
      fail(e1.getMessage());
    }
    Assert.assertTrue(true);
  }

  /**
   * Test method for {@link edu.rutgers.winlab.mfirst.InsertTask#call()}.
   */
  @Test(expected=IllegalArgumentException.class)
  public void testCall() {
    try {
      InsertMessage msg = new InsertMessage();
      msg.setGuid(GUID.fromInt(5));
      msg.setOriginAddress(IPv4UDPAddress.fromASCII("127.0.0.1:9999"));
      msg.setBindings(new NetworkAddress[]{IPv4UDPAddress.fromASCII("127.0.0.1:1234")});
      msg.setRequestId(1);
      msg.setVersion((byte)0);
      InsertTask task = new InsertTask(server,null, msg);
      task.call();
//      Assert.
    } catch (IOException e) {
      e.printStackTrace();
     fail(e.getMessage());
    }
  }

}
