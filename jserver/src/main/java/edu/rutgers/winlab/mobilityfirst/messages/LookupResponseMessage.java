/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;

/**
 * A Lookup Response message for GNRS.
 * 
 * @author Robert Moore
 * 
 */
public class LookupResponseMessage extends AbstractMessage {
  /**
   * ResponseCode for this message.
   */
  private ResponseCode responseCode;

  /**
   * The bindings for this message.
   */
  private GUIDBinding[] bindings;

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
  public GUIDBinding[] getBindings() {
    return this.bindings;
  }

  /**
   * Sets the bindings for this message.
   * 
   * @param bindings
   *          the new bindings for this message.
   */
  public void setBindings(GUIDBinding[] bindings) {
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
}
