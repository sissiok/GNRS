/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.junit.Assert;
import org.junit.Test;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;

/**
 * @author Robert Moore
 */
public class LookupEncoderTest {

  /**
   * Test method for
   * {@link edu.rutgers.winlab.mfirst.net.ipv4udp.LookupEncoder#encode(org.apache.mina.core.session.IoSession, edu.rutgers.winlab.mfirst.messages.LookupMessage, org.apache.mina.filter.codec.ProtocolEncoderOutput)}
   * .
   */
  @Test
  public void testEncode() {
    try {
      byte[] shouldBytes = new byte[] { 16, 1, 0, 42, 0, 0, 4, -46, 0, 0, 0, 6,
          1, 2, 3, 4, 19, -120, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
          14, 15, 16, 17, 18, 19, 0, 0, 0, 0 };

      DummySession session = new DummySession();
      ProtocolEncoderTest out = new ProtocolEncoderTest();
      LookupEncoder encoder = new LookupEncoder();
      LookupMessage message = new LookupMessage();

      GUID guid = new GUID();
      byte[] guidBytes = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7,
          0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x12, 0x13 };
      guid.setBinaryForm(guidBytes);
      message.setGuid(guid);
      message.setOptions(0l);
      IPv4UDPAddress origin = IPv4UDPAddress.fromASCII("1.2.3.4:5000");
      message.setOriginAddress(origin);
      message.setRequestId(1234);
      message.setVersion((byte) 0x10);

      encoder.encode(session, message, out);

      IoBuffer buff = (IoBuffer) out.next();
      byte[] bytes = buff.array();
      Assert.assertTrue(Arrays.equals(shouldBytes, bytes));

    } catch (UnsupportedEncodingException uee) {
      fail("Unable to decode ASCII");
    }
  }

}
