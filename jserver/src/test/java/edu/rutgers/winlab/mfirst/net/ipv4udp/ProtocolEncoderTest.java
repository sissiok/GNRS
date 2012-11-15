/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * @author Robert Moore
 *
 */
public class ProtocolEncoderTest implements ProtocolEncoderOutput {

  private final Queue<Object> queue = new LinkedBlockingQueue<Object>();
  
  @Override
  public WriteFuture flush() {
    // TODO Auto-generated method stub
    return null;
  }

  
  @Override
  public void mergeAll() {
    // TODO Auto-generated method stub

  }

 
  @Override
  public void write(Object arg0) {
    this.queue.offer(arg0);
  }
  
  public Object next(){
    return this.queue.poll();
  }

}
