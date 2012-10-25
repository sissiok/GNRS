/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import java.util.Collection;

import edu.rutgers.winlab.mobilityfirst.structures.GUID;

/**
 * @author Robert Moore
 * 
 */
public class LookupMessage extends AbstractMessage {
  private GUID guid;
  private byte destinationFlag;// dest_flag=1: the destination for the GUID
                               // entry is computed out; 0: the destination
                               // address hasn't been computed
  
  public LookupMessage(){
    super();
    super.type = MessageType.LOOKUP;
  }
  
  public GUID getGuid() {
    return this.guid;
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
}
