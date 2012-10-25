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
  private NetworkAddress address;
  private long ttl;
  private int weight;
  public NetworkAddress getAddress() {
    return address;
  }
  public void setAddress(NetworkAddress address) {
    this.address = address;
  }
  public long getTtl() {
    return ttl;
  }
  public void setTtl(int ttl) {
    this.ttl = ttl;
  }
  public int getWeight() {
    return weight;
  }
  public void setWeight(int weight) {
    this.weight = weight;
  }
}
