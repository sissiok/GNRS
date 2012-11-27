/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.AbstractResponseMessage;
import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.InsertResponseMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.Option;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * Callable task for handling timeouts.
 * 
 * @author Robert Moore
 */
public class TimeoutTask implements Callable<Object> {
  /**
   * Logger for this class.
   */
  private static final transient Logger LOG = LoggerFactory
      .getLogger(TimeoutTask.class);

  /**
   * The information about the expired message.
   */
  private final transient RelayInfo info;

  /**
   * Maximum number of attempts before declaring a failure.
   */
  private final transient GNRSServer server;

  /**
   * Creates a new timeout task for the relay info.
   * 
   * @param server
   *          reference back to the server
   * @param info
   *          the information about the timed-out message
   */
  public TimeoutTask(final GNRSServer server, final RelayInfo info) {
    super();
    this.info = info;
    this.server = server;
  }

  @Override
  public Object call() throws Exception {
    // Too many failed attempts, respond with FAILURE
    if(this.info.getNumAttempts() >= this.server.getConfig().getNumAttempts()){
      AbstractResponseMessage response = null;
      if(this.info.clientMessage instanceof LookupMessage){
        response = new LookupResponseMessage();
        if(!this.info.responseAddresses.isEmpty()){
          ((LookupResponseMessage)response).setBindings(this.info.responseAddresses.toArray(new NetworkAddress[]{}));
          response.setResponseCode(ResponseCode.SUCCESS);
        }else{
          response.setResponseCode(ResponseCode.FAILED);
        }
      } else if(this.info.clientMessage instanceof InsertMessage){
        response = new InsertResponseMessage();
        response.setResponseCode(ResponseCode.FAILED);
      }
      // We have a message to send
      if(response != null){
        response.setOriginAddress(this.server.getOriginAddress());
        response.setRequestId(this.info.clientMessage.getRequestId());
        response.setResponseCode(ResponseCode.FAILED);
        response.setVersion((byte)0);
        
        this.server.sendMessage(response, this.info.clientMessage.getOriginAddress());
      }
    }
    // Can still reattempt the message
    else{
      // Lookup message
      if(this.info.clientMessage instanceof LookupMessage){
        LookupMessage orig = (LookupMessage)this.info.clientMessage;
      
      Integer requestId = Integer.valueOf(this.server.getNextRequestId());

      LookupMessage relayMessage = new LookupMessage();
      relayMessage.setGuid(orig.getGuid());
      List<Option> options = orig.getOptions();
      if(!options.isEmpty()){
        for(Option opt : options){
          relayMessage.addOption(opt);
        }
      }
      relayMessage.finalizeOptions();
      relayMessage.setOriginAddress(this.server.getOriginAddress());
      relayMessage.setVersion((byte) 0);
      relayMessage.setRequestId(requestId.intValue());

      this.server.addNeededServer(requestId, this.info);
      this.info.markAttempt();
      this.server.sendMessage(relayMessage,
          this.info.remainingServers.toArray(new NetworkAddress[]{}));
      
      }else if(this.info.clientMessage instanceof InsertMessage){
        InsertMessage orig = (InsertMessage)this.info.clientMessage;
        Integer requestId = Integer.valueOf(this.server.getNextRequestId());

        final InsertMessage relayMessage = new InsertMessage();
        relayMessage.setGuid(orig.getGuid());
        List<Option> options = orig.getOptions();
        if(!options.isEmpty()){
          for(Option opt : options){
            relayMessage.addOption(opt);
          }
        }
        relayMessage.finalizeOptions();
        relayMessage.setOriginAddress(this.server.getOriginAddress());
        relayMessage.setVersion((byte) 0);
        relayMessage.setRequestId(requestId.intValue());
        relayMessage.setBindings(orig.getBindings());

        this.server.addNeededServer(requestId, this.info);

        this.info.markAttempt();
        this.server.sendMessage(relayMessage,
            this.info.remainingServers.toArray(new NetworkAddress[]{}));
        
      }else {
        LOG.warn("Retry unhandled for type {}",this.info.clientMessage.getClass());
      }
    }
    return null;
  }
}
