/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

/**
 * Response codes for response message.
 * 
 * @author Robert Moore
 * 
 */
public enum ResponseCode {
  /**
   * Indicates a successful action.
   */
  SUCCESS(0),
  /**
   * Indicates a general or unspecified error.
   */
  FAILED(1);
  
  /**
   * Used for toString() method.
   */
  private static final  String[] STRINGS = {"SUCCESS", "FAILED"};

  /**
   * The value of the response code for sending over the network.
   */
  private int value;

  /**
   * Creates a new ResponseCode.
   * 
   * @param value
   *          the value to send over the network.
   */
  private ResponseCode(final int value) {
    this.value = value;
  }

  /**
   * Gets this ResponseCode's value as a byte.
   * 
   * @return the byte value of this ResponseCode.
   */
  public int value() {
    return this.value;
  }
  
  /**
   * Returns a Responsecode based on the byte value.
   * @param b a byte value from the GNRS network protocol.
   * @return a ResponseCode appropriate for the value.
   */
  public static ResponseCode valueOf(int b){
    return b == SUCCESS.value ? SUCCESS : FAILED;
  }
  
  /**
   * Returns a String representation of this ResponseCode.
   */
  @Override
  public String toString(){
    return ResponseCode.STRINGS[this.value];
  }
}
