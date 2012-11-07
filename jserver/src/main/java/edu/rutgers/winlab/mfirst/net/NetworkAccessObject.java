/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;

/**
 * Interface that provides network communication for the GNRS server.
 * 
 * @author Robert Moore
 * 
 */
public interface NetworkAccessObject {
  /**
   * Registers the listener to receive messages as they arrive from the network.
   * 
   * @param listener
   *          the listener to receive messages.
   */
  public void addMessageListener(final MessageListener listener);

  /**
   * Stops the listener from receiving messages as they arrive. Messages may
   * continue arrive after this method is called if they were buffered
   * by the NetworkAccessObject.
   * 
   * @param listener
   *          the listener to remove
   */
  public void removeMessageListener(final MessageListener listener);

  /**
   * Sends a message to the network.
   * 
   * @param parameters
   *          the parameters necessary to send the message
   * @param message
   *          the message to send
   */
  public void sendMessage(final SessionParameters parameters,
      final AbstractMessage message);

  public void endSession(final SessionParameters parameters);

}
