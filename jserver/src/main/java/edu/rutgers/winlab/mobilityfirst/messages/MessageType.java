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
public enum MessageType {
  INSERT ((byte)0),
  LOOKUP ((byte)1),
  INSERT_ACK ((byte)2),
  LOOKUP_RESPONSE ((byte)3);
  
  private final byte value;
  
  MessageType(final byte value){
    this.value = value;
  }
  
  public byte value() { return this.value; }
};
