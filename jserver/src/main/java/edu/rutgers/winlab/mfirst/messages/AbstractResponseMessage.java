/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

/**
 * Common fields for all response messages in the GNRS protocol.
 * 
 * @author Robert Moore
 * 
 */
public abstract class AbstractResponseMessage extends AbstractMessage {

  /**
   * Value to indicate success/failure or other conditions resulting from a
   * request.
   */
  protected ResponseCode responseCode;

  @Override
  protected final int getPayloadLength() {
    // 2-byte response code + 2-byte padding
    return 4 + this.getResponsePayloadLength();
  }

  /**
   * Gets the length, in bytes, of the "payload" of this response message.
   * 
   * @return the payload length, in bytes.
   */
  protected abstract int getResponsePayloadLength();

  /**
   * Gets the response code for this message. The response code indicates
   * success, failure, or other results of requests.
   * 
   * @return the response code for this message.
   */
  public ResponseCode getResponseCode() {
    return this.responseCode;
  }

  /**
   * Sets the response code for this message.
   * 
   * @param responseCode
   *          the new response code.
   * @see ResponseCode
   */
  public void setResponseCode(ResponseCode responseCode) {
    this.responseCode = responseCode;
  }

}
