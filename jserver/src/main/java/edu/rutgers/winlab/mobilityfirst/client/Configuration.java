/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.client;

/**
 * @author Robert Moore
 *
 */
public class Configuration {

  private String serverHost = "localhost";
  
  private int serverPort = 5001;
  
  private int localPort = 4001;

  public String getServerHost() {
    return serverHost;
  }

  public void setServerHost(String serverHost) {
    this.serverHost = serverHost;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public int getLocalPort() {
    return localPort;
  }

  public void setLocalPort(int localPort) {
    this.localPort = localPort;
  }

}
