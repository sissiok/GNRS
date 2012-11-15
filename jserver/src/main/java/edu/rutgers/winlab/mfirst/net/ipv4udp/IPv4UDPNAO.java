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

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
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
public class IPv4UDPNAO extends IoHandlerAdapter implements NetworkAccessObject {

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
  private final transient Map<IoSession, IPv4UDPParameters> sessionMap = new ConcurrentHashMap<IoSession, IPv4UDPParameters>();

  /**
   * Incoming datagram acceptor.
   */
  private final transient NioDatagramAcceptor acceptor;

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
    if (this.config == null){
      throw new IllegalArgumentException("Unable to load configuration file \""
          + configFilename + "\".");
    }
    this.acceptor = new NioDatagramAcceptor();
    this.acceptor.setHandler(this);

    final DefaultIoFilterChainBuilder chain = this.acceptor.getFilterChain();
    // For encoding/decoding our messages
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(true)));

    final DatagramSessionConfig sessionConfig = this.acceptor.getSessionConfig();
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
    if (!this.config.isAsynchronousWrite()) {
      future.awaitUninterruptibly();
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
    // FIXME: Remote servers
    return true;
  }

  @Override
  public NetworkAddress getOriginAddress() {
    try {
      return IPv4UDPAddress.fromASCII(this.config.getBindAddress() + ":"
          + this.config.getBindPort());
    } catch (final UnsupportedEncodingException e) {
      LOG.error("Unable to create origin address due to encoding problems.", e);
      return null;
    }

  }

  /*
   * MINA stuff
   */

  @Override
  public void messageReceived(final IoSession session, final Object message) {
    IPv4UDPParameters params = this.sessionMap.get(session);
    if (params == null) {
      params = new IPv4UDPParameters();
      params.session = session;
      this.sessionMap.put(session, params);
    }
    for (final MessageListener listener : this.listeners) {
      listener.messageReceived(params, (AbstractMessage) message);
    }
  }

  @Override
  public void sessionOpened(final IoSession session) {
    final IPv4UDPParameters params = new IPv4UDPParameters();
    params.session = session;
    this.sessionMap.put(session, params);
  }

  @Override
  public void sessionClosed(final IoSession session) {
    this.sessionMap.remove(session);
  }

  @Override
  public void exceptionCaught(final IoSession session, final Throwable cause) {
    LOG.error("Exception caught.", cause);
  }

  @Override
  public void doShutdown() {
    this.acceptor.dispose(true);
  }

}
