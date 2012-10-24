/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * @author Robert Moore
 *
 */
public class GNRSServer {
  
  /**
   * Logging facility for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(GNRSServer.class);

  /**
   * @param args <Configuration File>
   */
  public static void main(String[] args) {
    if(args.length < 1){
      printUsageInfo();
      return;
    }
  }
  
  public static void printUsageInfo(){
    System.out.println("Parameters: <Config file>");
  }

}
