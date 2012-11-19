/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.InsertResponseMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;

/**
 * Task to handle Insert messages within the server. Designed to operate
 * independently of any other messages.
 * 
 * @author Robert Moore
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
    final long t10 = System.nanoTime();
    GNRSServer.NUM_INSERTS.incrementAndGet();

    final Collection<NetworkAddress> serverAddxes = this.server.getMappings(
        this.message.getGuid(), this.message.getOriginAddress().getType());

    final long t20 = System.nanoTime();

    boolean resolvedLocally = false;
    if (serverAddxes != null && !serverAddxes.isEmpty()) {
      for (Iterator<NetworkAddress> iter = serverAddxes.iterator(); iter
          .hasNext();) {
        NetworkAddress addx = iter.next();
        // Loopback? Then the local server should handle it.
        if (this.server.isLocalAddress(addx)) {
          iter.remove();
          resolvedLocally = true;
        }
      }
    }
    boolean localSuccess = false;

    if (resolvedLocally) {
      localSuccess = this.server.appendBindings(this.message.getGuid(),
          this.message.getBindings());
    }
    long t30 = System.nanoTime();
    // At least one IP prefix binding was for the local server
    if (this.message.isRecursive()) {
      this.message.setRecursive(false);

      final RelayInfo info = new RelayInfo();
      info.clientMessage = this.message;
      info.remainingServers.addAll(serverAddxes);

      final int requestId = this.server.getNextRequestId();

      final InsertMessage relayMessage = new InsertMessage();
      relayMessage.setGuid(this.message.getGuid());
      relayMessage.setOptions(this.message.getOptions());
      relayMessage.setOriginAddress(this.server.getOriginAddress());
      relayMessage.setVersion((byte) 0);
      relayMessage.setRequestId(requestId);
      relayMessage.setBindings(this.message.getBindings());

      if (serverAddxes != null) {
        this.server.addNeededServer(Integer.valueOf(requestId), info);
        this.server.sendMessage(relayMessage,
            serverAddxes.toArray(new NetworkAddress[] {}));
        info.markAttempt();
      } else {
        LOG.error("Invalid server addresses.  Cannot forward.");
      }
    }
    
    if(resolvedLocally && !this.message.isRecursive()){
      
      InsertResponseMessage response = new InsertResponseMessage();
      response.setOriginAddress(this.server.getOriginAddress());
      response.setRequestId(this.message.getRequestId());
      response.setResponseCode(localSuccess ? ResponseCode.SUCCESS : ResponseCode.FAILED);
      response.setVersion((byte)0);
      
      this.server.sendMessage(response, this.message.getOriginAddress());
      
    }

    long t40 = System.nanoTime();
    if (this.server.getConfig().isCollectStatistics()) {

      GNRSServer.MSG_LIFETIME.addAndGet(System.nanoTime()
          - this.message.createdNanos);

    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format(
          "Processing: %,dns [Map: %,dns, Bind: %,dns, Write: %,dns] \n",
          Long.valueOf(t40 - t10), Long.valueOf(t20 - t10),
          Long.valueOf(t30 - t20), Long.valueOf(t40 - t30)));
    }

    return null;
  }

}
