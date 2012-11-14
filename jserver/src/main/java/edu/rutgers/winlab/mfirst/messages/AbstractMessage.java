/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * Common fields for all GNRS application messages.
 * 
 * @author Robert Moore
 * 
 */

public abstract class AbstractMessage {

  /**
   * The time at which this object was created, in nanosecond. Note that
   * nanosecond time is only relative and has nothing to do with "wall time".
   */
  public final long createdNanos = System.nanoTime();

  /**
   * Actually used as 32-bit unsigned int
   */
  protected long requestId;

  /**
   * Version number of the protocol format.
   */
  protected byte version = 0;

  /**
   * Unsigned 8-bit type value
   */
  protected MessageType type;

  /**
   * Who sent the message originally.
   */
  protected NetworkAddress originAddress;

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
  public NetworkAddress getOriginAddress() {
    return this.originAddress;
  }

  /**
   * Sets the request ID for this message. The protocol specifies an unsigned
   * 32-bit integer value, but Java does not support unsigned types.
   * 
   * @param requestId
   */
  public void setRequestId(final long requestId) {
    this.requestId = (requestId & 0xFFFFFFFFl);
  }

  /**
   * Sets the type of this message. Should only be called by subclasses.
   * 
   * @param type
   *          the type of this message.
   * @see MessageType
   */
  protected void setType(final MessageType type) {
    this.type = type;
  }

  /**
   * Sets the originator address for this message.
   * 
   * @param address
   *          the originator address for this message.
   */
  public void setOriginAddress(final NetworkAddress address) {
    this.originAddress = address;
  }

  /**
   * The total encoded length of this message, in bytes.
   * 
   * @return the length of this message, in bytes, when encoded according to the
   *         network protocol.
   */
  public int getMessageLength() {
    // Version, type, length, request id, requestor address
    int length = 12 + this.getPayloadLength();
    if(this.originAddress != null){
      length += this.originAddress.getLength();
    }
    return length;
  }

  /**
   * The length of the "payload" section of this message.
   * 
   * @return the length of the payload of this message.
   */
  protected abstract int getPayloadLength();

  /**
   * The version value of this message.
   * 
   * @return the version of this message.
   */
  public byte getVersion() {
    return this.version;
  }

  /**
   * Sets the version value of this message.
   * 
   * @param version
   *          the new version value.
   */
  public void setVersion(final byte version) {
    this.version = version;
  }
}
