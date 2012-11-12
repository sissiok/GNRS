/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage.bdb;

import com.sleepycat.persist.model.Persistent;

import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * BerkeleyDB-compatible version of GUID binding.
 * 
 * @author Robert Moore
 * 
 */
@Persistent
public class BDBGUIDBinding {

  /**
   * The network address for this binding
   */
  public BDBNetworkAddress address;

  /**
   * The TTL value for this binding.
   */
  public long ttl;

  /**
   * The weight or priority of this binding. Used to provide preference to
   * different bindings when a client requests.
   */
  public int weight;

  public BDBGUIDBinding() {
    super();
  }

  public static BDBGUIDBinding fromGUIDBinding(final GUIDBinding binding) {

    BDBGUIDBinding returnBind = new BDBGUIDBinding();

    returnBind.address = BDBNetworkAddress.fromNetworkAddress(binding
        .getAddress());
    returnBind.ttl = binding.getTtl();
    returnBind.weight = binding.getWeight();

    return returnBind;
  }

  public GUIDBinding toGUIDBinding() {
    GUIDBinding binding = new GUIDBinding();
    binding.setAddress(this.address.toNetworkAddress());
    binding.setTtl(this.ttl);
    binding.setWeight(this.weight);
    return binding;
  }
}
