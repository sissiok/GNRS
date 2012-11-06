/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import java.util.Map.Entry;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.Trie;

import edu.rutgers.winlab.mobilityfirst.storage.NetworkAddressKeyAnalyzer;
import edu.rutgers.winlab.mobilityfirst.structures.AddressType;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * Testing class for the Patricia Trie implementation.
 * 
 * @author Robert Moore
 * 
 */

public class TrieTest {

  /**
   * Hard-coded tests.
   * 
   * @param args
   *          ignored.
   */
  public static void main(String[] args) {
    Trie<NetworkAddress, String> testTrie = new PatriciaTrie<NetworkAddress, String>(
        NetworkAddressKeyAnalyzer.create(AddressType.INET_4_UDP));
    NetworkAddress na1 = NetworkAddress.ipv4FromInteger(1);

    NetworkAddress na2 = NetworkAddress.ipv4FromInteger(2);
    NetworkAddress na258 = NetworkAddress.ipv4FromInteger(258);

    testTrie.put(na1, "one");
    // testTrie.put(na2,"two");
    testTrie.put(na258, "two hundred fifty-eight");

    Entry<NetworkAddress, String> res = testTrie.select(na2);
    System.out.println(res.getValue());
  }

}
