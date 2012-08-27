/** 
 * Filename: socketconnection.cpp
 * SocketConnection is an abstract class for the interface to send/receive a packet, whether UDP, TCP Socket, or IP raw socket.
 * A SocketConnection object is defined by a <SourceAddress and DestinationAddress>
 * 
 *  Note: Abstract class
 * 
 */

#include "socketconnection.h"

#include <stdio.h>
#include <stdlib.h>
#include<iostream>

using namespace std;

SocketConnection::SocketConnection():sockDescriptor_(0)
{
}

// set the connection's local address
void SocketConnection::setLocalAddress(Address* addr)
{
  localAddress_.setHostname(addr->getHostname());
  localAddress_.setPort(addr->getPort());
}

// set the connection's local address
void SocketConnection::setRemoteAddress(Address* addr)
{
  remoteAddress_.setHostname(addr->getHostname());
  remoteAddress_.setPort(addr->getPort());
}

/**
  *  Fill sockaddr_in 'address' structure with information taken from
  * 'addr' and return it cast to a 'struct sockaddr'.
  *  @input: Address* addr
  *  @output: struct sockaddr
  * 
  * It handles following situations:
  * - if hostname is given as empty "", then INADDR_ANY is used in return
  * - if an IP address is given, then address could be set directly
  * - if a hostname is given, call gethostbyname() to find the ip address of the hostname from DNS
  * 
  */

struct sockaddr* SocketConnection::setSockAddress(Address* addr, sockaddr_in* address)
{
  char *hostname;
  int portNumb;
  unsigned int tmp;
  struct hostent *hp;
  
  // Get address info from addr
  hostname = addr->getHostname();
  if(DEBUG>=1) cout << "gethostbyname for hostname: "  << hostname << endl;
  portNumb = addr->getPort();
  
  //assign socket specs & port Numb
  address->sin_family = AF_INET;
  address->sin_port   = htons((short)portNumb);

  if (strcmp(hostname, "") == 0) {
    address->sin_addr.s_addr = htonl(INADDR_ANY);  
  } 
  else {
    tmp = inet_aton(hostname, &(address->sin_addr));
     if (tmp == 0)
    { 
      if ((hp = gethostbyname(hostname)) == NULL) {
	cout << "gethostbyname err for hostname: "  << hostname << endl;
        herror("gethostbyname");
   	throw "Error in Resolving hostname!" ;                           
      }
      memcpy((char *)&address->sin_addr, (char *)hp->h_addr, hp->h_length);
     } 
  }
  return (sockaddr*)address;
}

// cast a socket address to normal address format
void SocketConnection::decodeSockAddress(Address *addr, struct sockaddr_in *address)
{
   addr->setHostname(inet_ntoa(address->sin_addr));
   addr->setPort(ntohs(address->sin_port));
}  

