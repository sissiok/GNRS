/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import org.apache.mina.core.session.IoSession;

import edu.rutgers.winlab.mobilityfirst.messages.AbstractMessage;

/**
 * @author Robert Moore
 * 
 */
public class MessageContainer {

  public AbstractMessage message;
  public IoSession session;
  public final long creationTimestamp = System.nanoTime();

}
