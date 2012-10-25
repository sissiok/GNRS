/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import edu.rutgers.winlab.mobilityfirst.structures.GNRSRecord;
import edu.rutgers.winlab.mobilityfirst.structures.GUID;
import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

import java.util.Collection;

/**
 * @author Robert Moore
 * 
 */
public class InsertMessage extends AbstractMessage {
  private GUID guid;
  private byte destinationFlag; // dest_flag=1: the destination for the GUID entry is
                       // computed out; 0: the destination address hasn't been
                       // computed
  private GUIDBinding[] bindings;
  
  public InsertMessage(){
    super();
    super.type = MessageType.INSERT;
  }
  
  
  public GUID getGuid() {
    return guid;
  }
  public void setGuid(GUID guid) {
    this.guid = guid;
  }
  public byte getDestinationFlag() {
    return destinationFlag;
  }
  public void setDestinationFlag(byte destinationFlag) {
    this.destinationFlag = destinationFlag;
  }
  public GUIDBinding[] getBindings() {
    return bindings;
  }
  public void setBindings(GUIDBinding[] bindings) {
    this.bindings = bindings;
  }

}
