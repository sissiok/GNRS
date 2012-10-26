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

import edu.rutgers.winlab.mobilityfirst.structures.GUID;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class LookupDecoder implements MessageDecoder {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.demux.MessageDecoder#decodable(org.apache.
   * mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer)
   */
  @Override
  public MessageDecoderResult decodable(IoSession session, IoBuffer buffer) {
    // Store the current cursor position in the buffer
    buffer.mark();
    // Need 5 bytes to check request ID and type
    if (buffer.remaining() < 5) {
      return MessageDecoderResult.NEED_DATA;
    }

    // Skip the request ID
    buffer.getInt();
    byte type = buffer.get();
    // Reset the cursor so we don't modify the buffer data.
    buffer.reset();
    if (type == MessageType.LOOKUP.value()) {
      return MessageDecoderResult.OK;
    }
    return MessageDecoderResult.NOT_OK;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.demux.MessageDecoder#decode(org.apache.mina
   * .core.session.IoSession, org.apache.mina.core.buffer.IoBuffer,
   * org.apache.mina.filter.codec.ProtocolDecoderOutput)
   */
  @Override
  public MessageDecoderResult decode(IoSession session, IoBuffer buffer,
      ProtocolDecoderOutput out) throws Exception {
    /*
     * Common message header stuff
     */
    long requestId = buffer.getUnsignedInt();
    byte type = buffer.get();
    if (type != MessageType.LOOKUP.value()) {
      return MessageDecoderResult.NOT_OK;
    }

    LookupMessage msg = new LookupMessage();

    byte[] senderAddressBytes = new byte[NetworkAddress.SIZE_OF_NETWORK_ADDRESS];
    buffer.get(senderAddressBytes);
    NetworkAddress senderAddress = new NetworkAddress();
    senderAddress.setBytes(senderAddressBytes);

    long senderPort = buffer.getUnsignedInt();

    /*
     * LookupMessage-specific stuff now
     */
    byte[] guidBytes = new byte[GUID.SIZE_OF_GUID];
    buffer.get(guidBytes);
    GUID guid = new GUID();
    guid.setGuid(guidBytes);

    byte destinationFlag = buffer.get();

    msg.setRequestId(requestId);
    msg.setSenderAddress(senderAddress);
    msg.setSenderPort(senderPort);
    msg.setGuid(guid);
    msg.setDestinationFlag(destinationFlag);

    // Send the decoded message to the next filter
    out.write(msg);

    // Everything went better than expected!
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
    // Nothing to do. No state kept.
  }

}
