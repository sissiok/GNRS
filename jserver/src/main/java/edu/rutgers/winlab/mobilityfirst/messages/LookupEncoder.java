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
public class LookupEncoder implements MessageEncoder<LookupMessage> {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.demux.MessageEncoder#encode(org.apache.mina
   * .core.session.IoSession, java.lang.Object,
   * org.apache.mina.filter.codec.ProtocolEncoderOutput)
   */
  @Override
  public void encode(IoSession session, LookupMessage message,
      ProtocolEncoderOutput out) throws Exception {
    // Common message stuff
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt((int) message.getRequestId());
    dos.writeByte(message.getType().value());
    dos.write(message.getSenderAddress().getBinaryForm());
    dos.writeInt((int) message.getSenderPort());

    // LookupMessage-specific
    dos.write(message.getGuid().getBinaryForm());
    dos.writeByte(message.getDestinationFlag());

    dos.flush();
    out.write(IoBuffer.wrap(baos.toByteArray()));
    dos.close();
  }

}
