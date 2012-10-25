/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

/**
 * @author Robert Moore
 *
 */
public class InsertAckMessage extends AbstractMessage {
  private byte responseCode;
  
  public InsertAckMessage(){
    super();
    super.type = MessageType.INSERT_ACK;
  }

  public byte getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(byte responseCode) {
    this.responseCode = responseCode;
  }
}
