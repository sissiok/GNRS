/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.mina.core.future.WriteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPParameters;

/**
 * Task to handle Lookup messages within the server. Designed to operate
 * independently of any other messages.
 * 
 * @author Robert Moore
 * 
 */
public class LookupTask implements Callable<Object> {
  /**
   * Logging facility for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(LookupTask.class);

  private final LookupMessage message;

  private final SessionParameters params;
  /**
   * The server processing the message.
   */
  private final GNRSServer server;

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
  public Object call() throws Exception {

    long t10 = System.nanoTime();
    GNRSServer.numLookups.incrementAndGet();

    Collection<NetworkAddress> serverAddxes = this.server.getMappings(
        message.getGuid(), message.getOriginAddress().getType());

    long t20 = System.nanoTime();

    
    
    LookupResponseMessage response = new LookupResponseMessage();
    response.setRequestId(this.message.getRequestId());

    if (serverAddxes == null) {
      response.setResponseCode(ResponseCode.FAILED);
    }

    boolean resolvedLocally = false;
    if (serverAddxes != null) {
      for (NetworkAddress addx : serverAddxes) {
        // Loopback? Then the local server should handle it.
        if (this.server.isLocalAddress(addx)) {
          resolvedLocally = true;
          break;
        }

      }
    }

    long t30 = System.nanoTime();

    // At least one IP prefix binding was for the local server
    if (resolvedLocally) {
      // log.debug("Resolving {} locally.", message);

      response.setBindings(this.server.getBindings(this.message.getGuid()));
      response.setResponseCode(ResponseCode.SUCCESS);

    } else {
      response.setResponseCode(ResponseCode.FAILED);
    }
    response.setOriginAddress(this.server.getOriginAddress());
    long t40 = System.nanoTime();
    // log.debug("[{}] Writing {}", this.container.session, response);
    this.server.sendMessage(this.params, response);

    long t50 = System.nanoTime();

    // TODO: Get stats working again.
    GNRSServer.messageLifetime.addAndGet(System.nanoTime()
        - message.createdNanos);

    if (log.isDebugEnabled()) {
      log.debug(String
          .format(
              "Processing: %,dns [Hash: %,dns, NetMap: %,dns, GetBind: %,dns, Write: %,dns] \n",
              t50 - t10, t20 - t10, t30 - t20, t40 - t30, t50 - t40));
    }
    return null;
  }

}
