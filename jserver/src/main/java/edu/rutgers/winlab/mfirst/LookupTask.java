/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;

/**
 * Task to handle Lookup messages within the server. Designed to operate
 * independently of any other messages.
 * 
 * @author Robert Moore
 */
public class LookupTask implements Callable<Object> {
  /**
   * Logging facility for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LookupTask.class);

  /**
   * The received lookup message.
   */
  private final transient LookupMessage message;

  /**
   * Session parameters received with the message.
   */
  private final transient SessionParameters params;
  /**
   * The server processing the message.
   */
  private final transient GNRSServer server;

  /**
   * Creates a new lookup task for the specified server and lookup message
   * container.
   * 
   * @param server
   *          the server that is handling the message.
   * @param params
   *          the session-specific metadata.
   * @param message
   *          the message to process.
   */
  public LookupTask(final GNRSServer server, final SessionParameters params,
      final LookupMessage message) {
    super();
    this.params = params;
    this.message = message;
    this.server = server;
  }

  @Override
  public Object call() {

    final long t10 = System.nanoTime();
    GNRSServer.NUM_LOOKUPS.incrementAndGet();

    final Collection<NetworkAddress> serverAddxes = this.server.getMappings(
        this.message.getGuid(), this.message.getOriginAddress().getType());

    final long t20 = System.nanoTime();

    final LookupResponseMessage response = new LookupResponseMessage();
    response.setRequestId(this.message.getRequestId());

    if (serverAddxes == null || serverAddxes.isEmpty()) {
      response.setResponseCode(ResponseCode.FAILED);
    }

    boolean resolvedLocally = false;
    if (serverAddxes != null && !serverAddxes.isEmpty()) {
      for (final NetworkAddress addx : serverAddxes) {
        // Loopback? Then the local server should handle it.
        if (this.server.isLocalAddress(addx)) {
          resolvedLocally = true;
          break;
        }

      }
    }
    
    long t30 = System.nanoTime();

    // At least one IP prefix binding was for the local server
    if (this.message.isRecursive() & !resolvedLocally) {
      this.message.setRecursive(false);

      RelayInfo info = new RelayInfo();
      info.clientMessage = this.message;
      info.remainingServers.addAll(serverAddxes);

      int requestId = this.server.getNextRequestId();

      LookupMessage relayMessage = new LookupMessage();
      relayMessage.setGuid(this.message.getGuid());
      relayMessage.setOptions(this.message.getOptions());
      relayMessage.setOriginAddress(this.server.getOriginAddress());
      relayMessage.setVersion((byte) 0);
      relayMessage.setRequestId(requestId);

      this.server.addNeededServer(requestId, info);

      this.server.sendMessage(relayMessage,
          serverAddxes.toArray(new NetworkAddress[] {}));
    } else {
      if (resolvedLocally) {
        // LOG.info("Resolving {} locally.", this.message);

        response.setBindings(this.server.getBindings(this.message.getGuid()));
        response.setResponseCode(ResponseCode.SUCCESS);

      } else {
        response.setResponseCode(ResponseCode.FAILED);
      }
      response.setOriginAddress(this.server.getOriginAddress());
      
      // log.debug("[{}] Writing {}", this.container.session, response);
      this.server.sendMessage(response, this.message.getOriginAddress());

    }
    final long t40 = System.nanoTime();
    if (this.server.getConfig().isCollectStatistics()) {
      
      GNRSServer.MSG_LIFETIME.addAndGet(System.nanoTime()
          - this.message.createdNanos);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format(
          "Processing: %,dns [Map: %,dns, GetBind: %,dns, Write: %,dns] \n",
          Long.valueOf(t40 - t10), Long.valueOf(t20 - t10),
          Long.valueOf(t30 - t20), Long.valueOf(t40 - t30)));
    }

    return null;
  }

}
