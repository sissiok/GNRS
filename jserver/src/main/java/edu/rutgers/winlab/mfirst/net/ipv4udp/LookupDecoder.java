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

import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.MessageType;
import edu.rutgers.winlab.mfirst.structures.AddressType;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class LookupDecoder implements MessageDecoder {

  
  @Override
  public MessageDecoderResult decodable(IoSession session, IoBuffer buffer) {
    // Store the current cursor position in the buffer
    buffer.mark();
    // Need 5 bytes to check request ID and type
    if (buffer.remaining() < 2) {
      return MessageDecoderResult.NEED_DATA;
    }

    // Skip the version
    // TODO: What to do with version?
    buffer.get();
    byte type = buffer.get();
    // Reset the cursor so we don't modify the buffer data.
    buffer.reset();
    if (type == MessageType.LOOKUP.value()) {
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
    byte version = buffer.get();
    byte type = buffer.get();
    int messageLength = buffer.getUnsignedShort();
    long requestId = buffer.getUnsignedInt();

    // Origin address
    AddressType addrType = AddressType.valueOf(buffer.getUnsignedShort());

    int originAddrLength = buffer.getUnsignedShort();
    byte[] originAddr = new byte[originAddrLength];
    buffer.get(originAddr);
    NetworkAddress originAddress = new NetworkAddress(addrType, originAddr);

    LookupMessage msg = new LookupMessage();
    msg.setVersion(version);
    msg.setRequestId(requestId);
    msg.setOriginAddress(originAddress);
    
    // Lookup-specific stuff
    byte[] guidBytes = new byte[GUID.SIZE_OF_GUID];
    buffer.get(guidBytes);
    GUID queryGUID = new GUID();
    queryGUID.setBinaryForm(guidBytes);
    long options = buffer.getUnsignedInt();
    msg.setGuid(queryGUID);
    msg.setOptions(options);

    // Send the decoded message to the next filter
    out.write(msg);

    // Everything went better than expected!
    return MessageDecoderResult.OK;

  }

  @Override
  public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
      throws Exception {
    // Nothing to do. No state kept.
  }

}
