/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

/**
 * @author Robert Moore
 *
 */
public class InsertEncoder implements MessageEncoder<InsertMessage> {

  /* (non-Javadoc)
   * @see org.apache.mina.filter.codec.demux.MessageEncoder#encode(org.apache.mina.core.session.IoSession, java.lang.Object, org.apache.mina.filter.codec.ProtocolEncoderOutput)
   */
  @Override
  public void encode(IoSession arg0, InsertMessage arg1,
      ProtocolEncoderOutput arg2) throws Exception {
    // TODO Auto-generated method stub

  }

}
