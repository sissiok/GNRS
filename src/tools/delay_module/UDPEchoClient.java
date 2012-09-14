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
import java.util.Random;

/**
 * @author Robert Moore
 * 
 */
public class UDPEchoClient {

  /**
   * Inter-packet delay time
   */
  public static final long INTERVAL = 500l;
  static Random rand = new Random(System.currentTimeMillis());

  public static void main(String[] args) throws Throwable {

    if (args.length < 3) {

      System.err.println("Please provide: <SRC PORT> <DST IP> <DST PORT>");
      return;
    }

    int srcPort = Integer.parseInt(args[0]);
    InetAddress dstIp = Inet4Address.getByName(args[1]);
    int dstPort = Integer.parseInt(args[2]);

    DatagramSocket rcvSock = new DatagramSocket(srcPort);

    for (int i = 0; i < 10; ++i) {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      DataOutputStream dout = new DataOutputStream(bout);
      int sequenceNumber = rand.nextInt(1000000);
      dout.writeInt(sequenceNumber);
      // Placeholder for number of packets sent
      dout.writeInt(0);
      dout.flush();
      byte[] payload = bout.toByteArray();

      DatagramPacket sndPacket = new DatagramPacket(payload, payload.length,
          dstIp, dstPort);
      rcvSock.send(sndPacket);
      long start = System.currentTimeMillis();
      DatagramPacket rcvPacket = new DatagramPacket(new byte[8], 8);

      rcvSock.receive(rcvPacket);
      long end = System.currentTimeMillis();
      byte[] rcvData = rcvPacket.getData();
      DataInputStream din = new DataInputStream(new ByteArrayInputStream(rcvData));
      int rcvSequenceNumber = din.readInt();
      int count = din.readInt();
      System.out.printf("%06d:%03d: %dms\n", rcvSequenceNumber, count, (end-start));
      Thread.sleep(INTERVAL);
    }
  }

}
