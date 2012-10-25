/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

/**
 * Configuration class for GNRS Server.
 * @author Robert Moore
 *
 */
public class Configuration {

  private int listenPort = 5001;
  private String bindIp = "";
  public int getListenPort() {
    return listenPort;
  }
  public void setListenPort(int listenPort) {
    this.listenPort = listenPort;
  }
  public String getBindIp() {
    return bindIp;
  }
  public void setBindIp(String bindIp) {
    this.bindIp = bindIp;
  }
  

}
