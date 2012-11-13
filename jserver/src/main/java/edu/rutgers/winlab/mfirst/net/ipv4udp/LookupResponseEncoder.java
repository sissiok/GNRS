/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * Apache MINA message encoder for {@link LookupResponseMessage} objects.
 * 
 * @author Robert Moore
 * 
 */
public class LookupResponseEncoder implements
    MessageEncoder<LookupResponseMessage> {

  
  @Override
  public void encode(final IoSession session, final LookupResponseMessage message,
      final ProtocolEncoderOutput out) throws Exception {
    
    // Common Response stuff
    final IoBuffer buffer = IoBuffer.allocate(message.getMessageLength());
    buffer.put(message.getVersion());
    buffer.put(message.getType().value());
    buffer.putUnsignedShort(message.getMessageLength());
    buffer.putUnsignedInt(message.getRequestId());
    
    // Address
    buffer.putUnsignedShort(message.getOriginAddress().getType().value());
    buffer.putUnsignedShort(message.getOriginAddress().getLength());
    buffer.put(message.getOriginAddress().getValue());
    
    buffer.putUnsignedShort(message.getResponseCode().value());
    // Padding
    buffer.putUnsignedShort(0);
    
    // Lookup response-specific
    buffer.putUnsignedInt(message.getNumBindings());
    if(message.getNumBindings() > 0){
      for(final NetworkAddress addx : message.getBindings()){
        buffer.putUnsignedShort(addx.getType().value());
        buffer.putUnsignedShort(addx.getLength());
        buffer.put(addx.getValue());
      }
    }
    
    buffer.flip();
    out.write(buffer);
    
  }

}
