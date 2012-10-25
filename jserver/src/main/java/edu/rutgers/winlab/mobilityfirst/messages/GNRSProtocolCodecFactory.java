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
  public GNRSProtocolCodecFactory(boolean isServer)
  {
    super();
    if(isServer)
    {
      super.addMessageDecoder(InsertDecoder.class);
      super.addMessageDecoder(UpdateDecoder.class);
      super.addMessageDecoder(LookupDecoder.class);
      
      super.addMessageEncoder(LookupResponseMessage.class, LookupResponseEncoder.class);
    }
    else
    {
      super.addMessageEncoder(InsertMessage.class, InsertEncoder.class);
      super.addMessageEncoder(UpdateMessage.class, UpdateEncoder.class);
      super.addMessageEncoder(LookupMessage.class, LookupEncoder.class);
      
      super.addMessageDecoder(LookupResponseDecoder.class);
    }
  }
}
