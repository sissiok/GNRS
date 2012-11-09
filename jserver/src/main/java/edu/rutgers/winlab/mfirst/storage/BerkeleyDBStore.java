/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.storage;

import edu.rutgers.winlab.mfirst.structures.GNRSRecord;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * A GUID storage engine using Berkeley DB as an in-memory and persistet
 * (on-disk) storage engine.
 * 
 * @author Robert Moore
 * 
 */
public class BerkeleyDBStore implements GUIDStore {

  public BerkeleyDBStore(){
    
  }
  
  @Override
  public GNRSRecord getBinding(GUID guid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Boolean insertBinding(GUID guid, GUIDBinding binding) {
    // TODO Auto-generated method stub
    return null;
  }

}
