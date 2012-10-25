#include "outgoingconnection.h"

//Default constructor- init local to : <localhost,DEFAULT_SEND_PORT_NUMBER>
OutgoingConnection::OutgoingConnection(): SocketConnection(), timer_(this)
{
  setLocalHostname("localhost");
  setLocalPort(DEFAULT_SEND_PORT_NUMBER);
}

//Constructor with local address given
OutgoingConnection::OutgoingConnection(char* hostname, short int port): SocketConnection(),timer_(this)
{
  setLocalHostname(hostname);
  setLocalPort(port);
}

//Open up a socket 
void OutgoingConnection::init()
{
  //Check socket descriptor
  if (sockDescriptor_ !=0) 
  {
    cerr << "socket has not been properly initialized, return now. "<< endl;
    return; 
  }
  //Check local address for initialization
  if(localAddress_.isSet() == false){
    setLocalHostname("localhost");
    setLocalPort(DEFAULT_SEND_PORT_NUMBER);
  }
  //Check remoteAddress for initialization
  //if(remoteAddress_.isSet() == false)
   // throw "Destination address of a sending port is not set!";
  //Open a UDP socket 
  if ((sockDescriptor_ = socket(AF_INET,SOCK_DGRAM,0)) < 0) {
     throw "Error while opening a UDP socket";
  }

  Address *emptyAddr = new Address("", localAddress_.getPort());  //TODO:memory leak might exist here
  struct sockaddr* addr = setSockAddress(emptyAddr, &localSockAddress_);
  //bind to socket descriptor 
  if (  bind(sockDescriptor_, addr, sizeof(struct sockaddr_in))  < 0 ){
             perror("bind");
             cerr << "error in binding receiving socket to addr " <<endl; 
  }
  delete emptyAddr; //emptyAddr is not needed, clear memory leak

  //Create sending buffer 
  sendingbuf_ = new char[MTU_SIZE +1];
  return; 
}

// send a packet
// Input: Packet buffer   and packetLength
void OutgoingConnection::sendPack(Packet* pkt)
{
  Address *dst = getRemoteAddr();
  //int pktLength = sizeof(struct Messages); 
int pktLength = pkt->getPayloadSize();
  int addrLength = sizeof(struct sockaddr_in); 
  struct sockaddr *destSockAddr = setSockAddress(dst,&remoteSockAddres_);
  int successSentLength = sendto(sockDescriptor_,pkt->getPayloadPointer(),pktLength,0,destSockAddr,addrLength); 
  
  if (successSentLength == -1) 
    throw "Sending Error from OngoingConnection::sendpack"; 
}




