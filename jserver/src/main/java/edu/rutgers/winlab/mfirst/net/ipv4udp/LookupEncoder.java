/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.Option;

/**
 * Apache MINA message encoder for {@link LookupMessage} objects.
 * @author Robert Moore
 * 
 */
public class LookupEncoder implements MessageEncoder<LookupMessage> {

  @Override
  public void encode(final IoSession session, final LookupMessage message,
      final ProtocolEncoderOutput out)  {
    final IoBuffer buff = IoBuffer.allocate(message.getMessageLength());

    // Generic request stuff
    buff.put(message.getVersion());
    buff.put(message.getType().value());
    buff.putUnsignedShort(message.getMessageLength());

    buff.putUnsignedInt(message.getRequestId());
    
    // Offset values
    int optionsOffset = 0;
    // 12 + address T&L + address length
    int payloadOffset = 16 + message.getOriginAddress().getLength();

    if(!message.getOptions().isEmpty()){
      optionsOffset = payloadOffset + GUID.SIZE_OF_GUID;
    }
    
    buff.putUnsignedShort(optionsOffset);
    buff.putUnsignedShort(payloadOffset);
    
    buff.putUnsignedShort(message.getOriginAddress().getType().value());
    buff.putUnsignedShort(message.getOriginAddress().getLength());
    buff.put(message.getOriginAddress().getValue());
   
    // Lookup-specific stuff
    buff.put(message.getGuid().getBinaryForm());
    List<Option> options = message.getOptions();
    if(options != null && !options.isEmpty()){
      buff.put(RequestOptionsTranscoder.encode(options));
    }
   

    buff.flip();
    out.write(buff);

  }

}
