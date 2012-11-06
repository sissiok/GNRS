/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.mina.core.future.WriteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mobilityfirst.messages.LookupMessage;
import edu.rutgers.winlab.mobilityfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mobilityfirst.messages.ResponseCode;
import edu.rutgers.winlab.mobilityfirst.structures.AddressType;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

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

  /**
   * The message and some metadata.
   */
  private final MessageContainer container;
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
   * @param container
   *          the message to process and some metadata.
   */
  public LookupTask(final GNRSServer server, final MessageContainer container) {
    super();
    this.container = container;
    this.server = server;
  }

  @Override
  public Object call() throws Exception {

    long t10 = System.nanoTime();
    GNRSServer.numLookups.incrementAndGet();

    if (this.container == null) {
      return null;
    }
    LookupMessage message = (LookupMessage) this.container.message;
    // log.debug("Received {}", message);

    Collection<NetworkAddress> hashedAddxes = this.server.guidHasher.hash(
        message.getGuid(), AddressType.INET_4_UDP, this.server.config.getNumReplicas());

    long t20 = System.nanoTime();

    if (hashedAddxes == null) {
      log.error("No binding generated for {}.", message.getGuid());
      return null;
    }

    // log.debug("Hashed {} -> {}", message.getGuid(), hashedAddxes);

    boolean resolvedLocally = false;

    for (NetworkAddress addx : hashedAddxes) {
      String asNumString = this.server.networkAddressMap.get(addx);
      if (asNumString == null) {
        log.error("Missing GNRS server for {}", addx);
        continue;
      }
      InetSocketAddress replicaAddress = this.server.asAddresses.get(Integer
          .parseInt(asNumString));
      if (replicaAddress == null) {
        log.error("No network information for AS {}.", replicaAddress);
        continue;
      }

      // Loopback? Then the local server should handle it.
      // TODO: Check if this is the server's local address
      if (replicaAddress.getAddress().isLoopbackAddress()
          || replicaAddress.equals(this.server.localAddress)) {
        resolvedLocally = true;
        break;
      }

    }

    long t30 = System.nanoTime();

    LookupResponseMessage response = new LookupResponseMessage();
    response.setRequestId(message.getRequestId());

    try {
      response.setOriginAddress(NetworkAddress.ipv4FromASCII(this.server.config
          .getBindIp()));
    } catch (UnsupportedEncodingException e) {
      log.error("Unable to parse bind IP for the server. Please check the configuration file.");
      return null;
    }
    response.setSenderPort(this.server.config.getListenPort());

    // At least one IP prefix binding was for the local server
    if (resolvedLocally) {
      // log.debug("Resolving {} locally.", message);

      response.setBindings(this.server.getBindings(message.getGuid()));
      response.setResponseCode(ResponseCode.SUCCESS);

    } else {
      response.setResponseCode(ResponseCode.ERROR);
    }
    long t40 = System.nanoTime();
    // log.debug("[{}] Writing {}", this.container.session, response);
    WriteFuture future = this.container.session.write(response);

    // FIXME: Remove the following line to allow messages to be buffered and
    // keep going
    future.awaitUninterruptibly();

    long t50 = System.nanoTime();

    GNRSServer.messageLifetime.addAndGet(System.nanoTime()
        - this.container.creationTimestamp);

    if (log.isDebugEnabled()) {
      log.debug(String
          .format(
              "Processing: %,dns [Hash: %,dns, NetMap: %,dns, GetBind: %,dns, Write: %,dns] \n",
              t50 - t10, t20 - t10, t30 - t20, t40 - t30, t50 - t40));
    }
    return null;
  }

}
