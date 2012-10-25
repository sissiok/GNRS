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
  /**
   * An Insert message.
   * 
   * @see InsertMessage
   */
  INSERT((byte) 0),
  /**
   * A Lookup message.
   * 
   * @see LookupMessage
   */
  LOOKUP((byte) 1),
  /**
   * An InsertAck message.
   * 
   * @see InsertAckMessage
   */
  INSERT_ACK((byte) 2),
  /**
   * A LookupResponse message.
   * 
   * @see LookupResponseMessage
   */
  LOOKUP_RESPONSE((byte) 3);

  /**
   * The protocol-compatible value of this message type.
   */
  private final byte value;

  /**
   * Creates a new MessageType with the specified byte value.
   * 
   * @param value
   */
  MessageType(final byte value) {
    this.value = value;
  }

  /**
   * Gets the GNRS protocol-appropriate byte for this message type.
   * 
   * @return the byte value of this message type.
   */
  public byte value() {
    return this.value;
  }
}
