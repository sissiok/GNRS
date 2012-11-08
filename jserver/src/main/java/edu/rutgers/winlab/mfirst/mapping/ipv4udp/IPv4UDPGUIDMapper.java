/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.mapping.ipv4udp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.mapping.GUIDMapper;
import edu.rutgers.winlab.mfirst.net.AddressType;
import edu.rutgers.winlab.mfirst.net.NetworkAccessObject;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.mapping.ipv4udp.Configuration;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;
import edu.rutgers.winlab.mfirst.storage.NetworkAddressMapper;
import edu.rutgers.winlab.mfirst.structures.GUID;

/**
 * @author Robert Moore
 * 
 */
public class IPv4UDPGUIDMapper implements GUIDMapper {

  /**
   * Logging for this class.
   */
  private static final Logger log = LoggerFactory
      .getLogger(IPv4UDPGUIDMapper.class);

  /**
   * Mapping network address prefixes to AS/addresses.
   */
  public final NetworkAddressMapper networkAddressMap = new NetworkAddressMapper(
      AddressType.INET_4_UDP);

  /**
   * Mapping of Autonomous System numbers to their network locations.
   */
  public final ConcurrentHashMap<Integer, InetSocketAddress> asAddresses = new ConcurrentHashMap<Integer, InetSocketAddress>();

  public IPv4UDPGUIDMapper(final String configFile) throws IOException {
    Configuration config = this.loadConfiguration(configFile);

      // Load the network prefix announcements
      this.loadPrefixes(config.getPrefixFile());

      // Load the AS network address binding values
      this.loadAsNetworkBindings(config.getAsBindingFile());
   
  }

  /**
   * Loads this Mapper's configuration file from the filename provided.
   * 
   * @param filename
   *          the name of the configuration file.
   * @return the configuration object
   */
  private Configuration loadConfiguration(final String filename) {
    XStream x = new XStream();
    return (Configuration) x.fromXML(new File(filename));
  }

  /**
   * Loads network prefix mappings from a file.
   * 
   * @param prefixFilename
   *          the filename of the prefix mapping file.
   * @throws IOException
   *           if an exception occurs while reading the file.
   */
  private void loadPrefixes(final String prefixFilename) throws IOException {
    File prefixFile = new File(prefixFilename);
    BufferedReader lineReader = new BufferedReader(new FileReader(prefixFile));

    String line = lineReader.readLine();
    while (line != null) {
      // Eliminate leading/trailing whitespace
      line = line.trim();
      // Skip comments
      if (line.length() == 0 || line.startsWith("#")) {
        line = lineReader.readLine();
        continue;
      }

      // log.debug("Parsing \"{}\"", line);
      // Extract any comments and discard
      String content = line.split("#")[0];

      String[] generalComponents = content.split("\\s+");
      if (generalComponents.length < 2) {
        log.warn("Not enough components to parse the line \"{}\".", line);
        continue;
      }
      // Extract the base address and prefix length
      String[] prefixParts = generalComponents[0].split("/");
      InetAddress addx = InetAddress.getByName(prefixParts[0]);
      byte[] addxBytes = addx.getAddress();
      // FIXME: Assuming IPv4 (4 bytes)
      int addxAsInt = (addxBytes[0] << 24) | (addxBytes[1] << 16)
          | (addxBytes[2] << 8) | (addxBytes[3]);
      // Extract prefix length
      int prefixLength = Integer.parseInt(prefixParts[1]);
      // Apply the prefix
      addxAsInt = addxAsInt & (0x80000000 >> prefixLength);

      // FIXME: Still bound to IPv4
      NetworkAddress na = IPv4UDPAddress.fromInteger(addxAsInt);
      this.networkAddressMap.put(na, generalComponents[1]);

      line = lineReader.readLine();
    }
    lineReader.close();
    log.info("Finished loading prefix map.");

  }

  private void loadAsNetworkBindings(final String asBindingFilename)
      throws IOException {
    File asBindingFile = new File(asBindingFilename);
    BufferedReader lineReader = new BufferedReader(
        new FileReader(asBindingFile));

    String line = lineReader.readLine();
    while (line != null) {
      // Eliminate leading/trailing whitespace
      line = line.trim();
      // Skip comments
      if (line.length() == 0 || line.startsWith("#")) {
        line = lineReader.readLine();
        continue;
      }

      // log.debug("Parsing \"{}\"", line);
      // Extract any comments and discard
      String content = line.split("#")[0];

      // Extract the 3 parts (AS #, IP address, port)
      String[] generalComponents = content.split("\\s+");
      if (generalComponents.length < 3) {
        log.warn("Not enough components to parse the line \"{}\".", line);
        continue;
      }

      Integer asNumber = Integer.valueOf(generalComponents[0]);
      String ipAddrString = generalComponents[1];
      int port = Integer.parseInt(generalComponents[2]);

      InetSocketAddress sockAddx = new InetSocketAddress(ipAddrString, port);
      this.asAddresses.put(asNumber, sockAddx);

      line = lineReader.readLine();
    }
    lineReader.close();
    log.info("Finished loading AS network binding values.");

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * edu.rutgers.winlab.mfirst.mapping.GUIDMapper#getMapping(edu.rutgers.winlab
   * .mfirst.structures.GUID, edu.rutgers.winlab.mfirst.net.AddressType[])
   */
  @Override
  public Collection<NetworkAddress> getMapping(final GUID guid,
      final int numAddresses, final AddressType... types) {
    
    
    
    // FIXME Must do tonight!
    return null;
  }

  
  @Override
  public EnumSet<AddressType> getTypes() {
    return EnumSet.of(AddressType.INET_4_UDP); 
  }

 
  @Override
  public AddressType getDefaultAddressType() {
    return AddressType.INET_4_UDP;
  }

}
