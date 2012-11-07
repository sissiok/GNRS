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

import edu.rutgers.winlab.mobilityfirst.structures.AddressType;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class InsertResponseDecoder implements MessageDecoder {

  @Override
  public MessageDecoderResult decodable(IoSession session, IoBuffer buffer) {
    // Store the current cursor position in the buffer
    buffer.mark();
    // Need 5 bytes to check request ID and type
    if (buffer.remaining() < 2) {
      return MessageDecoderResult.NEED_DATA;
    }

    // Skip the version number
    // TODO: What happens with version number?
    buffer.get();
    byte type = buffer.get();
    // Reset the cursor so we don't modify the buffer data.
    buffer.reset();
    if (type == MessageType.INSERT_RESPONSE.value()) {
      return MessageDecoderResult.OK;
    }
    return MessageDecoderResult.NOT_OK;

  }

 
  @Override
  public MessageDecoderResult decode(IoSession session, IoBuffer buffer,
      ProtocolDecoderOutput out) throws Exception {
    /*
     * Common message header stuff
     */
    // TODO: What to do with version?
    byte version = buffer.get();
    byte type = buffer.get();

    if (type != MessageType.INSERT_RESPONSE.value()) {
      return MessageDecoderResult.NOT_OK;
    }
    // Don't really care about message length
    int msgLength = buffer.getUnsignedShort();
    long requestId = buffer.getUnsignedInt();
    
    AddressType addrType = AddressType.valueOf(buffer.getUnsignedShort());
    
    int originAddrLength = buffer.getUnsignedShort();
    byte[] originAddr = new byte[originAddrLength];
    buffer.get(originAddr);
    NetworkAddress originAddress = new NetworkAddress(addrType, originAddr);
    
    InsertResponseMessage msg = new InsertResponseMessage();
    msg.setVersion(version);
    msg.setOriginAddress(originAddress);
    msg.setRequestId(requestId);

    // Response-specific stuff
    
    int responseCode = buffer.getUnsignedShort();

    msg.setResponseCode(ResponseCode.valueOf(responseCode));
    
    // Remove unused padding
    buffer.getUnsignedShort();

    // Write the decoded object to the next filter
    out.write(msg);

    // Everything OK!
    return MessageDecoderResult.OK;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.demux.MessageDecoder#finishDecode(org.apache
   * .mina.core.session.IoSession,
   * org.apache.mina.filter.codec.ProtocolDecoderOutput)
   */
  @Override
  public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
      throws Exception {
    // Nothing to do
  }

}
