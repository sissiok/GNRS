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

import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * Apache MINA message encoder for {@link InsertMessage} objects.
 * 
 * @author Robert Moore
 * 
 */
public class InsertEncoder implements MessageEncoder<InsertMessage> {


  @Override
  public void encode(IoSession session, InsertMessage message,
      ProtocolEncoderOutput out) throws Exception {

    IoBuffer buff = IoBuffer.allocate(message.getMessageLength());
    
    // Generic request stuff
    buff.put(message.getVersion());
    buff.put(message.getType().value());
    buff.putUnsignedShort(message.getMessageLength());
    
    buff.putUnsignedInt(message.getRequestId());
    
    buff.putUnsignedShort(message.getOriginAddress().getType().value());
    buff.putUnsignedShort(message.getOriginAddress().getLength());
    buff.put(message.getOriginAddress().getValue());
    
    // Specific for Insert messages
    buff.put(message.getGuid().getBinaryForm());
    buff.putUnsignedInt(message.getOptions());
    buff.putUnsignedInt(message.getNumBindings());
    if(message.getNumBindings() > 0){
      for(NetworkAddress addx : message.getBindings()){
        buff.putUnsignedShort(addx.getType().value());
        buff.putUnsignedShort(addx.getLength());
        buff.put(addx.getValue());
      }
    }
    
    buff.flip();
    out.write(buff);
    

  }

}
