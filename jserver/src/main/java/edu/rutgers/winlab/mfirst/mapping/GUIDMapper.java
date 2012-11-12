/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.mapping;

import java.util.Collection;
import java.util.EnumSet;

import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.structures.GUID;

/**
 * 
 * @author Robert Moore
 * 
 */
public interface GUIDMapper {

  /**
   * Returns the network-appropriate NetworkAddresses for the specified GUID.
   * The mapper should make a "best effort" to resolve as many of different
   * AddressTypes provided as possible. If no AddressType values are provided,
   * then the mapper should return a default address type (specified in
   * #getDefaultAddressType()).
   * 
   * @param guid
   *          the GUID to map
   * @param numAddresses
   *          the number of addresses of each type to map.
   * @param types
   *          the set of requested network address types to return mappings for.
   *          If specified, then all returned results should be one of these
   *          types. If not specified ({@code null}), then the type should be
   *          the default type for this mapper.
   * @return the mappings for the GUID according to the specified types and the
   *         available mapping implementations provided by the mapper.
   */
  public Collection<NetworkAddress> getMapping(GUID guid, int numAddresses,
      AddressType... types);

  /**
   * The set of AddressType values supported by this mapper.
   * 
   * @return the supported AddressTypes for this GUID mapper.
   */
  public EnumSet<AddressType> getTypes();

  /**
   * The default AddressType for the mapper. When no AddressType is provided to
   * {@link #getMapping(GUID, int, AddressType...)}, then this type will be
   * generated.
   * 
   * @return the default AddressType for this mapper.
   */
  public AddressType getDefaultAddressType();
}
