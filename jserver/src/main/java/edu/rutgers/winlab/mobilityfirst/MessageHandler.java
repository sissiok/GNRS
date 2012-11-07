/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class required by MINA to handle messaging-releated events. Most
 * importantly, it passes any received messages to the GNRS server via
 * {@link GNRSServer#messageArrived(IoSession, Object)}.
 * 
 * @author Robert Moore
 * 
 */
public class MessageHandler extends IoHandlerAdapter {

  /**
   * Logging for this class.
   */
  private static final Logger log = LoggerFactory
      .getLogger(MessageHandler.class);

  /**
   * The server that will process the messages.
   */
  private final GNRSServer server;

  /**
   * Creates a new message handler for the server.
   * @param server
   */
  public MessageHandler(final GNRSServer server) {
    super();
    this.server = server;
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) {
    log.error(String.format("[%s] Unhandled exception.", session.toString()),
        cause);
  }

  @Override
  public void messageReceived(IoSession session, Object message)
      throws Exception {
    this.server.messageArrived(session, message);
  }

  @Override
  public void sessionClosed(IoSession session) throws Exception {
    log.info("[{}] Session closed.", session);
  }

  @Override
  public void sessionCreated(IoSession session) throws Exception {
    log.info("[{}] Session created.", session);
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status) {
    log.info("[{}] Session idle: {}", session, status);
  }

  @Override
  public void sessionOpened(IoSession session) throws Exception {
    log.info("[{}] Session opened.", session);
  }
}
