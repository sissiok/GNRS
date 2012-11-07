/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import edu.rutgers.winlab.mfirst.net.ipv4udp.InsertDecoder;
import edu.rutgers.winlab.mfirst.net.ipv4udp.InsertEncoder;
import edu.rutgers.winlab.mfirst.net.ipv4udp.InsertResponseDecoder;
import edu.rutgers.winlab.mfirst.net.ipv4udp.InsertResponseEncoder;
import edu.rutgers.winlab.mfirst.net.ipv4udp.LookupDecoder;
import edu.rutgers.winlab.mfirst.net.ipv4udp.LookupEncoder;
import edu.rutgers.winlab.mfirst.net.ipv4udp.LookupResponseDecoder;
import edu.rutgers.winlab.mfirst.net.ipv4udp.LookupResponseEncoder;

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
      super.addMessageEncoder(InsertResponseMessage.class, InsertResponseEncoder.class);
    } else {
      super.addMessageEncoder(InsertMessage.class, InsertEncoder.class);
      super.addMessageEncoder(LookupMessage.class, LookupEncoder.class);

      super.addMessageDecoder(LookupResponseDecoder.class);
      super.addMessageDecoder(InsertResponseDecoder.class);
    }
  }
}
