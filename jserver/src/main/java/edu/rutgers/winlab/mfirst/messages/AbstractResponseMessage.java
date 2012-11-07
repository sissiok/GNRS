/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

/**
 * @author Robert Moore
 *
 */
public abstract class AbstractResponseMessage extends AbstractMessage {

  protected ResponseCode responseCode;
  
  @Override
  protected final int getPayloadLength() {
    // 2-byte response code + 2-byte padding
    return 4 + this.getResponsePayloadLength();
  }
  
  protected abstract int getResponsePayloadLength();

  public ResponseCode getResponseCode() {
    return this.responseCode;
  }

  public void setResponseCode(ResponseCode responseCode) {
    this.responseCode = responseCode;
  }

}
