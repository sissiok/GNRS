/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

/**
 * A generic interface for message options to implement.
 * 
 * @author Robert Moore
 */
public abstract class Option {

  private static final byte FINAL_FLAG = (byte) 0x80;
  public static final transient byte USER_TYPES_FLAG = (byte) 0x7F;

  private transient byte type;
  private final transient byte length;

  protected Option(final byte type, final byte length) {
    super();
    this.type = (byte) (type & USER_TYPES_FLAG);
    this.length = length;
  }

  public byte getType() {
    return this.type;
  }

  public byte getLength() {
    return this.length;
  }

  public abstract Object getOption();
  
  public abstract byte[] getBytes();

  public boolean isFinal() {
    return (this.type & FINAL_FLAG) == FINAL_FLAG;
  }

  public void setFinal() {
    this.type |= FINAL_FLAG;
  }
  
  public static boolean isFinal(final byte type){
    return (type & FINAL_FLAG) == FINAL_FLAG;
  }
}
