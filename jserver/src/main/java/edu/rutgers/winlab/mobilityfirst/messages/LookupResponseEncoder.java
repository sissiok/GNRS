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

import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;

/**
 * @author Robert Moore
 * 
 */
public class LookupResponseEncoder implements
    MessageEncoder<LookupResponseMessage> {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.demux.MessageEncoder#encode(org.apache.mina
   * .core.session.IoSession, java.lang.Object,
   * org.apache.mina.filter.codec.ProtocolEncoderOutput)
   */
  @Override
  public void encode(IoSession session, LookupResponseMessage message,
      ProtocolEncoderOutput out) throws Exception {
    // Common message stuff
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt((int) message.getRequestId());
    dos.writeByte(message.getType().value());
    dos.write(message.getSenderAddress().getBinaryForm());
    dos.writeInt((int) message.getSenderPort());

    // LookupResponseMessage-specific
    dos.writeByte(message.getResponseCode().value());
    // Careful, perhaps there were no bindings.
    if (message.getBindings() != null) {
      dos.writeShort(message.getBindings().length);
      for (GUIDBinding binding : message.getBindings()) {
        dos.write(binding.getAddress().getBinaryForm());
        dos.writeInt((int) binding.getTtl());
        dos.writeShort(binding.getWeight());
      }
    } else {
      dos.writeShort(0);
    }

    dos.flush();
    out.write(IoBuffer.wrap(baos.toByteArray()));
    dos.close();
  }

}
