/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.structures;

/**
 * @author Robert Moore
 * 
 */
public class GUIDBinding {

  /**
   * The network address for this binding
   */
  private NetworkAddress address;

  /**
   * The TTL value for this binding.
   */
  private long ttl;

  /**
   * The weight or priority of this binding. Used to provide preference to
   * different bindings when a client requests.
   */
  private int weight;

  /**
   * Get the address that this binding holds.
   * 
   * @return the network address that this binding holds.
   */
  public NetworkAddress getAddress() {
    return this.address;
  }

  /**
   * Sets the new network address binding.
   * 
   * @param address
   *          the new network address for this binding.
   */
  public void setAddress(NetworkAddress address) {
    this.address = address;
  }

  /**
   * Time To Live value of this binding. Note that the wire protocol specifies
   * and unsigned 32-bit integer, but Java does not support unsigned types.
   * 
   * @return the TTL value.
   */
  public long getTtl() {
    return this.ttl;
  }

  /**
   * Sets the new TTL value for this binding. Note that the wire protocol
   * specifies and unsigned 32-bit integer, but Java does not support unsigned
   * types.
   * 
   * @param ttl
   *          the new TTL value.
   */
  public void setTtl(long ttl) {
    this.ttl = ttl;
  }

  /**
   * Weight or preference value for this binding. Note that the wire protocol
   * specifies an unsigned 16-bit value, but Java does not support unsigned
   * types.
   * 
   * @return the weight of this binding.
   */
  public int getWeight() {
    return this.weight;
  }

  /**
   * Sets the weight value for this binding. Note that the wire protocol
   * specifies an unsigned 16-bit value, but Java does not support unsigned
   * types.
   * 
   * @param weight
   *          the new weight value.
   */
  public void setWeight(int weight) {
    this.weight = weight;
  }
}