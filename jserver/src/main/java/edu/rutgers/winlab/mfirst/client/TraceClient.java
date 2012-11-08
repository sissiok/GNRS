/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mfirst.messages.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.MessageType;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;
import edu.rutgers.winlab.mfirst.structures.GUID;
import edu.rutgers.winlab.mfirst.structures.GUIDBinding;

/**
 * @author Robert Moore
 * 
 */
public class TraceClient extends IoHandlerAdapter {

  /**
   * Logging for this class.
   */
  static final Logger log = LoggerFactory.getLogger(TraceClient.class);

  /**
   * Sends messages to a server based on a trace file.
   * 
   * @param args
   */
  public static void main(String[] args) {
    if (args.length < 3) {
      printUsageInfo();
      return;
    }

    XStream x = new XStream();

    Configuration config = (Configuration) x.fromXML(new File(args[0]));
    log.debug("Loaded configuration file \"{}\".", args[0]);

    File traceFile = new File(args[1]);

    log.debug("Loaded trace file \"{}\".", traceFile);

    int delay = Integer.parseInt(args[2]);

    TraceClient client = new TraceClient(config, traceFile, delay);

    log.debug("Configured trace client.");
    client.connect();
    log.debug("Finished main thread.");
  }

  /**
   * Information about how to call the application from the commandline.
   */
  public static void printUsageInfo() {
    System.out.println("Usage: <Config File> <Trace File> <Delay Value>");
  }

  /**
   * Connector to communicate with the server.
   */
  NioDatagramConnector connector;
  /**
   * Configuration for the client.
   */
  private final Configuration config;
  /**
   * Trace file containing the messages to send.
   */
  private final File traceFile;
  /**
   * How long to wait between messages (microseconds).
   */
  private final int delay;

