/*
 * Copyright (c) 2012, Rutgers University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.opt.ExpirationOption;
import edu.rutgers.winlab.mfirst.messages.opt.Option;
import edu.rutgers.winlab.mfirst.messages.opt.RecursiveRequestOption;
import edu.rutgers.winlab.mfirst.messages.opt.TTLOption;

/**
 * @author Robert Moore
 */
public class RequestOptionsTranscoder {
  private static final Logger LOG = LoggerFactory
      .getLogger(RequestOptionsTranscoder.class);

  public static IoBuffer encode(List<Option> options) {
    try {
      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      DataOutputStream dao = new DataOutputStream(bao);
      for (Option option : options) {
        dao.write(option.getType());
        dao.write(option.getLength());
        dao.write(option.getBytes());

      }

      dao.flush();
      IoBuffer returnedBuff = IoBuffer.wrap(bao.toByteArray());
      return returnedBuff;
    } catch (IOException ioe) {
      LOG.error("Unable to encode options.", ioe);
      return null;
    }
  }

  public static List<Option> decode(IoBuffer optionsBuffer) {
    Option lastOption = null;
    List<Option> options = new LinkedList<Option>();
    if (optionsBuffer.remaining() > 0) {
      byte type = 0;
      byte length = 0;
      do {
        type = optionsBuffer.get();
        length = optionsBuffer.get();
        boolean isFinal = Option.isFinal(type);
        type &= Option.USER_TYPES_FLAG;

        switch (type) {
        case RecursiveRequestOption.TYPE:
          boolean recursive = (optionsBuffer.getUnsignedShort() != 0);
          lastOption = new RecursiveRequestOption(recursive);

          break;
        case TTLOption.TYPE: {
          long[] ttls = new long[length / 8];
          for (int i = 0; i < ttls.length; ++i) {
            ttls[i] = optionsBuffer.getLong();
          }
          lastOption = new TTLOption(ttls);
        }
          break;
        case ExpirationOption.TYPE: {
          long[] expirations = new long[length / 8];
          for (int i = 0; i < expirations.length; ++i) {
            expirations[i] = optionsBuffer.getLong();
          }
          lastOption = new ExpirationOption(expirations);
        }
          break;
        default:
          LOG.info("Unsupported options type {}", type);
          lastOption = null;
          break;
        }
        if (isFinal) {
          lastOption.setFinal();
        }
        options.add(lastOption);
      } while (lastOption != null && !lastOption.isFinal());
    }
    return options;
  }
}
