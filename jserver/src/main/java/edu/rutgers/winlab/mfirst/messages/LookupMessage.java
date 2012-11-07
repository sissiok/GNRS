/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import edu.rutgers.winlab.mfirst.structures.GUID;

/**
 * @author Robert Moore
 * 
 */
public class LookupMessage extends AbstractResponseMessage {
  /**
   * The GUID to look up.
   */
  private GUID guid;
  
  /**
   * Options for this lookup request.
   */
  private long options;
  /**
   * Creates a new Lookup message.
   */
  public LookupMessage() {
    super();
    super.type = MessageType.LOOKUP;
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
  
  

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("LKP #").append(this.getRequestId()).append(" (");
    sb.append(this.guid).append(")");
    return sb.toString();
  }

  /**
   * Gets the options flags for this message.
   * @return the options flags.
   */
  public long getOptions() {
    return this.options;
  }

  /**
   * Sets the options flags for this message.
   * @param options the new options flags.
   */
  public void setOptions(long options) {
    this.options = options & 0xFFFFFFFF;
  }

  
  @Override
  protected int getResponsePayloadLength() {
    // GUID, options
   return this.guid.getBinaryForm().length + 4;
  }
}
