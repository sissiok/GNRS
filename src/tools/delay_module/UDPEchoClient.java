/*
 * UDP Echo Client
 * Copyright (C) 2012 Robert Moore
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert Moore
 * 
 */
public class UDPEchoClient {

  /**
   * Inter-packet delay time
   */
  public static final long INTERVAL = 5l;
  public static final int RECEIVE_TIMEOUT = 1000;
  public static final int NUM_PKTS = 1000;

  public static boolean keepRunning = true;

  public static void main(String[] args) throws Throwable {

    final long[] sendTimes = new long[NUM_PKTS];
    Arrays.fill(sendTimes,-1);
    final long[] rtts = new long[NUM_PKTS];
    Arrays.fill(rtts,-1);

    if (args.length < 3) {

      System.err.println("Please provide: <SRC PORT> <DST IP> <DST PORT>");
      return;
    }

    final int srcPort = Integer.parseInt(args[0]);
    final InetAddress dstIp = Inet4Address.getByName(args[1]);
    final int dstPort = Integer.parseInt(args[2]);

    final DatagramSocket rcvSock = new DatagramSocket(srcPort);
    rcvSock.setSoTimeout(RECEIVE_TIMEOUT);

    final Thread receiver = new Thread() {

      public void run() {
        byte[] data = new byte[8];
        DatagramPacket rcvPacket = new DatagramPacket(data, data.length);

        while (UDPEchoClient.keepRunning) {
          try {
            rcvSock.receive(rcvPacket);
            long rcvTime = System.nanoTime();
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(
                rcvPacket.getData()));
            int sequenceNumber = din.readInt();
            int ackValue = din.readInt();
            if (ackValue == 0) {
              System.err.println("Uh-oh, no ACK flag in response!");
              continue;
            }
            synchronized (sendTimes) {
              rtts[sequenceNumber] = rcvTime - sendTimes[sequenceNumber];
            }
          } catch (SocketTimeoutException ste) {
            // Ignored, check the loop condition
            continue;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    };

    final Thread sender = new Thread() {
      public void run() {

        for (int i = 0; i < NUM_PKTS; ++i) {
          try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            dout.writeInt(i);
            // Value of 0 when sent, 1 when returned...
            dout.writeInt(0);
            dout.flush();
            byte[] payload = bout.toByteArray();

            DatagramPacket sndPacket = new DatagramPacket(payload,
                payload.length, dstIp, dstPort);
            synchronized (sendTimes) {
              rcvSock.send(sndPacket);
              sendTimes[i] = System.nanoTime();
            }
            Thread.sleep(INTERVAL);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        UDPEchoClient.keepRunning = false;
      }
    };

    System.out.println("Starting threads.");

    receiver.start();
    sender.start();

    while (receiver.isAlive()) {
      Thread.sleep(100);
    }

    System.out.println("Computing statistics...");

    printStats(rtts);

  }

  public static void printStats(long[] rtts) {
    float sumRTT = 0f;
    int count = 0;
    for (long rtt : rtts) {
      if (rtt < 0) {
        continue;
      }
      ++count;
      sumRTT += rtt;
    }

    float avgRtt = sumRTT / count;

    float[] squareDiff = new float[rtts.length];

    for (int i = 0; i < rtts.length; ++i) {
      long rtt = rtts[i];
      if (rtt < 0) {
        continue;
      }
      squareDiff[i] = (float) Math.pow(rtt - avgRtt, 2);
    }

    double variance = 0;
    for (float diff : squareDiff) {
      variance += diff;
    }
    variance /= count;
    double stdev = Math.sqrt(variance);

    System.out.printf("Sum: %,.1fns\nCount: %,d\nMean: %,.3fns\nS.Dev: %,.3fns\n", sumRTT, count, avgRtt, stdev);
  }
}
