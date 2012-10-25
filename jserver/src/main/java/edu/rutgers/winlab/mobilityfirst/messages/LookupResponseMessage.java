/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;

/**
 * @author Robert Moore
 *
 */
public class LookupResponseMessage extends AbstractMessage {
  private byte responseCode;
  private GUIDBinding[] bindings;
  
  public LookupResponseMessage(){
    super();
    super.type = MessageType.LOOKUP_RESPONSE;
  }
  
  public byte getResponseCode() {
    return this.responseCode;
  }
  public void setResponseCode(byte responseCode) {
    this.responseCode = responseCode;
  }
  public GUIDBinding[] getBindings() {
    return this.bindings;
  }
  public void setBindings(GUIDBinding[] bindings) {
    this.bindings = bindings;
  }
}
