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

import edu.rutgers.winlab.mobilityfirst.messages.InsertResponseMessage;
import edu.rutgers.winlab.mobilityfirst.messages.InsertMessage;
import edu.rutgers.winlab.mobilityfirst.messages.ResponseCode;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * Task to handle Insert messages within the server. Designed to operate
 * independently of any other messages.
 * 
 * @author Robert Moore
 * 
 */
public class InsertTask implements Callable<Object> {

  /**
   * Logging for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(InsertTask.class);

  /**
   * The message and some metadata for this task.
   */
  private final MessageContainer container;

  /**
   * The server that is handling the message.
   */
  private final GNRSServer server;

  /**
   * Creates a new InsertTask for the specified server and message container.
   * 
   * @param server
   *          the server that received or is handling the message.
   * @param container
   *          the message and some metadata.
   */
  public InsertTask(final GNRSServer server, final MessageContainer container) {
    super();
    this.server = server;
    this.container = container;
  }

  @Override
  public Object call() throws Exception {
    // Just send back a SUCCESS message for now.
    InsertMessage msg = (InsertMessage) this.container.message;

    boolean success = this.server.insertBindings(msg.getGuid(),
        msg.getBindings());

    InsertResponseMessage response = new InsertResponseMessage();
    response.setRequestId(msg.getRequestId());
    response.setResponseCode(success ? ResponseCode.SUCCESS
        : ResponseCode.FAILED);

    try {
      response.setOriginAddress(NetworkAddress.ipv4FromASCII(this.server.config
          .getBindIp() +":" + this.server.config.getListenPort()));
    } catch (UnsupportedEncodingException e) {
      log.error("Unable to parse bind IP for the server. Please check the configuration file.");
      return null;
    }


    this.container.session.write(response);

    return null;
  }

}
