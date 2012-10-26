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
 * @author Robert Moore
 * 
 */
public class MessageHandler extends IoHandlerAdapter {
  
  private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

  private GNRSServer server;

  public MessageHandler(final GNRSServer server) {
    super();
    this.server = server;
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) {
    log.error(String.format("[%s] Unhandled exception.",session.toString()), cause);
  }

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception{
    log.debug("[{}] Received message: {}", session, message);
    this.server.messageArrived(session, message);
  }
  
  @Override
  public void sessionClosed(IoSession session) throws Exception {
    log.debug("[{}] Session closed.", session);
  }
  
  @Override
  public void sessionCreated(IoSession session) throws Exception {
    log.debug("[{}] Session created.", session);
  }
  
  @Override
  public void sessionIdle(IoSession session, IdleStatus status){
    log.debug("[{}] Session idle: {}", session, status);
  }
  
  @Override
  public void sessionOpened(IoSession session) throws Exception {
    log.debug("[{}] Session opened.", session);
  }
}

