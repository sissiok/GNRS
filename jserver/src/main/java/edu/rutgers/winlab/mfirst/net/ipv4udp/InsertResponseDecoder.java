/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import edu.rutgers.winlab.mfirst.messages.InsertResponseMessage;
import edu.rutgers.winlab.mfirst.messages.MessageType;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * Apache MINA message decoder for Insert Response messages.
 * 
 * @author Robert Moore
 * 
 */
public class InsertResponseDecoder implements MessageDecoder {

  @Override
  public MessageDecoderResult decodable(final IoSession session, final IoBuffer buffer) {
    // Store the current cursor position in the buffer
    buffer.mark();
    // Need 5 bytes to check request ID and type
    if (buffer.remaining() < 2) {
      return MessageDecoderResult.NEED_DATA;
    }

    // Skip the version number
    // TODO: What happens with version number?
    buffer.get();
    final byte type = buffer.get();
    // Reset the cursor so we don't modify the buffer data.
    buffer.reset();
    if (type == MessageType.INSERT_RESPONSE.value()) {
      return MessageDecoderResult.OK;
    }
    return MessageDecoderResult.NOT_OK;

  }

 
  @Override
  public MessageDecoderResult decode(final IoSession session, final IoBuffer buffer,
      final ProtocolDecoderOutput out) throws Exception {
    /*
     * Common message header stuff
     */
    // TODO: What to do with version?
    final byte version = buffer.get();
    final byte type = buffer.get();

    if (type != MessageType.INSERT_RESPONSE.value()) {
      return MessageDecoderResult.NOT_OK;
    }
    // Don't really care about message length
    buffer.getUnsignedShort();
    final long requestId = buffer.getUnsignedInt();
    
    final AddressType addrType = AddressType.valueOf(buffer.getUnsignedShort());
    
    final int originAddrLength = buffer.getUnsignedShort();
    final byte[] originAddr = new byte[originAddrLength];
    buffer.get(originAddr);
    final NetworkAddress originAddress = new NetworkAddress(addrType, originAddr);
    
    final InsertResponseMessage msg = new InsertResponseMessage();
    msg.setVersion(version);
    msg.setOriginAddress(originAddress);
    msg.setRequestId(requestId);

    // Response-specific stuff
    
    final int responseCode = buffer.getUnsignedShort();

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
  public void finishDecode(final IoSession arg0, final ProtocolDecoderOutput arg1)
      throws Exception {
    // Nothing to do
  }

}
