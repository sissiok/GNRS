/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.AbstractResponseMessage;
import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.InsertResponseMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;

/**
 * @author Robert Moore
 */
public class ResponseTask implements Callable<Object> {

  private static final Logger LOG = LoggerFactory.getLogger(ResponseTask.class);

  private final transient GNRSServer server;
  private final transient SessionParameters params;
  private final transient AbstractResponseMessage message;

  public ResponseTask(final GNRSServer server, final SessionParameters params,
      final AbstractResponseMessage message) {
    super();
    this.server = server;
    this.params = params;
    this.message = message;
  }

  @Override
  public Object call() throws Exception {

    GNRSServer.NUM_RESPONSES.incrementAndGet();
    // LOG.info("Using relay info for {}", respMsg);
    Integer reqId = Integer.valueOf((int) this.message.getRequestId());
    RelayInfo info = this.server.awaitingResponse.get(reqId);
    // LOG.info("[{}]Using relay info for {}", respMsg, info.clientMessage);
    // We are actually expecting this response
    if (info != null) {
      // LOG.info("Retrieved relay info");
      // This is a server we need a response from
      if (info.remainingServers.remove(this.message.getOriginAddress())) {
        // LOG.info("Removed {} from servers", respMsg.getOriginAddress());
        // Add the bindings (if any)
        if (this.message instanceof LookupResponseMessage) {
          LookupResponseMessage lrm = (LookupResponseMessage) this.message;
          for (NetworkAddress netAddr : lrm.getBindings()) {
            // LOG.info("Adding {} to LKR bindings.", lrm.getBindings());
            info.responseAddresses.add(netAddr);
          }
        }
        // If this was the last server, reply to the client
        if (info.remainingServers.isEmpty()) {
          // LOG.info("All servers have replied.");
          this.server.awaitingResponse.remove(reqId);

          if (info.clientMessage instanceof LookupMessage) {
            LookupResponseMessage lrm = new LookupResponseMessage();
            lrm.setRequestId(info.clientMessage.getRequestId());
            lrm.setOriginAddress(this.server.getOriginAddress());
            lrm.setResponseCode(ResponseCode.SUCCESS);
            lrm.setVersion((byte) 0x0);
            lrm.setBindings(info.responseAddresses
                .toArray(new NetworkAddress[] {}));
            // LOG.info("Going to send reply back to client: {}", lrm);
            this.server.sendMessage(lrm, info.clientMessage.getOriginAddress());
          } else if (info.clientMessage instanceof InsertMessage) {
            InsertResponseMessage irm = new InsertResponseMessage();
            irm.setRequestId(info.clientMessage.getRequestId());
            irm.setOriginAddress(this.server.getOriginAddress());
            irm.setResponseCode(ResponseCode.SUCCESS);
            irm.setVersion((byte) 0);
            this.server.sendMessage(irm, info.clientMessage.getOriginAddress());
          } else {
            LOG.error("Unsupported message received?");
          }
        } else {
          // LOG.info("Awaiting servers: {}", info.remainingServers);
        }
      } else {
        LOG.warn("Unable to find relay info for {}", this.message);
      }
    }

    if (this.server.getConfig().isCollectStatistics()) {

      GNRSServer.MSG_LIFETIME.addAndGet(System.nanoTime()
          - this.message.createdNanos);

    }

    LOG.info("Outstanding requests: {}",this.server.awaitingResponse.size());
    
    return null;
  }

}
