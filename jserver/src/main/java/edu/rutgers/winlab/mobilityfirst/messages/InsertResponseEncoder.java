/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

/**
 * @author Robert Moore
 * 
 */
public class InsertResponseEncoder implements MessageEncoder<InsertResponseMessage> {

 
  @Override
  public void encode(IoSession session, InsertResponseMessage message,
      ProtocolEncoderOutput out) throws Exception {
    // Common Response stuff
    IoBuffer buffer = IoBuffer.allocate(message.getMessageLength());
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
    
    buffer.flip();
    out.write(buffer);
  }

}
