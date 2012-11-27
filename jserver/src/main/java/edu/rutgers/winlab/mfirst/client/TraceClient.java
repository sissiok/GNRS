/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
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
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.GUID;
import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.MessageType;
import edu.rutgers.winlab.mfirst.messages.RecursiveRequestOption;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.ipv4udp.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mfirst.net.ipv4udp.IPv4UDPAddress;

/**
 * A simple GNRS client that sends GNRS messages based on a trace file.
 * 
 * @author Robert Moore
 */
public class TraceClient extends IoHandlerAdapter {

  /**
   * Logging for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TraceClient.class);

  /**
   * Sends messages to a server based on a trace file.
   * 
   * @param args
   */
  public static void main(final String[] args) {
    if (args.length < 3) {
      printUsageInfo();
      return;
    }

    final XStream xStream = new XStream();

    final Configuration config = (Configuration) xStream.fromXML(new File(
        args[0]));
    LOG.debug("Loaded configuration file \"{}\".", args[0]);

    final File traceFile = new File(args[1]);

    LOG.debug("Loaded trace file \"{}\".", traceFile);

    final int delay = Integer.parseInt(args[2]);

    final TraceClient client = new TraceClient(config, traceFile, delay);

    LOG.debug("Configured trace client.");
    client.connect();
    LOG.debug("Finished main thread.");
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
  private final transient NioDatagramConnector connector;
  
  private final transient NioDatagramAcceptor acceptor;
  /**
   * Configuration for the client.
   */
  private final transient Configuration config;
  /**
   * Trace file containing the messages to send.
   */
  private final transient File traceFile;
  /**
   * How long to wait between messages (microseconds).
   */
  private final transient int delay;

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


    this.acceptor = new NioDatagramAcceptor();
    this.acceptor.setHandler(this);
    DatagramSessionConfig sessionConfig = this.acceptor.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);
    DefaultIoFilterChainBuilder chain = this.acceptor.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory()));
    
    this.connector = new NioDatagramConnector();
    this.connector.setHandler(this);
    sessionConfig = this.connector
        .getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);
    chain = this.connector.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory()));

  }

  /**
   * Sets-up the necessary networking components so that communication with the
   * server can begin.
   * 
   * @return {@code true} if everything goes well, else {@code false}.
   */
  public boolean connect() {
    boolean retValue = true;
    try {
  
    this.acceptor.bind(new InetSocketAddress(this.config.getClientPort()));
    
    LOG.debug("Creating connect future.");
    final ConnectFuture connectFuture = this.connector
        .connect(new InetSocketAddress(this.config.getServerHost(), this.config
            .getServerPort()));

    connectFuture.awaitUninterruptibly();

    connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
      @Override
      public void operationComplete(final ConnectFuture future) {
        if (future.isConnected()) {

          TraceClient.this.runTrace(future.getSession());

        }
      }

    });

    LOG.debug("Future listener will handle connection event and start trace.");
    } catch (IOException e) {
      LOG.error("Unable to bind to local port.", e);
      retValue = false;
    }

    return retValue;
  }

  /**
   * Reads the trace file and sends the messages it contains.
   * 
   * @param session
   *          the connection to the server.
   */
  public void runTrace(final IoSession session) {
    LOG.info("Connected to {}", session);
    LOG.info("Starting trace from {}.", this.traceFile);

    String line = null;
    NetworkAddress fromAddress = null;
    try {
      final BufferedReader reader = new BufferedReader(new FileReader(
          this.traceFile));
      fromAddress = IPv4UDPAddress.fromASCII(this.config.getClientHost() + ":"
          + this.config.getClientPort());

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0 || line.charAt(0) == '#') {
          continue;
        }

        LOG.debug("FILE: {}", line);
        final AbstractMessage message = TraceClient.parseMessage(line);
        if (message == null) {
          LOG.warn("Unable to parse message from \"" + line + "\".");
          continue;
        }

        message.setOriginAddress(fromAddress);
        message.finalizeOptions();

        session.write(message);
//        try {
          java.util.concurrent.locks.LockSupport.parkNanos(this.delay*1000l);
//          Thread.sleep(this.delay);
//        } catch (final InterruptedException ie) {
          // Ignored
//        }

      }
      LOG.info("Finished reading trace file. Waiting 5 seconds.");
      try {
        Thread.sleep(5000);
      } catch (final InterruptedException ie) {
        // Ignored?
      }
      reader.close();
    } catch (final UnsupportedEncodingException uee) {
      LOG.error(
          "Unable to parse local host name from configuration parameter.", uee);
      return;
    } catch (final IOException ioe) {
      LOG.error("Exception occurred while reading trace file.", ioe);

    }
    session.close(true);
    this.connector.dispose(true);
    this.acceptor.dispose(true);
  }

  /**
   * Parses a message from the trace file.
   * 
   * @param asString
   *          a line from the trace file.
   * @return the parsed message, or {@code null} if none was parsed.
   */
  public static AbstractMessage parseMessage(final String asString) {
    LOG.debug("Parsing \"{}\"", asString);
    // Extract any comments and discard
    final String line = asString.split("#")[0];
    AbstractMessage msg = null;

    final String[] generalComponents = line.split("\\s+");
    if (generalComponents.length >= 3) {

      // Sequence number
      final int sequenceNumber = Integer.parseInt(generalComponents[0]);
      // Type
      final MessageType type = MessageType.parseType(generalComponents[1]);
      // GUID
      GUID guid = null;
      try {
        guid = GUID.fromASCII(generalComponents[2]);

        switch (type) {
        case INSERT: {
          msg = parseInsertMessage(guid, sequenceNumber, generalComponents);
          break;
        }
        case LOOKUP: {
          final LookupMessage lookMsg = new LookupMessage();
          lookMsg.addOption(new RecursiveRequestOption(true));
          msg = lookMsg;
          lookMsg.setGuid(guid);
          lookMsg.setRequestId(sequenceNumber);
          break;
        }
        default:
          LOG.error("Unknown message type {}", type);
          break;
        }
      } catch (final UnsupportedEncodingException uee) {
        LOG.error("Unable to parse GUID value from string.", uee);
      }
    } else {
      LOG.error("Not enough components to parse from the line {}.",
          Integer.valueOf(generalComponents.length));
    }
    return msg;
  }

  /**
   * Parses an Insert message from a trace file line.
   * 
   * @param guid
   *          the GUID for the message.
   * @param sequenceNumber
   *          the sequence number for the message
   * @param generalComponents
   *          the split line from the file.
   * @return an Insert Message parsed from the line, or {@code null} if parsing
   *         failed.
   */
  private static InsertMessage parseInsertMessage(final GUID guid,
      final int sequenceNumber, final String[] generalComponents) {
    InsertMessage msg = null;
    // Make sure there is something to split
    if (generalComponents.length < 4) {
      LOG.error("Missing GUID binding value.");

    } else {

      final String[] bindingValues = generalComponents[3].split(",");
      if (bindingValues.length % 3 != 0) {
        LOG.error("Binding values are not a multiple of 3: {}",
            Integer.valueOf(bindingValues.length));
      }
      final NetworkAddress[] bindings = new NetworkAddress[bindingValues.length / 3];
      for (int i = 0; i < bindings.length; ++i) {
        NetworkAddress netAddr = null;
        try {
          netAddr = IPv4UDPAddress.fromASCII(bindingValues[0]);
        } catch (final UnsupportedEncodingException uee) {
          LOG.error("Unable to parse network address from ASCII string.", uee);
          break;
        }

        bindings[i] = netAddr;

      }

      final InsertMessage insMsg = new InsertMessage();
      msg = insMsg;
      insMsg.setBindings(bindings);
      insMsg.setGuid(guid);
      insMsg.setRequestId(sequenceNumber);
      insMsg.addOption(new RecursiveRequestOption(true));
    }
    return msg;
  }

  @Override
  public void exceptionCaught(final IoSession session, final Throwable cause) {
    LOG.error("Caught unhandled exception.", cause);
  }

  @Override
  public void messageReceived(final IoSession session, final Object message) {
    LOG.debug("[{}] Received {}", session, message);
  }

  @Override
  public void messageSent(final IoSession session, final Object message) {
    LOG.debug("[{}] Sent {}", session, message);
  }
}
