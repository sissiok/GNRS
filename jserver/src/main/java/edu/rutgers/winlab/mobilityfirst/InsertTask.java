/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mobilityfirst.messages.InsertAckMessage;
import edu.rutgers.winlab.mobilityfirst.messages.InsertMessage;
import edu.rutgers.winlab.mobilityfirst.messages.ResponseCode;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 *
 */
public class InsertTask implements Callable<Object> {
  
  private static final Logger log = LoggerFactory.getLogger(InsertTask.class);
  
  private final MessageContainer container;
  private final GNRSServer server;
  
  public InsertTask(final GNRSServer server, final MessageContainer container){
    super();
    this.server = server;
    this.container = container;
  }

  /* (non-Javadoc)
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public Object call() throws Exception {
    InsertMessage msg = (InsertMessage) container.message;
    InsertAckMessage response = new InsertAckMessage();
    response.setRequestId(msg.getRequestId());
    response.setResponseCode(ResponseCode.SUCCESS);

    try {
      response.setSenderAddress(NetworkAddress.fromASCII(this.server.config
          .getBindIp()));
    } catch (UnsupportedEncodingException e) {
      log.error("Unable to parse bind IP for the server. Please check the configuration file.");
     return null;
    }
    response.setSenderPort(this.server.config.getListenPort() & 0xFFFFFFFFl);
    if (!container.session.isClosing()) {
      log.debug("[{}] Writing {}", container.session, response);
      container.session.write(response);
    } else {
      log.warn("[{}] Unable to write {} to closing session.",
          container.session, response);
    }
    return null;
  }

}
