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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Robert Moore
 * 
 */
public class UDPEchoClient {

  /**
   * Inter-packet delay time
   */
  public static final long INTERVAL = 1l;
  public static final int RECEIVE_TIMEOUT = 3000;
  public static final int DEFAULT_NUM_PKTS = 5000;

  public static boolean keepRunning = true;

  public static long[] sendTimes;
  public static long[] receiveTimes;

  public static void main(String[] args) throws Throwable {

    if (args.length < 3) {

      System.err.println("Please provide: <SRC PORT> <DST IP> <DST PORT> [<NUM PKTS>]");
      return;
    }

    final int srcPort = Integer.parseInt(args[0]);
    final InetAddress dstIp = Inet4Address.getByName(args[1]);
    final int dstPort = Integer.parseInt(args[2]);

    int tmpPkts = DEFAULT_NUM_PKTS;

    if(args.length > 3){
      tmpPkts = Integer.parseInt(args[3]);
      if(tmpPkts <= 0){
        tmpPkts = DEFAULT_NUM_PKTS;
      }
    }

    final int numPkts = tmpPkts;
    sendTimes = new long[numPkts];
    receiveTimes = new long[numPkts];
    Arrays.fill(sendTimes, -1);
    Arrays.fill(receiveTimes, -1);

    final DatagramSocket rcvSock = new DatagramSocket(srcPort);
    rcvSock.setSoTimeout(RECEIVE_TIMEOUT);
    final LinkedBlockingQueue<DatagramPacket> packetQueue = new LinkedBlockingQueue<DatagramPacket>();

    final Thread receiver = new Thread() {

      public void run() {
        int count = 0;
        byte[] data = new byte[8];
        DatagramPacket rcvPacket = new DatagramPacket(data, data.length);
        boolean keepRunning = true;
        long rcvTime = 0l;
        while (keepRunning) {
          try {
            rcvSock.receive(rcvPacket);
            rcvTime = System.nanoTime();
            ++count;
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(
                rcvPacket.getData()));
            int sequenceNumber = din.readInt();
            int ackValue = din.readInt();
            din.close();
            if (ackValue == 0) {
              System.err.println("Uh-oh, no ACK flag in response!");
              continue;
            }
            receiveTimes[sequenceNumber] = rcvTime;

          } catch (SocketTimeoutException ste) {
            keepRunning = false;
            continue;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        System.out.println("Received " + count + " pkts.");
      }
    };

    final Thread sender = new Thread() {
      public void run() {
        long sndTime = 0l;
        for (int i = 0; i < numPkts; ++i) {
          try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            dout.writeInt(i);
            // Value of 0 when sent, 1 when returned...
            dout.writeInt(0);
            dout.flush();
            byte[] payload = bout.toByteArray();
            dout.close();

            DatagramPacket sndPacket = new DatagramPacket(payload,
                payload.length, dstIp, dstPort);
            synchronized (sendTimes) {
              rcvSock.send(sndPacket);
              sndTime = System.nanoTime();
              sendTimes[i] = sndTime;
            }
            if(INTERVAL > 0){
              Thread.sleep(INTERVAL);
            }
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

    printStats(numPkts);

  }

  public static void printStats(int numPkts) {

    long[] rtts = new long[numPkts];
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    Arrays.fill(rtts, -1);
    float sumRTT = 0f;
    int count = 0;
    for (int i = 0; i < receiveTimes.length; ++i) {
      long rcvTime = receiveTimes[i];
      if (rcvTime < 0) {
        continue;
      }
      ++count;
      long diff = rcvTime - sendTimes[i];
      if (diff < min) {
        min = diff;
      }
      if (diff > max) {
        max = diff;
      }
      sumRTT += (rtts[i] = rcvTime - sendTimes[i]);
    }

//    for(int i = 0; i < rtts.length; ++i){
//      System.out.println(sendTimes[i] + "-" + receiveTimes[i] + ": " + rtts[i]);
//    }
    
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
    double pktLoss = ((rtts.length-count*1f)/rtts.length)*100;

    System.out.printf( "Min: %,.4fms\nMax: %,.4fms\nCount: %,d (%.2f %% loss)\nMean: %,.4fms\nS.Dev: %,.4f\n", min/1000000f, max/1000000f, count, pktLoss, avgRtt / 1000000, stdev / 1000000);

    long range = max - min;

    final long scale = 100000;

    if (range > 0 && count > 10 && max > 0) {
      // Size in nanoseconds of each bucket
      long bucketSize = (long)Math.max(scale, Math.ceil(range/39f));

      int[] buckets = new int[((int) Math.ceil((1f * range) / bucketSize))+1];
      String[] bucketLabels = new String[buckets.length];

      long baseBucket = (min / scale)*scale;

      for (int i = 0; i < bucketLabels.length; ++i) {
        bucketLabels[i] = String.format("%,.1f", (baseBucket + (bucketSize*i))/(scale*10f));
      }

      for (int i = 0; i < rtts.length; ++i) {
        if(rtts[i] > 0){
          ++buckets[(int) ((rtts[i] - min) / bucketSize)];
        }
      }

      int bucketMin = Integer.MAX_VALUE;
      int bucketMax = Integer.MIN_VALUE;

      for (int i = 0; i < buckets.length; ++i) {
        if (buckets[i] < bucketMin) {
          bucketMin = buckets[i];
        }
        if (buckets[i] > bucketMax) {
          bucketMax = buckets[i];
        }
      }

      float tickSize = numPkts/ 400f;
      System.out.printf("Tick: %.2f packets\n",tickSize);

      for (int i = 0; i < buckets.length; ++i) {
        System.out.print(bucketLabels[i] + " ");
        int fullTicks = (int)(buckets[i] / tickSize);
        int partTicks = (int)(buckets[i] % tickSize);
        for (int j = 0; j < fullTicks; ++j) {
          System.out.print('#');
        }
        if (partTicks > 0) {
          System.out.print('.');
        }
        System.out.printf(" [%d]\n",buckets[i]);
      }
    }
  }
}
