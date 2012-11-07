/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.client;

/**
 * @author Robert Moore
 * 
 */
public class Configuration {

  /**
   * Server hostname.
   */
  private String serverHost = "localhost";

  /**
   * Server port.
   */
  private int serverPort = 5001;

  /**
   * Client hostname (locally).
   */
  private String clientHost = "localhost";

  /**
   * Client sending port.
   */
  // FIXME: Make sure this is used
  private int clientPort = 4001;

  /**
   * Returns the server hostname for this configuration.
   * 
   * @return the server hostname.
   */
  public String getServerHost() {
    return this.serverHost;
  }

  /**
   * Sets the server hostname for this configuration.
   * 
   * @param serverHost
   *          the new server hostname.
   */
  public void setServerHost(String serverHost) {
    this.serverHost = serverHost;
  }

  /**
   * Gets the server port number.
   * 
   * @return the server port.
   */
  public int getServerPort() {
    return this.serverPort;
  }

  /**
   * Sets the server port.
   * 
   * @param serverPort
   *          the new server port number.
   */
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  /**
   * Gets the client sending port.
   * 
   * @return the client sending port number.
   */
  public int getClientPort() {
    return this.clientPort;
  }

  /**
   * Sets the client sending port.
   * 
   * @param clientPort
   *          the new sending port.
   */
  public void setClientPort(int clientPort) {
    this.clientPort = clientPort;
  }

  /**
   * Gets the client sending hostname.
   * 
   * @return the client hostname.
   */
  public String getClientHost() {
    return this.clientHost;
  }

  /**
   * Sets the client sending hostname.
   * 
   * @param clientHost
   *          the new client hostname.
   */
  public void setClientHost(String clientHost) {
    this.clientHost = clientHost;
  }

}
