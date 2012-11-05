/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.storage;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mobilityfirst.structures.GUID;
import edu.rutgers.winlab.mobilityfirst.structures.NetworkAddress;

/**
 * Hashes GUID values based on Java-supported hashing algorithms. Incorporates
 * "chaining" in order to generate enough network address values. More
 * specifically, the GUID value is the input to the first digest value. When all
 * bytes of that digest have been consumed, then the GUID value is fed back into
 * the algorithm along with the MD5 output of the first hash. That digest is
 * then used, and so on until enough network addresses have been generated.
 * 
 * Please refer to the Java documentation for a <a href=
 * "http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#MessageDigest"
 * >list of standard algorithms</a> available. Note that not all algorithms are
 * available on all JVM implementations.
 * 
 * @author Robert Moore
 * 
 * 
 */
public class MessageDigestHasher implements GUIDHasher {
  
  /**
   * Logging for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(MessageDigestHasher.class);

  /**
   * Name of the algorithm to use.
   */
  final String algorithmName;
  
  /**
   * Thread-specific set of message digest objects.
   */
  private ThreadLocal<MessageDigest> localDigest;

  /**
   * Creates a new message digest-based hashing object.
   * 
   * @param algorithmName
   *          the algorithm to use for hashing.
   */
  public MessageDigestHasher(final String algorithmName) {
    super();
    this.algorithmName = algorithmName.toUpperCase();
    // Create a new MessageDigest for each thread.
    this.localDigest = new ThreadLocal<MessageDigest> () {
      @Override
      public MessageDigest initialValue(){
        try {
          return MessageDigest.getInstance(MessageDigestHasher.this.algorithmName);
        }catch(NoSuchAlgorithmException nsae){
          return null;
        }
      }
    };
  }

  @Override
  public Collection<NetworkAddress> hash(GUID guid, int numAddresses)
      throws NoSuchAlgorithmException {
    ArrayList<NetworkAddress> addresses = new ArrayList<NetworkAddress>(
        numAddresses);
    MessageDigest digest = this.localDigest.get();
    if(digest == null){
      log.error("Unable to hash because \"{}\" is not supported.",this.algorithmName);
      return null;
    }
    int digestBytes = digest.getDigestLength();
    // Figure out how many bytes we need to buffer for our network addresses
    int numberDigests = (int) Math.ceil((NetworkAddress.SIZE_OF_NETWORK_ADDRESS
        * numAddresses * 1f)
        / digestBytes);
    ByteBuffer buffer = ByteBuffer.allocate(numberDigests * digestBytes);

    // Initializes to 0
    byte[] previousDigest = new byte[digestBytes];

    // Generate some bytes!
    for (int i = 0; i < numberDigests; ++i) {
      digest.update(guid.getBinaryForm());
      digest.update(previousDigest);
      previousDigest = digest.digest();
      buffer.put(previousDigest);
    }

    // Prepare the buffer for reading
    buffer.flip();

    // Generate the addresses and put them in the collection
    for (int i = 0; i < numAddresses; ++i) {
      NetworkAddress na = new NetworkAddress();
      byte[] bytes = new byte[NetworkAddress.SIZE_OF_NETWORK_ADDRESS];
      buffer.get(bytes);
      na.setBinaryForm(bytes);
      addresses.add(na);
    }
    
    return addresses;
  }

}
