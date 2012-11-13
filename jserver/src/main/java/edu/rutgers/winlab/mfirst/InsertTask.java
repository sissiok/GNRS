/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.InsertResponseMessage;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.SessionParameters;

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
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(InsertTask.class);

  /**
   * The message and some metadata for this task.
   */
  private final transient SessionParameters params;

  /**
   * The received insert message.
   */
  private final transient InsertMessage message;

  /**
   * The server that is handling the message.
   */
  private final transient GNRSServer server;

  /**
   * Creates a new InsertTask for the specified server and message container.
   * 
   * @param server
   *          the server that received or is handling the message.
   * @param params
   *          the session-related metadata.
   * @param message
   *          the message to process
   */
  public InsertTask(final GNRSServer server, final SessionParameters params,
      final InsertMessage message) {
    super();
    this.server = server;
    this.params = params;
    this.message = message;
  }

  @Override
  public Object call() {

    final boolean success = this.server.appendBindings(this.message.getGuid(),
        this.message.getBindings());

    final InsertResponseMessage response = new InsertResponseMessage();
    response.setRequestId(this.message.getRequestId());
    response.setResponseCode(success ? ResponseCode.SUCCESS
        : ResponseCode.FAILED);
    response.setOriginAddress(this.server.getOriginAddress());

    this.server.sendMessage(this.params, response);

    return null;
  }

}
