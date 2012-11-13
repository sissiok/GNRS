/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;

/**
 * A message for inserting a GUID->NetworkAddress binding into the GNRS server.
 * 
 * @author Robert Moore
 * 
 */
public class InsertMessage extends AbstractMessage {
  /**
   * The GUID to insert.
   */
  private GUID guid;

  /**
   * The set of GUID&rarr;NetworkAddress bindings for this Insert message.
   */
  private NetworkAddress[] bindings;

  /**
   * Set of message options.
   */
  private long options;

  /**
   * Creates a new Insert message.
   */
  public InsertMessage() {
    super();
    super.type = MessageType.INSERT;
  }

  /**
   * Returns the GUID for this message.
   * 
   * @return the GUID for this message.
   */
  public GUID getGuid() {
    return this.guid;
  }

  /**
   * Sets the GUID for this message.
   * 
   * @param guid
   *          the new GUID for this message.
   */
  public void setGuid(GUID guid) {
    this.guid = guid;
  }

  /**
   * Gets the set of GUID&rarr;Network Address bindings for this message.
   * 
   * @return the Network Address bindings for this message.
   */
  public NetworkAddress[] getBindings() {
    return this.bindings;
  }
  
  /**
   * Returns the number of bindings in this message.
   * @return the number of bindings.
   */
  public long getNumBindings(){
    return this.bindings == null ? 0 : this.bindings.length;
  }

  /**
   * Sets the GUID&rarr;Network Address bindings for this message.
   * 
   * @param bindings
   *          the new Network Address bindings for this message.
   */
  public void setBindings(NetworkAddress[] bindings) {
    this.bindings = bindings;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("INS #");
    sb.append(this.getRequestId()).append(' ').append(this.guid)
        .append(" -> {");
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
  protected int getPayloadLength() {
    // GUID, Options, num bindings, bindings
    return this.guid.getBinaryForm().length + 8 + this.getBindingsLength();
  }

  /**
   * The length (in bytes) of the bindings contained in this message. Only to be
   * used for network encoding.
   * 
   * @return the length (in bytes) of the bindings when encoded for the network
   *         protocol.
   */
  protected int getBindingsLength() {
    int length = 0;
    if (this.bindings != null) {
      for (NetworkAddress addr : this.bindings) {
        length += (4 + addr.getLength());
      }
    }
    return length;
  }

  /**
   * Gets the message options.
   * 
   * @return the message options value.
   */
  public long getOptions() {
    return this.options;
  }

  /**
   * Sets the message options.
   * 
   * @param options
   *          the new message options value.
   */
  public void setOptions(long options) {
    this.options = options & 0xFFFFFFFF;
  }

}
