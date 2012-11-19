/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.Set;

import org.apache.mina.util.ConcurrentHashSet;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * @author Robert Moore
 *
 */
public class RelayInfo {

  /**
   * The original client message.
   */
  public AbstractMessage clientMessage;
  
  /**
   * The addresses for the response (if a LookupResponse).
   */
  public Set<NetworkAddress> responseAddresses = new ConcurrentHashSet<NetworkAddress>();
  
  /**
   * The servers that have yet to respond.
   */
  public final Set<NetworkAddress> remainingServers = new ConcurrentHashSet<NetworkAddress>();
  
  /**
   * Number of attempts made so far.
   */
  private transient int numAttempts = 0;
  
  /**
   * Timestamp of the last attempt to send the message.
   */
  private transient long lastAttempt = 0l;
  
  /**
   * Marks this info to indicate that an attempt was made.
   * @return returns the current number of attempts (including this one).
   */
  public int markAttempt(){
    
    this.lastAttempt = System.currentTimeMillis();
    return ++this.numAttempts;
  }

  /**
   * Get the number of attempts made.
   * @return the number of attempts made.
   * @see #markAttempt()
   */
  public int getNumAttempts() {
    return this.numAttempts;
  }

  /**
   * Get the timestamp (in milliseconds since the Unix epoch) of the last
   * attempt.
   * @return the timestamp of the last attempt, or 0 if no attempt has previously been made.
   * @see #markAttempt()
   */
  public long getAttemptTs() {
    return this.lastAttempt;
  }
  
}
