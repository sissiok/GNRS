/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import edu.rutgers.winlab.mobilityfirst.structures.GUID;
import edu.rutgers.winlab.mobilityfirst.structures.GUIDBinding;

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
   * Indicates whether or not the final destination of this insert message has
   * been computed. If the value is 1, then the destination is computed; 0 means
   * it has not yet been computed.
   */
  private byte destinationFlag;
  /**
   * The set of GUID&rarr;NetworkAddress bindings for this Insert message.
   */
  private GUIDBinding[] bindings;

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
   * Gets the destination flag for this message. A flag value of 0 denotes that
   * the destination of the GUID binding has not yet been computed; a value of 1
   * indicates that it has. Note that the wire protocol specifies an 8-bit
   * unsigned integer, but Java does not support unsigned types.
   * 
   * @return the destination flag for this message.
   */
  public byte getDestinationFlag() {
    return this.destinationFlag;
  }

  /**
   * Sets the destination flag value for this message. Note that the wire
   * protocol specifies an 8-bit unsigned integer, but Java does not support
   * unsigned types.
   * 
   * @param destinationFlag
   *          the new destination flag value for this message.
   */
  public void setDestinationFlag(byte destinationFlag) {
    this.destinationFlag = destinationFlag;
  }

  /**
   * Gets the set of GUID&rarr;Network Address bindings for this message.
   * 
   * @return the Network Address bindings for this message.
   */
  public GUIDBinding[] getBindings() {
    return this.bindings;
  }

  /**
   * Sets the GUID&rarr;Network Address bindings for this message.
   * 
   * @param bindings
   *          the new Network Address bindings for this message.
   */
  public void setBindings(GUIDBinding[] bindings) {
    this.bindings = bindings;
  }

}
