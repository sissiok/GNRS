/** 
 * Filename: socketconnection.h
 * 
 * SocketConnection is an abstract class for the interface to send/receive a packet, whether UDP, TCP Socket, or IP raw socket.
 * A SocketConnection object is defined by a <SourceAddress and DestinationAddress>
 * 
 *  Note: Abstract class
 * 
 */
#ifndef SOCKETCONNECTION_H
#define SOCKETCONNECTION_H

#include "address.h"
#include "common.h"

#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <unistd.h>


#define DEFAULT_SEND_PORT_NUMBER 3000
#define DEFAULT_RECV_PORT_NUMBER 4000
#define MTU_SIZE 1500

using namespace std; 

class SocketConnection
{

public:
  
    SocketConnection();

    virtual ~SocketConnection(){close(sockDescriptor_);}
    
    //initialize - need to be implemented by the child class
    virtual void init()=0;
    
    // set the connection's local address
    void setLocalAddress(Address *addr);
    
    // set the connection's remote address
    void setRemoteAddress(Address *daddr);
    
    // get the address of the port at the other end of communication link
    inline Address *getRemoteAddr() { return &remoteAddress_;}
    
    // close the port
    inline void closeConnection() { close( sockDescriptor_ );}
    
protected:
    // cast an Address to socket address format
    struct sockaddr* setSockAddress(Address *addr, struct sockaddr_in *address);
    
    // cast a socket address to normal address format
    void decodeSockAddress(Address *addr, struct sockaddr_in *address);
    
    // set hostname of local address
    inline void setLocalHostname(const char* hostname) { localAddress_.setHostname(hostname);}
    
    // set port number of local address
    inline void setLocalPort(const short port) {localAddress_.setPort(port); }
    
    // set hostname of remote address
    inline void setRemoteHostname(const char* hostname) {  remoteAddress_.setHostname(hostname);}
    
    // set port number of a remote address
    inline void setRemotePort(const short port) { remoteAddress_.setPort(port);  }
    
    // get the socket file descriptor 
    inline int getSock() {  return sockDescriptor_;}
    
protected:
  //Local address - Note: Address  = <IP,Port> 
  Address localAddress_;
  //Remote address
  Address remoteAddress_;
  //My IP
  struct sockaddr_in localSockAddress_;
  //Remote IP
  struct sockaddr_in remoteSockAddres_;
  //socket descriptor
  int sockDescriptor_; 
};

#endif //SOCKETCONNECTION_H
