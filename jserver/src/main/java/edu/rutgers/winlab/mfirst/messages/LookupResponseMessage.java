/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * A Lookup Response message for GNRS.
 * 
 * @author Robert Moore
 * 
 */
public class LookupResponseMessage extends AbstractResponseMessage {
  /**
   * ResponseCode for this message.
   */
  private ResponseCode responseCode;

  /**
   * The bindings for this message.
   */
  private NetworkAddress[] bindings;

  /**
   * Creates a new Lookup Response message.
   */
  public LookupResponseMessage() {
    super();
    super.type = MessageType.LOOKUP_RESPONSE;
  }

  /**
   * Gets the ResponseCode for this message.
   * 
   * @return the ResponseCode for this message.
   */
  public ResponseCode getResponseCode() {
    return this.responseCode;
  }

  /**
   * Sets the ResponseCode for this message.
   * 
   * @param responseCode
   *          the new ResponseCode
   */
  public void setResponseCode(ResponseCode responseCode) {
    this.responseCode = responseCode;
  }

  /**
   * Gets the bindings for this message.
   * 
   * @return the bindings for this message, or {@code null} if there are none.
   */
  public NetworkAddress[] getBindings() {
    return this.bindings;
  }

  /**
   * Sets the bindings for this message.
   * 
   * @param bindings
   *          the new bindings for this message.
   */
  public void setBindings(NetworkAddress[] bindings) {
    this.bindings = bindings;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("LKR #").append(this.getRequestId())
        .append("/").append(this.responseCode).append(" {");
    if (this.bindings != null) {
      for (int i = 0; i < this.bindings.length; ++i) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(this.bindings[i]);
      }
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  protected int getResponsePayloadLength() {
    // Num bindings, bindings
    return 4 + this.getBindingLength();
    
  }

  protected int getBindingLength() {
    int length = 0;
    if (this.bindings != null) {
      for (NetworkAddress addx : this.bindings) {
        // Type, length, value
        length += (addx.getLength() + 4);
      }
    }
    return length;
  }
  
  /**
   * Returns the number of bindings in this message.
   * @return the number of bindings in this message.
   */
  public long getNumBindings(){
    return this.bindings == null ? 0 : this.bindings.length;
  }
}
