/*
 * Copyright (c) 2012, Rutgers University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
      final ProtocolEncoderOutput out) {
    
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
