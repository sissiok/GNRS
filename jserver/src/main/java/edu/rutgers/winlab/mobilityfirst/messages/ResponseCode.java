/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

/**
 * @author Robert Moore
 * 
 */
public enum ResponseCode {
  /**
   * Indicates a successful action.
   */
  SUCCESS((byte) 0),
  /**
   * Indicates a general or unspecified error.
   */
  ERROR((byte) 1);
  
  /**
   * Used for toString() method.
   */
  private static final  String[] STRINGS = {"SUCCESS", "ERROR"};

  /**
   * The value of the response code for sending over the network.
   */
  private byte value;

  /**
   * Creates a new ResponseCode.
   * 
   * @param value
   *          the value to send over the network.
   */
  private ResponseCode(final byte value) {
    this.value = value;
  }

  /**
   * Gets this ResponseCode's value as a byte.
   * 
   * @return the byte value of this ResponseCode.
   */
  public byte value() {
    return this.value;
  }
  
  /**
   * Returns a Responsecode based on the byte value.
   * @param b a byte value from the GNRS network protocol.
   * @return a ResponseCode appropriate for the value.
   */
  public static ResponseCode valueOf(byte b){
    return b == SUCCESS.value ? SUCCESS : ERROR;
  }
  
  /**
   * Returns a String representation of this ResponseCode.
   */
  @Override
  public String toString(){
    return ResponseCode.STRINGS[this.value];
  }
}
