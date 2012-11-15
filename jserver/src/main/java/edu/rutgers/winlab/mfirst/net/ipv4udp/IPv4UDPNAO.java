/*
 * Mobility First GNRS Server Copyright (C) 2012 Robert Moore and Rutgers
 * University All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.client.GeneratingClient;
import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mfirst.messages.AbstractResponseMessage;
import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.MessageType;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.net.MessageListener;
import edu.rutgers.winlab.mfirst.net.NetworkAccessObject;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;

/**
 * Network Access Object (NAO) implementation for IPv4/UDP GNRS networking
 * implementations.
 * 
 * @author Robert Moore
 */
public class IPv4UDPNAO extends IoHandlerAdapter implements
    NetworkAccessObject, IoFutureListener<ConnectFuture> {

  /**
   * Logging for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(IPv4UDPNAO.class);

  /**
   * Set of listeners for this NAO.
   */
  private final transient Set<MessageListener> listeners = new ConcurrentHashSet<MessageListener>();

  /**
   * Configuration options for this NAO.
   */
  private final transient Configuration config;

  /**
   * SessionParameter objects for each IoSession.
   */
  private final transient Map<IoSession, IPv4UDPParameters> inSessionMap = new ConcurrentHashMap<IoSession, IPv4UDPParameters>();

  private final transient Map<NetworkAddress, IPv4UDPParameters> incomingConnections = new ConcurrentHashMap<NetworkAddress, IPv4UDPParameters>();

  /**
   * Messages that are awaiting connections.
   */
  private final transient Map<ConnectFuture, RelayInfo> awaitingConnect = new ConcurrentHashMap<ConnectFuture, RelayInfo>();

  /**
   * Existing outbound connections for network addresses.
   */
  private final transient Map<NetworkAddress, IPv4UDPParameters> outgoingConnections = new ConcurrentHashMap<NetworkAddress, IPv4UDPParameters>();

  private final transient Map<Integer, RelayInfo> awaitingResponse = new ConcurrentHashMap<Integer, RelayInfo>();

  /**
   * Incoming datagram acceptor.
   */
  private final transient NioDatagramAcceptor acceptor;

  /**
   * For sending datagrams.
   */
  private final transient NioDatagramConnector connector;

  private final transient IPv4UDPAddress listenAddress;

  private final transient IPv4UDPAddress sendAddress;

  private final transient InetSocketAddress sendSockAddr;

  private final transient AtomicInteger nextRequestId = new AtomicInteger(
      (int) System.currentTimeMillis());

  /**
   * Creates a new instance of network access object for IPv4/UDP networking.
   * 
   * @param configFilename
   *          name of configuration file for this NAO.
   * @throws IOException
   *           if an IOException occurs while binding to the listen port.
   */
  public IPv4UDPNAO(final String configFilename) throws IOException {
    super();
    this.config = this.loadConfiguration(configFilename);
    if (this.config == null) {
      throw new IllegalArgumentException("Unable to load configuration file \""
          + configFilename + "\".");
    }
    this.listenAddress = IPv4UDPAddress.fromASCII(this.config.getBindAddress()
        + ":" + this.config.getBindPort());

    this.sendAddress = IPv4UDPAddress.fromASCII(this.config.getBindAddress()
        + ":" + this.config.getSendPort());
    this.sendSockAddr = IPv4UDPAddress.toSocketAddr(this.sendAddress);

    // Incoming messages (and direct replies)
    this.acceptor = new NioDatagramAcceptor();
    this.configureAcceptor();
    // Outgoing messages
    this.connector = new NioDatagramConnector();
    this.configureConnector();

  }

  private void configureAcceptor() throws IOException {

    this.acceptor.setHandler(this);

    final DefaultIoFilterChainBuilder chain = this.acceptor.getFilterChain();
    // For encoding/decoding our messages
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(true)));

    final DatagramSessionConfig sessionConfig = this.acceptor
        .getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);

    // Bind to wildcard (all) interface
    if (this.config.getBindAddress() == null
        || this.config.getBindAddress().trim().length() == 0) {
      this.acceptor.bind(new InetSocketAddress(this.config.getBindPort()));
    }
    // Bind to a specific IP address.
    else {
      this.acceptor.bind(new InetSocketAddress(this.config.getBindAddress(),
          this.config.getBindPort()));
    }
  }

  /**
   * Sets up the outgoing messages connector.
   * 
   * @throws IOException
   */
  private void configureConnector() throws IOException {
    this.connector.setHandler(this);
    final DatagramSessionConfig sessionConfig = this.connector
        .getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);
    final DefaultIoFilterChainBuilder chain = this.connector.getFilterChain();
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(false)));
  }

  /**
   * Loads this NAOs configuration file from the filename provided.
   * 
   * @param filename
   *          the name of the configuration file.
   * @return the loaded configuration.
   */
  private Configuration loadConfiguration(final String filename) {
    final XStream xStream = new XStream();
    return (Configuration) xStream.fromXML(new File(filename));
  }

  @Override
  public void addMessageListener(final MessageListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void sendMessage(final SessionParameters parameters,
      final AbstractMessage message) {

    if (!(parameters instanceof IPv4UDPParameters)) {
      throw new IllegalArgumentException(
          "Not an instance of IPv4UDP networking parameters: " + parameters);
    }

    final IPv4UDPParameters params = (IPv4UDPParameters) parameters;
    final WriteFuture future = params.session.write(message);
    LOG.info("Awaiting actual write of {} to {}", message, params.session);
    if (!this.config.isAsynchronousWrite()) {
      future.awaitUninterruptibly();
    }
    LOG.info("Wrote {} to {}", message, params.session);
  }

  @Override
  public void sendMessage(final AbstractMessage message,
      final NetworkAddress... destAddrs) {
    LOG.info("Sending {} to {}", message, destAddrs);
    for (NetworkAddress destAddr : destAddrs) {
      IPv4UDPParameters params = this.outgoingConnections.get(destAddr);
      if (params == null) {
        LOG.info("Establishing connection to {}", IPv4UDPAddress.toSocketAddr(destAddr));
        final ConnectFuture future = this.connector.connect(
            IPv4UDPAddress.toSocketAddr(destAddr), this.sendSockAddr);
        RelayInfo info = new RelayInfo();
        info.clientMessage = message;
        info.serverAddress = destAddr;
        this.awaitingConnect.put(future, info);
        future.addListener(this);
        // FIXME: Must call awaitUninterruptably. This is a known issue in MINA
        // <https://issues.apache.org/jira/browse/DIRMINA-911>
        future.awaitUninterruptibly();
      }
      // Have an existing connection
      else {
        int requestId = this.nextRequestId.getAndIncrement();
        LOG.info("Using requestID {}", requestId);
        RelayInfo info = new RelayInfo();
        info.clientMessage = message;
        info.serverAddress = destAddr;
        info.serverParams = params;
        info.clientParams = this.outgoingConnections.get(message
            .getOriginAddress());
        MessageType type = message.getType();

        switch (type) {
        case INSERT: {
          InsertMessage clientMessage = (InsertMessage) message;
          InsertMessage sentMessage = new InsertMessage();
          sentMessage.setBindings(clientMessage.getBindings());
          sentMessage.setGuid(clientMessage.getGuid());
          sentMessage.setOptions(clientMessage.getOptions());
          sentMessage.setOriginAddress(this.listenAddress);
          sentMessage.setRequestId(requestId);
          sentMessage.setVersion((byte) 0);
          this.awaitingResponse.put(Integer.valueOf(requestId), info);
          this.sendMessage(params, sentMessage);

        }
          break;
        case INSERT_RESPONSE:
          break;
        case LOOKUP: {
          LookupMessage clientMessage = (LookupMessage) message;
          LookupMessage sentMessage = new LookupMessage();
          sentMessage.setRecursive(false);
          sentMessage.setGuid(clientMessage.getGuid());
          sentMessage.setOriginAddress(this.listenAddress);
          sentMessage.setVersion((byte) 0);
          sentMessage.setRequestId(requestId);
          this.awaitingResponse.put(Integer.valueOf(requestId), info);
          this.sendMessage(params, sentMessage);
        }
          break;
        case LOOKUP_RESPONSE:
          break;
        case UPDATE:
          break;
        case UPDATE_RESPONSE:
          break;
        case UNKNOWN:
          break;
        }
      }
    }

  }

  @Override
  public void endSession(final SessionParameters parameters) {
    if (!(parameters instanceof IPv4UDPParameters)) {
      throw new IllegalArgumentException(
          "Not an instance of IPv4UDP networking parameters: " + parameters);
    }
    final IPv4UDPParameters params = (IPv4UDPParameters) parameters;
    params.session.close(true);
  }

  @Override
  public void removeMessageListener(final MessageListener listener) {
    this.listeners.remove(listener);
  }

  @Override
  public boolean isLocal(final NetworkAddress netAddr) {

    return this.listenAddress.equals(netAddr);
  }

  @Override
  public NetworkAddress getOriginAddress() {
    return this.listenAddress;
  }

  /*
   * MINA stuff
   */

  @Override
  public void messageReceived(final IoSession session, final Object message) {
    if (message instanceof AbstractResponseMessage) {
      this.handleResponse(session, (AbstractResponseMessage) message);
    } else {
      LOG.info("Received {} from {}", message, session);
      IPv4UDPParameters params = this.inSessionMap.get(session);
      if (params == null) {
        params = new IPv4UDPParameters();
        params.session = session;
        this.inSessionMap.put(session, params);
        if (message instanceof AbstractMessage) {
          this.incomingConnections.put(
              ((AbstractMessage) message).getOriginAddress(), params);
        }

      }

      for (final MessageListener listener : this.listeners) {
        listener.messageReceived(params, (AbstractMessage) message);
      }
    }
  }

  private void handleResponse(final IoSession session,
      final AbstractResponseMessage respMsg) {
    Integer reqId = Integer.valueOf((int) respMsg.getRequestId());
    RelayInfo info = this.awaitingResponse.get(reqId);
    // We are actually expecting this response
    if (info != null) {
      // This is a server we need a response from
      if (info.remainingServers.remove(respMsg.getOriginAddress())) {
        // Add the bindings (if any)
        if (respMsg instanceof LookupResponseMessage) {
          LookupResponseMessage lrm = (LookupResponseMessage) respMsg;
          for (NetworkAddress netAddr : lrm.getBindings()) {
            info.responseAddresses.add(netAddr);
          }
        }
        // If this was the last server, reply to the client
        if (info.remainingServers.isEmpty()) {
          this.awaitingResponse.remove(reqId);

          if (info.clientMessage instanceof LookupMessage) {
            LookupResponseMessage lrm = new LookupResponseMessage();
            lrm.setRequestId(info.clientMessage.getRequestId());
            lrm.setOriginAddress(this.listenAddress);
            lrm.setResponseCode(ResponseCode.SUCCESS);
            lrm.setVersion((byte) 0x0);
            lrm.setBindings(info.responseAddresses
                .toArray(new NetworkAddress[] {}));
            this.sendMessage(info.clientParams, lrm);
          } else if (info.clientMessage instanceof InsertMessage) {
            LOG.error("Insert not implemented");
          } else {
            LOG.error("Unsupported message received?");
          }
        }
      }
    }
  }

  @Override
  public void sessionOpened(final IoSession session) {
    final IPv4UDPParameters params = new IPv4UDPParameters();
    params.session = session;
    this.inSessionMap.put(session, params);
  }

  @Override
  public void sessionClosed(final IoSession session) {
    this.inSessionMap.remove(session);
  }

  @Override
  public void exceptionCaught(final IoSession session, final Throwable cause) {
    LOG.error("Exception caught.", cause);
  }

  @Override
  public void doShutdown() {
    this.acceptor.dispose(true);
  }

  @Override
  public void operationComplete(ConnectFuture future) {
    RelayInfo info = this.awaitingConnect.remove(future);
    if (info == null) {
      LOG.error("Unable to find correct message for {}", future);
    } else {
      IPv4UDPParameters params = new IPv4UDPParameters();
      params.session = future.getSession();
      info.serverParams = params;
      this.outgoingConnections.put(info.serverAddress, info.serverParams);
      this.sendMessage(info.clientMessage, info.serverAddress);
    }
  }

}
