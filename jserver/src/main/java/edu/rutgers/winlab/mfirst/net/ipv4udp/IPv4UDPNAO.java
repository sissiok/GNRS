/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mfirst.net.ipv4udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
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

import edu.rutgers.winlab.mfirst.messages.AbstractMessage;
import edu.rutgers.winlab.mfirst.messages.GNRSProtocolCodecFactory;
import edu.rutgers.winlab.mfirst.net.MessageListener;
import edu.rutgers.winlab.mfirst.net.NetworkAccessObject;
import edu.rutgers.winlab.mfirst.net.SessionParameters;

/**
 * @author Robert Moore
 * 
 */
public class IPv4UDPNAO extends IoHandlerAdapter implements NetworkAccessObject {

  /**
   * Set of listeners for this NAO.
   */
  private final Set<MessageListener> listeners = new ConcurrentHashSet<MessageListener>();

  /**
   * Whether or not writes should be asynchronous (non-blocking). Default is
   * synchronous (blocking) writes.
   */
  private boolean asyncWrites = false;

  /**
   * SessionParameter objects for each IoSession.
   */
  private final Map<IoSession, IPv4UDPParameters> sessionMap = new ConcurrentHashMap<IoSession, IPv4UDPParameters>();

  private final NioDatagramAcceptor acceptor;

  /**
   * Creates a new instance of network access object for IPv4/UDP networking.
   * 
   * @param asynchronous
   *          whether or not message sending should be asynchronous.
   *          Asynchronous writes to the network may cause internal buffering,
   *          synchronous writes will block.
   * @param port
   *          for incoming UDP packets.
   * @throws IOException
   *           if an IOException occurs while binding to the listen port.
   */
  // FIXME: Need to have specific configuration.
  public IPv4UDPNAO(final boolean asynchronous, final int listenPort)
      throws IOException {
    super();
    this.asyncWrites = asynchronous;

    this.acceptor = new NioDatagramAcceptor();
    this.acceptor.setHandler(this);

    DefaultIoFilterChainBuilder chain = this.acceptor.getFilterChain();
    // For encoding/decoding our messages
    chain.addLast("gnrs codec", new ProtocolCodecFilter(
        new GNRSProtocolCodecFactory(true)));

    DatagramSessionConfig sessionConfig = this.acceptor.getSessionConfig();
    sessionConfig.setReuseAddress(true);
    sessionConfig.setCloseOnPortUnreachable(false);

    this.acceptor.bind(new InetSocketAddress(listenPort));
  }

  @Override
  public void addMessageListener(MessageListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void sendMessage(SessionParameters parameters, AbstractMessage message) {
    if (!(parameters instanceof IPv4UDPParameters)) {
      throw new IllegalArgumentException(
          "Not an instance of IPv4UDP networking parameters: " + parameters);
    }
    IPv4UDPParameters p = (IPv4UDPParameters) parameters;
    WriteFuture f = p.session.write(message);
    if (this.asyncWrites) {
      f.awaitUninterruptibly();
    }
  }

  @Override
  public void endSession(final SessionParameters parameters) {
    if (!(parameters instanceof IPv4UDPParameters)) {
      throw new IllegalArgumentException(
          "Not an instance of IPv4UDP networking parameters: " + parameters);
    }
    IPv4UDPParameters p = (IPv4UDPParameters) parameters;
    p.session.close(true);
  }

  @Override
  public void removeMessageListener(final MessageListener listener) {
    this.listeners.remove(listener);
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
    for (MessageListener listener : this.listeners) {
      listener.messageReceived(params, (AbstractMessage) message);
    }
  }

  @Override
  public void sessionOpened(final IoSession session) {
    IPv4UDPParameters params = new IPv4UDPParameters();
    params.session = session;
    this.sessionMap.put(session, params);
  }

  @Override
  public void sessionClosed(final IoSession session) {
    this.sessionMap.remove(session);
  }
}
