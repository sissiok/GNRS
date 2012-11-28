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
public class TTLOption extends Option {

  public static final byte TYPE = 0x03;
  
  public static final byte LENGTH = 0x08;
  
  private final transient Long ttl;
  
  public TTLOption(final long ttl){
    super(TYPE, LENGTH);
    this.ttl = Long.valueOf(ttl);
  }
  
 
  @Override
  public Object getOption() {
   return this.ttl;
  }

  
  @Override
  public byte[] getBytes() {
    byte[] bytes = new byte[8];
    int index = 7;
    final long asLong = this.ttl.longValue();
    for(int i = 0; i < 8; ++i, --index){
      bytes[i] = (byte)(asLong >>> (index*8));
    }
    return bytes;
  }

}
