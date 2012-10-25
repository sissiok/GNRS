/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

/**
 * Acknowledgment message for Insert messages.
 * 
 * @author Robert Moore
 * 
 */
public class InsertAckMessage extends AbstractMessage {
  /**
   * The response code for this message. Note that the wire protocol specifies
   * and unsigned 8-bit integer, but Java does not support unsigned types.
   */
  private byte responseCode;

  /**
   * Creates a new instance of the message.
   */
  public InsertAckMessage() {
    super();
    super.type = MessageType.INSERT_ACK;
  }

  /**
   * The response of the Insert message. Note that the wire protocol specifies
   * and unsigned 8-bit integer, but Java does not support unsigned types.
   * 
   * @return the response code of the Insert message.
   */
  public byte getResponseCode() {
    return this.responseCode;
  }

  /**
   * Sets the response code for this message. Note that the wire protocol
   * specifies and unsigned 8-bit integer, but Java does not support unsigned
   * types.
   * 
   * @param responseCode
   *          the new response code.
   */
  public void setResponseCode(byte responseCode) {
    this.responseCode = responseCode;
  }
}
