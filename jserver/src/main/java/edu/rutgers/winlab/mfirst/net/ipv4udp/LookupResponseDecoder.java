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

import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.MessageType;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * Apache MINA message decoder for Lookup Response messages.
 * 
 * @author Robert Moore
 * 
 */
public class LookupResponseDecoder implements MessageDecoder {

  @Override
  public MessageDecoderResult decodable(final IoSession session, final IoBuffer buffer) {
    // Store the current cursor position in the buffer
    buffer.mark();
    // Need 2 bytes to check version and type
    if (buffer.remaining() < 2) {
      return MessageDecoderResult.NEED_DATA;
    }

    // Skip the version field
    // TODO: What to do with versions?
    buffer.get();
    final byte type = buffer.get();
    // Reset the cursor so we don't modify the buffer data.
    buffer.reset();
    if (type == MessageType.LOOKUP_RESPONSE.value()) {
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
    buffer.get();

    // Already checked message type in decodable(IoSession, IoBuffer)
    buffer.get();

    // Don't need message length
    buffer.getUnsignedShort();
    final long requestId = buffer.getUnsignedInt();

    // Origin Address
    final AddressType addrType = AddressType.valueOf(buffer.getUnsignedShort());

    final int originAddrLength = buffer.getUnsignedShort();
    final byte[] originAddr = new byte[originAddrLength];
    buffer.get(originAddr);
    final NetworkAddress originAddress = new NetworkAddress(addrType, originAddr);

    // Response code
    final ResponseCode responseCode = ResponseCode.valueOf(buffer.getUnsignedShort());
    // Padding
    buffer.getShort();

    final LookupResponseMessage msg = new LookupResponseMessage();
    msg.setOriginAddress(originAddress);
    msg.setRequestId(requestId);
    msg.setResponseCode(responseCode);

    // Lookup response-specific

    // Lookup response-specific stuff
    final long numBindings = buffer.getUnsignedInt();
    final NetworkAddress[] bindings = new NetworkAddress[(int) numBindings];

    for (int i = 0; i < numBindings; ++i) {
      final int addxType = buffer.getUnsignedShort();
      final int addxLength = buffer.getUnsignedShort();
      final byte[] addxBytes = new byte[addxLength];
      buffer.get(addxBytes);

      final NetworkAddress na = new NetworkAddress(AddressType.valueOf(addxType),
          addxBytes);
      bindings[i] = na;
    }
    msg.setBindings(bindings);

    // Write decoded message to next filter.
    out.write(msg);

    // Everything went well
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
    // Nothing to see here.
  }

}
