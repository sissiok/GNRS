/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 *
 */
public class InsertEncoder implements MessageEncoder<InsertMessage> {

  /* (non-Javadoc)
   * @see org.apache.mina.filter.codec.demux.MessageEncoder#encode(org.apache.mina.core.session.IoSession, java.lang.Object, org.apache.mina.filter.codec.ProtocolEncoderOutput)
   */
  @Override
  public void encode(IoSession session, InsertMessage message,
      ProtocolEncoderOutput out) throws Exception {
    
    // Common message stuff
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt((int)message.getRequestId());
    dos.writeByte(message.getType().value());
    dos.write(message.getSenderAddress().getBytes());
    dos.writeInt(message.getSenderPort());
    
    // InsertMessage-specific
    dos.write(message.getGuid().getGuid());
    dos.writeByte(message.getDestinationFlag());
    dos.writeShort(message.getBindings().length);
    
    for(GUIDBinding binding : message.getBindings()){
      dos.write(binding.getAddress().getBytes());
      dos.writeInt((int)binding.getTtl());
      dos.writeShort(binding.getWeight());
    }
    dos.flush();
    out.write(baos.toByteArray());
    dos.close();
    
  }

}
