/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * @author Robert Moore
 *
 */
public class ProtocolDecoderTest implements ProtocolDecoderOutput {

  private final Queue<Object> messages = new LinkedBlockingQueue<Object>();
  
  @Override
  public void flush(NextFilter arg0, IoSession arg1) {
    // Nothing to do
  }

  
  @Override
  public void write(Object arg0) {
    this.messages.offer(arg0);
  }

  public Object nextObject(){
    return this.messages.poll();
  }
}
