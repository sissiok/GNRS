/*
 * UDP Echo Server
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

/**
 * @author Robert Moore
 * 
 */
public class UDPEchoServer {
  public static void main(String[] args) throws Throwable {
    if (args.length < 1) {
      System.err.println("Need to specify the listen port!");
      return;
    }

    int rcvPort = Integer.parseInt(args[0]);
    final DatagramSocket rcvSocket = new DatagramSocket(rcvPort);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {

        rcvSocket.close();
      }
    });

    DatagramPacket rcvPacket = new DatagramPacket(new byte[8], 8);
    int count = 0;
    while (true) {
      rcvSocket.receive(rcvPacket);
      System.out.println("Pkt from " + rcvPacket.getAddress() + ":" + rcvPacket.getPort());
      byte[] data = rcvPacket.getData();
      DataInputStream din = new DataInputStream(new ByteArrayInputStream(data));
      int sequenceNumber = din.readInt();
      din.close();
      ByteArrayOutputStream bout = new ByteArrayOutputStream(8);
      DataOutputStream dout = new DataOutputStream(bout);
      dout.writeInt(sequenceNumber);
      dout.writeInt(++count);
      dout.flush();
      data = bout.toByteArray();
      dout.close();
      DatagramPacket sndPacket = new DatagramPacket(data, data.length,
          rcvPacket.getAddress(), rcvPacket.getPort());
      rcvSocket.send(sndPacket);
    }
  }
}
