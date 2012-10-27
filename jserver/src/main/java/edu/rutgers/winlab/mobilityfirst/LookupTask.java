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

import edu.rutgers.winlab.mobilityfirst.messages.LookupMessage;
import edu.rutgers.winlab.mobilityfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mobilityfirst.messages.ResponseCode;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 *
 */
public class LookupTask implements Callable<Object> {
  private static final Logger log = LoggerFactory.getLogger(LookupTask.class);

  private final MessageContainer container;
  private final GNRSServer server;
  
  public LookupTask(final GNRSServer server, final MessageContainer container){
    super();
    this.container = container;
    this.server = server;
  }
  
  /* (non-Javadoc)
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public Object call() throws Exception {
    

    ++GNRSServer.numLookups;

    if (container == null) {
      return null;
    }
    LookupMessage message = (LookupMessage) container.message;
    LookupResponseMessage response = new LookupResponseMessage();
    response.setRequestId(message.getRequestId());
    response.setResponseCode(ResponseCode.ERROR);
    try {
      response.setSenderAddress(NetworkAddress.fromASCII(this.server.config
          .getBindIp()));
    } catch (UnsupportedEncodingException e) {
      log.error("Unable to parse bind IP for the server. Please check the configuration file.");
      return null;
    }
    response.setSenderPort(this.server.config.getListenPort());
    log.debug("[{}] Writing {}", container.session, response);
    container.session.write(response);
    return null;
  }

 

}
