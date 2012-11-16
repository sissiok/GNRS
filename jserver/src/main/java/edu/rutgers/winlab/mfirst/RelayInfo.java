/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.HashSet;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ConcurrentHashSet;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mfirst.messages.AbstractResponseMessage;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * @author Robert Moore
 *
 */
public class RelayInfo {

  public AbstractMessage clientMessage;
  
  public Set<NetworkAddress> responseAddresses = new ConcurrentHashSet<NetworkAddress>();
  
  public final Set<NetworkAddress> remainingServers = new ConcurrentHashSet<NetworkAddress>();
  
}
