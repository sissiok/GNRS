/*
 * Filename: incomingconnection.cpp
 * 
 *  Abtract class for receiving connection
 */

#include "incomingconnection.h"
#include "socketconnection.h"


IncomingConnection::IncomingConnection(): SocketConnection()
{
   //pkt_ = new Packet(MAX_BUFFER_LENGTH); 
}

//Init the receiving Connection
void IncomingConnection::init()
{
   //Check socket descriptor
  if (sockDescriptor_ != 0 )
  {
    cerr << "socket has not been properly initialized, return now. "<< endl;
    return; 
  }
  //Check local address for initialization
  if (localAddress_.isSet()==false){
    setLocalHostname("localhost");
    setLocalPort(DEFAULT_RECV_PORT_NUMBER);
  }
  //Setup the socket
  if ((sockDescriptor_ = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
    throw "Error while opening UDP socket of a receiver";
  }
  Address *emptyAddr = new Address("", localAddress_.getPort());  //TODO:memory leak might exist here
  struct sockaddr* addr = setSockAddress(emptyAddr, &localSockAddress_);
  //bind to socket descriptor 
  if (  bind(sockDescriptor_, addr, sizeof(struct sockaddr_in))  < 0 ){
             perror("bind");
             cerr << "error in binding receiving socket to addr " <<endl; 
  }
  delete emptyAddr; //emptyAddr is not needed, clear memory leak
  //Init temp buffer, not used at all
  tmpBuffer_ =  new char[MAX_BUFFER_LENGTH];
}


/* 
 *  The main receiving function of a receiving connection.
 *  Return a packet received
 *  Note: Packet is also stored in pkt_ field 
 */
// Packet* IncomingConnection::receivePacketThroughBuffer()
// {
//   struct sockaddr_in tmpSockAddr;
//   int length = sizeof(struct sockaddr);
//   //call recvfrom() to get packet
//   int len = (int)recvfrom(sockDescriptor_, tmpBuffer_, MAX_BUFFER_LENGTH, 0, (struct sockaddr*)&tmpSockAddr,(socklen_t *)&length); 
//   //check if it receives anything 
//   if (len == -1) 
//   {
//            perror("recvfrom");
//            return false;
//   }   
//   //decode the socket address to get remote address 
//   decodeSockAddress(&remoteAddress_, &tmpSockAddr);
//   //get packet header from the buffer 
//   pkt_->extractHeader(tmpBuffer_);
//   //fill the payload of the packet 
//   pkt_->fillPayload(len-1-pkt_->getHeaderSize(), tmpBuffer_+pkt_->getHeaderSize()+1 );
//   return pkt_;
// }

// Receiving packet without converting it to separate header
Packet* IncomingConnection::receivePacketDirectly()
{
  struct sockaddr_in tmpSockAddr;
  char buf[MAX_BUFFER_LENGTH];
  int length = sizeof(struct sockaddr);
  //call recvfrom() to get packet
  int len = (int)recvfrom(sockDescriptor_, buf, MAX_BUFFER_LENGTH, 0, (struct sockaddr*)&tmpSockAddr,(socklen_t *)&length); 
  //check if it receives anything 
  if (len == -1) 
  {
           perror("recvfrom");
           return false;
  }  
	pkt_ = new Packet(len);
	//pkt_->setPayload(buf,len);  //buf is only a local variable, will be released after the function call.	
	memcpy(pkt_->getPayloadPointer(), buf, len);
	pkt_->setPayloadSize(len);
  //decode the socket address to get remote address 
  if(DEBUG>=1)  {
	decodeSockAddress(&remoteAddress_, &tmpSockAddr);
    	printf("Received packet from %s:%d\n", remoteAddress_.getHostname(), remoteAddress_.getPort());
	}
  return pkt_; 
}

