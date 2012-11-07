/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

/**
 * Acknowledgment message for Insert messages.
 * 
 * @author Robert Moore
 * 
 */
public class InsertResponseMessage extends AbstractResponseMessage {
  /**
   * Creates a new instance of the message.
   */
  public InsertResponseMessage() {
    super();
    super.type = MessageType.INSERT_RESPONSE;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("INR #");
    sb.append(this.getRequestId()).append("/").append(super.responseCode);
    return sb.toString();
  }

  
  @Override
  protected int getResponsePayloadLength() {
    // No additional length
    return 0;
  }
}
