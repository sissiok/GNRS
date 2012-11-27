/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.messages;

/**
 * Message option for recursive requests.
 * 
 * @author Robert Moore
 */
public class RecursiveRequestOption extends Option {

  /**
   * Type value for this option.
   */
  public static final byte TYPE = 0x01;

  /**
   * Length for this option.
   */
  public static final byte LENGTH = 0x02;

  private static final byte[][] BYTES = new byte[][] { { 0, 0 }, { 0, 1 } };

  /**
   * Flag to indicate recursion.
   */
  private final transient boolean recursive;

  /**
   * Creates a new option with the specified recursion value.
   * 
   * @param recursive
   *          {@code true} if a recursion is requested, else {@code false} for a
   *          non-recursive request.
   */
  public RecursiveRequestOption(final boolean recursive) {
    super(TYPE, LENGTH);
    this.recursive = recursive;
  }

  /**
   * A Boolean object indicating whether the message should be processed in a
   * recursive manner (from a client) or not (from another server).
   */
  @Override
  public Object getOption() {
    return Boolean.valueOf(this.recursive);
  }

  @Override
  public byte[] getBytes() {
    return this.recursive ? BYTES[1] : BYTES[0];
  }

  /**
   * Returns whether or not this message is recursive or not.
   * 
   * @return {@code true} if the message is recursive, else {@code false}.
   */
  public boolean isRecursive() {
    return this.recursive;
  }

}
