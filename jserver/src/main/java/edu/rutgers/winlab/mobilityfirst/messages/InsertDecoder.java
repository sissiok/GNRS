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
import edu.rutgers.winlab.mobilityfirst.structures.GUID;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * @author Robert Moore
 * 
 */
public class InsertDecoder implements MessageDecoder {

  @Override
  public MessageDecoderResult decodable(IoSession session, IoBuffer buffer) {
    // Store the current cursor position in the buffer
    buffer.mark();
    // Need 2 bytes to check version and type
    if (buffer.remaining() < 2) {
      buffer.reset();
      return MessageDecoderResult.NEED_DATA;
    }

    // Skip the version number
    // TODO: What to do with the version?
    buffer.get();
    byte type = buffer.get();
    // Reset the cursor so we don't modify the buffer data.
    buffer.reset();
    if (type == MessageType.INSERT.value()) {
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

    InsertMessage msg = new InsertMessage();
    msg.setVersion(version);
    msg.setRequestId(requestId);
    msg.setOriginAddress(originAddress);

    // Insert-specific stuff
    GUID guid = new GUID();
    byte[] guidBytes = new byte[GUID.SIZE_OF_GUID];
    buffer.get(guidBytes);
    guid.setBinaryForm(guidBytes);
    msg.setGuid(guid);

    long options = buffer.getUnsignedInt();
    msg.setOptions(options);

    long numBindings = buffer.getUnsignedInt();
    NetworkAddress[] bindings = new NetworkAddress[(int)numBindings];

    for (int i = 0; i < numBindings; ++i) {
      int addxType = buffer.getUnsignedShort();
      int addxLength = buffer.getUnsignedShort();
      byte[] addxBytes = new byte[addxLength];
      buffer.get(addxBytes);

      NetworkAddress na = new NetworkAddress(AddressType.valueOf(addxType),
          addxBytes);
      bindings[i] = na;
    }
    msg.setBindings(bindings);

    // Send the decoded message to the next filter
    out.write(msg);

    // Everything OK!
    return MessageDecoderResult.OK;
  }

  
  @Override
  public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
      throws Exception {
    // Nothing to do
  }

}
