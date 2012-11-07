/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;

/**
 * @author Robert Moore
 *
 */
public interface MessageListener {
  public void messageReceived(final SessionParameters parameters, final AbstractMessage msg);
}
