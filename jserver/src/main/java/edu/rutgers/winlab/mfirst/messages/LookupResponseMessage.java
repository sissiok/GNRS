/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * A Lookup Response message for GNRS.
 * 
 * @author Robert Moore
 * 
 */
public class LookupResponseMessage extends AbstractResponseMessage {

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
  public void setBindings(final NetworkAddress[] bindings) {
    this.bindings = bindings;
  }

  @Override
  public String toString() {
    final StringBuilder sBuff = new StringBuilder("LKR #").append(this.getRequestId())
        .append("/").append(this.responseCode).append(" {");
    if (this.bindings != null) {
      for (int i = 0; i < this.bindings.length; ++i) {
        if (i > 0) {
          sBuff.append(", ");
        }
        sBuff.append(this.bindings[i]);
      }
    }
    sBuff.append("}");
    return sBuff.toString();
  }

  @Override
  protected int getResponsePayloadLength() {
    // Num bindings, bindings
    return 4 + this.getBindingsLength();

  }

  /**
   * The length of the bindings in this message. Used to compute the message
   * length.
   * 
   * @return the length, in bytes, of the bindings in this message.
   */
  protected int getBindingsLength() {
    int length = 0;
    if (this.bindings != null) {
      for (final NetworkAddress addx : this.bindings) {
        // Type, length, value
        length += (addx.getLength() + 4);
      }
    }
    return length;
  }

  /**
   * Returns the number of bindings in this message.
   * 
   * @return the number of bindings in this message.
   */
  public long getNumBindings() {
    return this.bindings == null ? 0 : this.bindings.length;
  }
}