  /**
   * Creates a new client with the specified configuration file, trace file, and
   * intermessage delay (microsecond).
   * 
   * @param config
   *          the configuration file for the client.
   * @param traceFile
   *          the set of messages to send.
   * @param delay
   *          how long to pause between messages, in microseconds.
   */
  public TraceClient(final Configuration config, final File traceFile,
      final int delay) {
    super();

    this.config = config;
    this.traceFile = traceFile;
    this.delay = delay;

    this.connector = new NioDatagramConnector();
    this.connector.setHandler(this);
    DatagramSessionConfig sessionConfig = this.connector.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);
    DefaultIoFilterChainBuilder chain = this.connector.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(false)));

  }

  /**
   * Sets-up the necessary networking components so that communication with the
   * server can begin.
   * 
   * @return {@code true} if everything goes well, else {@code false}.
   */
  public boolean connect() {
    log.debug("Creating connect future.");
    ConnectFuture connectFuture = this.connector.connect(new InetSocketAddress(
        this.config.getServerHost(), this.config.getServerPort()));

    connectFuture.awaitUninterruptibly();

    connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
      @Override
      public void operationComplete(ConnectFuture future) {
        if (future.isConnected()) {
          TraceClient.log.info("Connected to {}", future.getSession());
          TraceClient.this.runTrace(future.getSession());
          future.getSession().close(true);
          TraceClient.this.connector.dispose(true);
        }
      }

    });

    log.debug("Future listener will handle connection event and start trace.");

    return true;
  }

  /**
   * Reads the trace file and sends the messages it contains.
   * 
   * @param session
   *          the connection to the server.
   */
  void runTrace(final IoSession session) {
    log.info("Starting trace from {}.", this.traceFile);
    BufferedReader reader = loadFile(this.traceFile);
    if (reader == null) {
      log.warn("Unable to open file reader. Aborting trace.");
      return;
    }
    String line = null;
    NetworkAddress fromAddress = null;
    try {
      fromAddress = IPv4UDPAddress.fromASCII(this.config.getClientHost()
          + ":" + this.config.getClientPort());
    } catch (UnsupportedEncodingException uee) {
      log.error(
          "Unable to parse local host name from configuration parameter.", uee);
      return;
    }

    int fromPort = this.config.getClientPort();
    try {
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0 || line.startsWith("#")) {
          continue;
        }

        log.debug("FILE: {}", line);
        AbstractMessage message = TraceClient.parseMessage(line);
        if (message == null) {
          log.warn("Unable to parse message from \"" + line + "\".");
          continue;
        }

        message.setOriginAddress(fromAddress);

        log.debug("Writing {} to {}", message, session);
        session.write(message);
        try {
          Thread.sleep(this.delay);
        } catch (InterruptedException ie) {
          // Ignored
        }

      }
      log.info("Finished reading trace file. Waiting 5 seconds.");
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ie) {
        // Ignored?
      }
      reader.close();
    } catch (IOException ioe) {
      log.error("Exception occurred while reading trace file.", ioe);
    }
  }

  /**
   * Parses a message from the trace file.
   * 
   * @param s
   *          a line from the trace file.
   * @return the parsed message, or {@code null} if none was parsed.
   */
  public static AbstractMessage parseMessage(final String s) {
    log.debug("Parsing \"{}\"", s);
    // Extract any comments and discard
    String line = s.split("#")[0];

    String[] generalComponents = line.split("\\s+");
    if (generalComponents.length < 3) {
      log.error("Not enough components to parse from the line {}.",
          Integer.valueOf(generalComponents.length));
      return null;
    }

    // Sequence number
    int sequenceNumber = Integer.parseInt(generalComponents[0]);
    // Type
    MessageType type = MessageType.parseType(generalComponents[1]);
    // GUID
    GUID guid = null;
    try {
      guid = GUID.fromASCII(generalComponents[2]);
    } catch (UnsupportedEncodingException uee) {
      log.error("Unable to parse GUID value from string.", uee);
      return null;
    }
    AbstractMessage msg = null;
    switch (type) {
    case INSERT: {
      // Make sure there is something to split
      if (generalComponents.length < 4) {
        log.error("Missing GUID binding value.");
        break;
      }

      String[] bindingValues = generalComponents[3].split(",");
      if (bindingValues.length % 3 != 0) {
        log.error("Binding values are not a multiple of 3: {}",
            Integer.valueOf(bindingValues.length));
        break;
      }
      NetworkAddress[] bindings = new NetworkAddress[bindingValues.length / 3];
      for (int i = 0; i < bindings.length; ++i) {
        NetworkAddress na = null;
        try {
          na = IPv4UDPAddress.fromASCII(bindingValues[0]);
        } catch (UnsupportedEncodingException uee) {
          log.error("Unable to parse network address from ASCII string.", uee);
          break;
        }
        long ttl = Long.parseLong(bindingValues[1]);
        int weight = Integer.parseInt(bindingValues[2]);
        bindings[i] = na;

      }

      InsertMessage insMsg = new InsertMessage();
      msg = insMsg;
      insMsg.setBindings(bindings);
      insMsg.setGuid(guid);
      insMsg.setRequestId(sequenceNumber);
      break;
    }
    case LOOKUP: {
      LookupMessage lookMsg = new LookupMessage();
      msg = lookMsg;
      lookMsg.setGuid(guid);
      lookMsg.setRequestId(sequenceNumber);
      break;
    }
    default:
      log.error("Unknown message type {}", type);
    }
    return msg;
  }

  /**
   * Wrapper to load files without throwing an exception.
   * 
   * @param file
   *          the file to load into a reader.
   * @return the BufferedReader for the loaded file, or {@code null} if an
   *         exception occurred.
   */
  private BufferedReader loadFile(final File file) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      return reader;
    } catch (Exception e) {
      log.error("Unable to load file for reading.", e);
      return null;
    }
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) {
    log.error("Caught unhandled exception.", cause);
  }

  @Override
  public void messageReceived(IoSession session, Object message) {
    log.debug("[{}] Received {}", session, message);
  }

  @Override
  public void messageSent(IoSession session, Object message) {
    log.debug("[{}] Sent {}", session, message);
  }
}
