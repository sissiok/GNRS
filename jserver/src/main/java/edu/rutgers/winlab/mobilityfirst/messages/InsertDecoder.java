/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/**
 * @author Robert Moore
 *
 */
public class InsertDecoder implements MessageDecoder {

  /* (non-Javadoc)
   * @see org.apache.mina.filter.codec.demux.MessageDecoder#decodable(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer)
   */
  @Override
  public MessageDecoderResult decodable(IoSession arg0, IoBuffer arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.mina.filter.codec.demux.MessageDecoder#decode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput)
   */
  @Override
  public MessageDecoderResult decode(IoSession arg0, IoBuffer arg1,
      ProtocolDecoderOutput arg2) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.mina.filter.codec.demux.MessageDecoder#finishDecode(org.apache.mina.core.session.IoSession, org.apache.mina.filter.codec.ProtocolDecoderOutput)
   */
  @Override
  public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
      throws Exception {
    // TODO Auto-generated method stub

  }

}
