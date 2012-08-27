/*
 * Filename: incomingconnection.h
 * 
 *  Abtract class for receiving connection
 */
#ifndef INCOMINGCONNECTION_H
#define INCOMINGCONNECTION_H

#define MAX_BUFFER_LENGTH 1000

#include "socketconnection.h"
#include "packet.h"
#include "common.h"

#include <iostream>
#include <stdio.h>
#include <errno.h>

using namespace std; 

class IncomingConnection : public SocketConnection
{

public:
  IncomingConnection();
  ~IncomingConnection(){delete[] tmpBuffer_;}
  void init();
  
  //The main receive function of receiving port to receive a single packet.
  Packet* receivePacketThroughBuffer(); 
  // Receiving packet without converting it to separate header
  Packet* receivePacketDirectly(); 
    
protected:
  //  This pointer points to the packet. This packet is just received.
  Packet *pkt_;   
  //temporary buffer for packets
  char *tmpBuffer_; 
};

#endif // INCOMINGCONNECTION_H
