/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;

/**
 * Interface for classes that wish to respond to GNRS messages as they arrive on
 * a network interface.
 * 
 * @author Robert Moore
 * 
 */
public interface MessageListener {
  /**
   * Called when a GNRS message (request or response) is received by the
   * underlying network.
   * 
   * @param parameters
   *          any network-specific parameters that are associated with this
   *          message. This value should be passed back to the NAO when replying
   *          to the message.
   * @param msg
   *          the message that was received.
   */
  public void messageReceived(final SessionParameters parameters,
      final AbstractMessage msg);
}
