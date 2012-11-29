/*
 * Copyright (c) 2012, Rutgers University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.rutgers.winlab.mfirst;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rutgers.winlab.mfirst.messages.ExpirationOption;
import edu.rutgers.winlab.mfirst.messages.InsertMessage;
import edu.rutgers.winlab.mfirst.messages.InsertResponseMessage;
import edu.rutgers.winlab.mfirst.messages.LookupMessage;
import edu.rutgers.winlab.mfirst.messages.LookupResponseMessage;
import edu.rutgers.winlab.mfirst.messages.Option;
import edu.rutgers.winlab.mfirst.messages.RecursiveRequestOption;
import edu.rutgers.winlab.mfirst.messages.ResponseCode;
import edu.rutgers.winlab.mfirst.messages.TTLOption;
import edu.rutgers.winlab.mfirst.net.NetworkAddress;
import edu.rutgers.winlab.mfirst.net.SessionParameters;
import edu.rutgers.winlab.mfirst.storage.GUIDBinding;

/**
 * Task to handle Insert messages within the server. Designed to operate
 * independently of any other messages.
 * 
 * @author Robert Moore
 */
public class InsertTask implements Callable<Object> {

  /**
   * Logging for this class.
   */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(InsertTask.class);

  /**
   * The message and some metadata for this task.
   */
  private final transient SessionParameters params;

  /**
   * The received insert message.
   */
  private final transient InsertMessage message;

  /**
   * The server that is handling the message.
   */
  private final transient GNRSServer server;

  /**
   * Creates a new InsertTask for the specified server and message container.
   * 
   * @param server
   *          the server that received or is handling the message.
   * @param params
   *          the session-related metadata.
   * @param message
   *          the message to process
   */
  public InsertTask(final GNRSServer server, final SessionParameters params,
      final InsertMessage message) {
    super();
    this.server = server;
    this.params = params;
    this.message = message;
  }

  @Override
  public Object call() {
    final long startProc = System.nanoTime();
    GNRSServer.NUM_INSERTS.incrementAndGet();

    final Collection<NetworkAddress> serverAddxes = this.server.getMappings(
        this.message.getGuid(), this.message.getOriginAddress().getType());

    

    boolean resolvedLocally = false;
    if (serverAddxes != null && !serverAddxes.isEmpty()) {
      for (Iterator<NetworkAddress> iter = serverAddxes.iterator(); iter
          .hasNext();) {
        NetworkAddress addx = iter.next();
        // Loopback? Then the local server should handle it.
        if (this.server.isLocalAddress(addx)) {
          iter.remove();
          resolvedLocally = true;
        }
      }
    }
    boolean localSuccess = false;

    boolean recursive = false;
    List<Option> options = this.message.getOptions();
    long expirationTime = this.server.getConfig().getDefaultExpiration()
        + System.currentTimeMillis();
    long ttlValue = this.server.getConfig().getDefaultTtl();
    if (!options.isEmpty()) {
      for (Option opt : options) {
        if (opt instanceof RecursiveRequestOption) {
          recursive = ((RecursiveRequestOption) opt).isRecursive();
        } else if (opt instanceof ExpirationOption) {
          expirationTime = ((ExpirationOption) opt).getExpiration();
        } else if (opt instanceof TTLOption) {
          ttlValue = ((TTLOption) opt).getTtl();
        }
      }
    }

    if (resolvedLocally) {
      localSuccess = this.server.appendBindings(this.message.getGuid(),
          this.message.getBindings());
    } else if (recursive && this.message.getBindings() != null) {
      GUIDBinding[] bindings = new GUIDBinding[this.message.getBindings().length];

      for (int i = 0; i < bindings.length; ++i) {
        NetworkAddress netAddr = this.message.getBindings()[i];
        GUIDBinding bind = new GUIDBinding();
        bind.setAddress(netAddr);
        bind.setExpiration(expirationTime);
        bind.setTtl(ttlValue);
      }

      this.server.addToCache(this.message.getGuid(), bindings);
    }
    
    // At least one IP prefix binding was for the local server
    if (recursive) {
      // this.message.setRecursive(false);

      final RelayInfo info = new RelayInfo();
      info.clientMessage = this.message;
      info.remainingServers.addAll(serverAddxes);

      final int requestId = this.server.getNextRequestId();

      final InsertMessage relayMessage = new InsertMessage();
      relayMessage.setGuid(this.message.getGuid());

      for (Option opt : this.message.getOptions()) {
        if (!(opt instanceof RecursiveRequestOption)) {
          relayMessage.addOption(opt);
        }
      }
      relayMessage.finalizeOptions();

      relayMessage.setOriginAddress(this.server.getOriginAddress());
      relayMessage.setVersion((byte) 0);
      relayMessage.setRequestId(requestId);
      relayMessage.setBindings(this.message.getBindings());

      if (serverAddxes != null) {
        this.server.addNeededServer(Integer.valueOf(requestId), info);
        this.server.sendMessage(relayMessage,
            serverAddxes.toArray(new NetworkAddress[] {}));
        info.markAttempt();
      } else {
        LOG.error("Invalid server addresses.  Cannot forward.");
      }
    }

    if (resolvedLocally && !recursive) {

      InsertResponseMessage response = new InsertResponseMessage();
      response.setOriginAddress(this.server.getOriginAddress());
      response.setRequestId(this.message.getRequestId());
      response.setResponseCode(localSuccess ? ResponseCode.SUCCESS
          : ResponseCode.FAILED);
      response.setVersion((byte) 0);

      this.server.sendMessage(response, this.message.getOriginAddress());

    }

    long endProc = System.nanoTime();
    if (this.server.getConfig().isCollectStatistics()) {
      GNRSServer.INSERT_STATS[GNRSServer.QUEUE_TIME_INDEX].addAndGet(startProc-this.message.createdNanos);
      GNRSServer.INSERT_STATS[GNRSServer.PROC_TIME_INDEX].addAndGet(endProc-startProc);
      GNRSServer.INSERT_STATS[GNRSServer.TOTAL_TIME_INDEX].addAndGet(endProc-this.message.createdNanos);

    }

    return null;
  }

}
