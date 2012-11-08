/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import org.apache.mina.core.session.IoSession;

import edu.rutgers.winlab.mfirst.net.SessionParameters;

/**
 * Metadata class for messages handled by the server.
 * 
 * @author Robert Moore
 * 
 */
public class IPv4UDPParameters implements SessionParameters{

  /**
   * The session that the message is associated with.
   */
  public IoSession session;
  /**
   * Timestamp for recording performance statistics.
   */
  public final long creationTimestamp = System.nanoTime();

}
