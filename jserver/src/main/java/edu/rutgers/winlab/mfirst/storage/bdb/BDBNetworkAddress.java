/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.persist.model.Persistent;

import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * BerkeleyDB-compatible version of a NetworkAddress
 * 
 * @author Robert Moore
 * 
 */
@Persistent
public class BDBNetworkAddress {

  public int type;
  
  public byte[] value;

  public static BDBNetworkAddress fromNetworkAddress(final NetworkAddress na){
    BDBNetworkAddress newAddr = new BDBNetworkAddress();
    newAddr.type = na.getType().value();
    newAddr.value = na.getValue();
    return newAddr;
  }
  
  public NetworkAddress toNetworkAddress(){
    return new NetworkAddress(AddressType.valueOf(this.type), this.value);
  }
}
