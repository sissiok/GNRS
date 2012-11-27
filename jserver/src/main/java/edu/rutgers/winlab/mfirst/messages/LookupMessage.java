/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

import edu.rutgers.winlab.mfirst.GUID;

/**
 * A Lookup/Retrieval for a GUID value from the GNRS system.
 * <p>
 * Lookup/Retrieval messages specify a query GUID value to a GNRS server. The
 * response message, a {@link LookupResponseMessage}, is sent with the current
 * binding values (if present) back to the requester.
 * </p>
 * 
 * @author Robert Moore
 */
public class LookupMessage extends AbstractMessage {
  /**
   * The GUID to look up.
   */
  private GUID guid;

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
  public void setGuid(final GUID guid) {
    this.guid = guid;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder("LKP");
   
    buffer.append(" #").append(this.getRequestId()).append(" (");
    buffer.append(this.guid).append(")");
    return buffer.toString();
  }

  @Override
  protected int getPayloadLength() {
    // GUID, options
    int length = 4;
    if (this.guid != null && this.guid.getBinaryForm() != null) {
      length += this.guid.getBinaryForm().length;
    }
    return length;
  }

}
