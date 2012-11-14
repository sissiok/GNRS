/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;

/**
 * Interface that provides network communication for the GNRS server.
 * 
 * @author Robert Moore
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
   * continue arrive after this method is called if they were buffered by the
   * NetworkAccessObject.
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

  /**
   * Invoked when a session terminates and the NAO should clean-up any stored
   * state.
   * 
   * @param parameters
   *          session parameters.
   */
  public void endSession(final SessionParameters parameters);

  /**
   * Determine whether a specified network address object references the local
   * server.
   * 
   * @param address
   *          the address to check.
   * @return {@code true} if the NetworkAddress identifies this server, else
   *         {@code false}.
   */
  public boolean isLocal(final NetworkAddress address);

  /**
   * Returns this server's "origin" address as sent in request or response
   * messages.
   * 
   * @return the value of the "Origin Address" field for messages originating at
   *         this server.
   */
  public NetworkAddress getOriginAddress();

  /**
   * Called before the server exits so the network access can clean-up any
   * resources.
   */
  public void doShutdown();

}
