/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
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

import edu.rutgers.winlab.mfirst.messages.ExpirationOption;
import edu.rutgers.winlab.mfirst.messages.Option;
import edu.rutgers.winlab.mfirst.messages.RecursiveRequestOption;
import edu.rutgers.winlab.mfirst.messages.TTLOption;

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
        case TTLOption.TYPE:
          long ttl = optionsBuffer.getLong();
          lastOption = new TTLOption(ttl);
          break;
        case ExpirationOption.TYPE:
          long timestamp = optionsBuffer.getLong();
          lastOption = new ExpirationOption(timestamp);
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
