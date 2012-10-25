/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

/**
 * @author Robert Moore
 * 
 */
public class GNRSProtocolCodecFactory extends DemuxingProtocolCodecFactory {
  /**
   * Generates a new protocol codec factory for a server or client using the
   * GNRS protocol.
   * 
   * @param isServer
   *          {@code true} if the factory should be for a server
   */
  public GNRSProtocolCodecFactory(boolean isServer) {
    super();
    if (isServer) {
      super.addMessageDecoder(InsertDecoder.class);
      super.addMessageDecoder(LookupDecoder.class);

      super.addMessageEncoder(LookupResponseMessage.class,
          LookupResponseEncoder.class);
      super.addMessageEncoder(InsertAckMessage.class, InsertAckEncoder.class);
    } else {
      super.addMessageEncoder(InsertMessage.class, InsertEncoder.class);
      super.addMessageEncoder(LookupMessage.class, LookupEncoder.class);

      super.addMessageDecoder(LookupResponseDecoder.class);
      super.addMessageDecoder(InsertAckDecoder.class);
    }
  }
}
