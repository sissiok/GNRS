/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import org.apache.mina.core.session.IoSession;

import edu.rutgers.winlab.mobilityfirst.messages.AbstractMessage;

/**
 * Metadata class for messages handled by the server.
 * 
 * @author Robert Moore
 * 
 */
public class MessageContainer {

  /**
   * The received message.
   */
  public AbstractMessage message;
  /**
   * The session that the message is associated with.
   */
  public IoSession session;
  /**
   * Timestamp for recording performance statistics.
   */
  public final long creationTimestamp = System.nanoTime();

}
