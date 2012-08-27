/* 
 * Filename: outgoingconnection.h
 * Abstract class to send packet out
 * 
 * Note: 2 functions must be implemented by child class are: 
 *	+ TimeHandler
 * 	+ destructor
 *
 * Bind to local address is one important task in init() 
 * Here source address of node itself (myaddr_) does not really be used by bind function of port.
 * The program use INADDR_ANY as the address filled in address parameters of bind().
 * So, we need an empty hostname with the port number.
 */

#ifndef OUTGOINGCONNECTION_H
#define OUTGOINGCONNECTION_H

#include "socketconnection.h"
#include "packet.h"
#include "transmissiontimer.h"
#include "Messages.h"
#include <iostream>
#include <stdio.h>

using namespace std; 

class OutgoingConnection: public SocketConnection
{

public:
  //Default constructor- init local to : <localhost,DEFAULT_SEND_PORT_NUMBER>
  OutgoingConnection();
  
  // Another constructor with local address given
  OutgoingConnection(char* hostname, short port);
  
  //De-constructor  - Virtual Function
  ~OutgoingConnection() {
	delete[] sendingbuf_;
	//close(sockDescriptor_); }
  }

  //Initialize the connection
  void init();
  
  /*
  * Send a packet.
  * The default socket file descriptor will always be used for send()
  */
  void sendPack(Packet *pkt);
  
  /*
  * TimerHandler is called when the TransmissionTimer expires.
  * Note: This function is virtual. 
  */
  virtual void timerHandler(){if(DEBUG==1) cout<<"outgoingconnection timer handler"<<endl;}

protected:
  //Sending buffer   
  char *sendingbuf_;

public:
  /*
  * The timer used to schedule future events in a sending port.
  * When this timer expires, the timerHandler will be called.
  */
  TransmissionTimer timer_;
};

#endif //OUTGOINGCONNECTION_H
