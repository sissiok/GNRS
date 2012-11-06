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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mobilityfirst.messages.LookupMessage;
import edu.rutgers.winlab.mobilityfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mobilityfirst.messages.ResponseCode;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 *  Task to handle Lookup messages within the server. Designed to operate
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

    GNRSServer.numLookups.incrementAndGet();

    if (this.container == null) {
      return null;
    }
    LookupMessage message = (LookupMessage) this.container.message;
    log.debug("Received {}", message);
    
    Collection<NetworkAddress> hashedAddxes = this.server.guidHasher.hash(message.getGuid(), this.server.config.getNumReplicas());
    
    if(hashedAddxes == null){
      log.error("No binding generated for {}.",message.getGuid());
      return null;
    }
    
    log.debug("Hashed {} -> {}",message.getGuid(),hashedAddxes);
    
    for(NetworkAddress addx : hashedAddxes){
      String asNumString = this.server.networkAddressMap.get(addx);
      if(asNumString == null){
        log.error("Missing GNRS server for {}",addx);
        continue;
      }
      InetSocketAddress replicaAddress = this.server.asAddresses.get(Integer.parseInt(asNumString));
      if(replicaAddress == null){
        log.error("No network information for AS {}.",replicaAddress);
        continue;
      }
      
      // Loopback? Then the local server should handle it.
      if(replicaAddress.getAddress().isLoopbackAddress()){
        log.debug("Retrieving binding locally.");
        LookupResponseMessage response = new LookupResponseMessage();
        response.setRequestId(message.getRequestId());
        response.setResponseCode(ResponseCode.SUCCESS);
        try {
          response.setSenderAddress(NetworkAddress.fromASCII(this.server.config
              .getBindIp()));
        } catch (UnsupportedEncodingException e) {
          log.error("Unable to parse bind IP for the server. Please check the configuration file.");
          return null;
        }
        response.setSenderPort(this.server.config.getListenPort());
        
        response.setBindings(this.server.getBindings(message.getGuid()));
        
        log.debug("[{}] Writing {}", this.container.session, response);
        this.container.session.write(response);
        GNRSServer.messageLifetime.addAndGet(System.nanoTime()
            - this.container.creationTimestamp);
      }
      
      // TODO: Check if this is the server's local address
    }
    
    
    return null;
  }

}
