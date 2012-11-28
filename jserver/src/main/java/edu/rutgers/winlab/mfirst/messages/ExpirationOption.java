/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

/**
 * @author Robert Moore
 *
 */
public class ExpirationOption extends Option {

  public static final byte TYPE = 0x02;
  
  public static final byte LENGTH = 0x08;
  
  private final transient Long timestamp;
  
  public ExpirationOption(final long timestamp){
    super(TYPE, LENGTH);
    this.timestamp = Long.valueOf(timestamp);
  }
  
 
  @Override
  public Object getOption() {
   return this.timestamp;
  }

  
  @Override
  public byte[] getBytes() {
    byte[] bytes = new byte[8];
    int index = 7;
    final long asLong = this.timestamp.longValue();
    for(int i = 0; i < 8; ++i, --index){
      bytes[i] = (byte)(asLong >>> (index*8));
    }
    return bytes;
  }

}
