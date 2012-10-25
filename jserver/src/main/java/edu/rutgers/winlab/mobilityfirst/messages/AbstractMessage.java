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
  protected int senderPort;
  
  protected AbstractMessage(){
    super();
  }

  public long getRequestId() {
    return requestId;
  }

  public MessageType getType() {
    return type;
  }

  public NetworkAddress getSenderAddress() {
    return senderAddress;
  }

  public int getSenderPort() {
    return senderPort;
  }
}
