/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.Option;
import edu.rutgers.winlab.mfirst.messages.RecursiveRequestOption;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;
import edu.rutgers.winlab.mfirst.storage.GUIDBinding;

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

    GNRSServer.NUM_LOOKUPS.incrementAndGet();

    Collection<GUIDBinding> cachedBindings = this.server
        .getCached(this.message.getGuid());
    if (cachedBindings != null && !cachedBindings.isEmpty()) {
      LOG.info("Using cached values for {}.",this.message.getGuid());
      LookupResponseMessage response = new LookupResponseMessage();
      
      NetworkAddress[] addxes = new NetworkAddress[cachedBindings.size()];
      int i = 0;
      for(GUIDBinding b : cachedBindings){
        addxes[i++] = b.getAddress();
      }
      
      response.setBindings(addxes);
      response.setOriginAddress(this.server.getOriginAddress());
      response.setRequestId(this.message.getRequestId());
      response.setResponseCode(ResponseCode.SUCCESS);
      response.setVersion((byte)0);
      
      this.server.sendMessage(response, this.message.getOriginAddress());
    } else {

      final Collection<NetworkAddress> serverAddxes = this.server.getMappings(
          this.message.getGuid(), this.message.getOriginAddress().getType());

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
      
      boolean recursive = false;
      List<Option> options = this.message.getOptions();
      if(!options.isEmpty()){
        for(Option opt : options){
          if(opt instanceof RecursiveRequestOption){
            recursive = ((RecursiveRequestOption)opt).isRecursive();
            break;
          }
        }
      }

      // No bindings were for the local server
      if (recursive && !resolvedLocally) {
//        this.message.setRecursive(false);

        RelayInfo info = new RelayInfo();
        info.clientMessage = this.message;
        info.remainingServers.addAll(serverAddxes);

        int requestId = this.server.getNextRequestId();

        LookupMessage relayMessage = new LookupMessage();
        relayMessage.setGuid(this.message.getGuid());
        for(Option opt : this.message.getOptions()){
          if(!(opt instanceof RecursiveRequestOption)){
            relayMessage.addOption(opt);
          }
        }
        relayMessage.finalizeOptions();
        relayMessage.setOriginAddress(this.server.getOriginAddress());
        relayMessage.setVersion((byte) 0);
        relayMessage.setRequestId(requestId);

        this.server.addNeededServer(requestId, info);

        this.server.sendMessage(relayMessage,
            serverAddxes.toArray(new NetworkAddress[] {}));
        info.markAttempt();
      }
      // Either resolved at this server or non-recursive
      else {
        // Resolved at this server
        if (resolvedLocally) {

          response.setBindings(this.server.getBindings(this.message.getGuid()));
          response.setResponseCode(ResponseCode.SUCCESS);

        }
        // Non-local but not recursive, so a problem with the remote host
        else {
          response.setResponseCode(ResponseCode.FAILED);
        }
        response.setOriginAddress(this.server.getOriginAddress());

        // log.debug("[{}] Writing {}", this.container.session, response);
        this.server.sendMessage(response, this.message.getOriginAddress());

      }

      if (this.server.getConfig().isCollectStatistics()) {

        GNRSServer.MSG_LIFETIME.addAndGet(System.nanoTime()
            - this.message.createdNanos);
      }
    }

    return null;
  }
}
