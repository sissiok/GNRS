/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */

public abstract class AbstractMessage {

  /**
   * Actually used as 32-bit unsigned int
   */
  protected long requestId;

  /**
   * Unsigned 8-bit type value
   */
  protected MessageType type;

  /**
   * Who sent the message originally.
   */
  protected NetworkAddress senderAddress;

  /**
   * The listen port of the sender.
   */
  protected long senderPort;

  /**
   * Protected constructor. Only to be called by subclasses.
   */
  protected AbstractMessage() {
    super();
  }

  /**
   * Gets the request ID of this message. Note that the wire protocol specifies
   * an unsigned 32-bit integer, but Java doesn't support unsigned types.
   * 
   * @return the request ID of this message.
   */
  public long getRequestId() {
    return this.requestId;
  }

  /**
   * Gets the type of this message. Depends on the subclass of this message.
   * 
   * @return the type value of this message.
   * @see MessageType
   */
  public MessageType getType() {
    return this.type;
  }

  /**
   * Gets the NetworkAddress of the originator of this message.
   * 
   * @return the NetworkAddress of the message originator.
   */
  public NetworkAddress getSenderAddress() {
    return this.senderAddress;
  }

  /**
   * Gets the sender port for this message. Note that the wire protocol
   * specifies an unsigned 32-bit integer, but Java doesn't support unsigned
   * types.
   * 
   * @return the sender port value of this message.
   */
  public long getSenderPort() {
    return this.senderPort;
  }

  /**
   * Sets the request ID for this message. The protocol specifies an unsigned
   * 32-bit integer value, but Java does not support unsigned types.
   * 
   * @param requestId
   */
  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

  /**
   * Sets the type of this message. Should only be called by subclasses.
   * 
   * @param type
   *          the type of this message.
   * @see MessageType
   */
  protected void setType(MessageType type) {
    this.type = type;
  }

  /**
   * Sets the sender (originator) address for this message.
   * 
   * @param senderAddress
   *          the sender address for this message.
   */
  public void setSenderAddress(NetworkAddress senderAddress) {
    this.senderAddress = senderAddress;
  }

  /**
   * Sets the sender (originator) port for this message. Note that the wire
   * protocol specifies an unsigned 32-bit value, but Java does not support
   * unsigned types.
   * 
   * @param senderPort
   *          the sender port value.
   */
  public void setSenderPort(long senderPort) {
    this.senderPort = senderPort;
  }
}
