/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University.
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * @author Robert Moore
 * 
 */
public class GNRSServer extends Thread {

  /**
   * Logging facility for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(GNRSServer.class);

  /**
   * @param args
   *          <Configuration File>
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      printUsageInfo();
      return;
    }
    XStream x = new XStream();
    Configuration config = (Configuration) x.fromXML(new File(args[0]));
    try {
      GNRSServer server = new GNRSServer(config);
      server.start();
    } catch (IOException ioe) {
      log.error("Unable to start server.", ioe);
      return;
    }
  }

  /**
   * Prints out a helpful message to the command line. Let the user know how to
   * invoke.
   */
  public static void printUsageInfo() {
    System.out.println("Parameters: <Config file>");
  }

  /*
   * Class stuff below here.
   */

  private final Configuration config;

  /**
   * Creates a new GNRS server with the specified configuration. The server will
   * not start running until the {@code #start()} method is invoked.
   * 
   * @param config
   *          the configuration to use.
   */
  public GNRSServer(final Configuration config) throws IOException {
    this.config = config;

    NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
    acceptor.setHandler(new MessageHandler(this));

    DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

    DatagramSessionConfig sessionConfig = acceptor.getSessionConfig();
    sessionConfig.setReuseAddress(true);

    acceptor.bind(new InetSocketAddress(this.config.getListenPort()));

    log.info("Server listening on port {}.",
        Integer.valueOf(this.config.getListenPort()));
  }

}
