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

  private String clientHost = "localhost";

  private int clientPort = 4001;

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

  public int getClientPort() {
    return this.clientPort;
  }

  public void setClientPort(int clientPort) {
    this.clientPort = clientPort;
  }

  public String getClientHost() {
    return this.clientHost;
  }

  public void setClientHost(String clientHost) {
    this.clientHost = clientHost;
  }

}
