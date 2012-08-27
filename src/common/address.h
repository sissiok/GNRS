/*  Filename: address.h
 *  Address Class is to handle addresses of unix/linux sockets.
 *  It is defined by a pair of <Hostname or IP address, portnumber> 
 *  For normal sockets, the address used will be a combination of IP address and port.
 *  In Socket Programming, IP address itself is usually not enough to distinguish an connection, port # is also needed.
 *  For PF_PACKET sockets, the address used is MAC address(HW address). So, we also put macaddr_ as a member variable.
 */

#ifndef ADDRESS_H
#define ADDRESS_H

#include <string.h>
#include <stdio.h>

#define MAX_HOSTNAME_LENGTH 256
#define MAC_ADDR_LENGTH 6

class Address
{

public:
  // Constructor
  Address();
  // Alternative construcor with parameters
  Address(const char* hostname, short port); 
 
  
  //convert MacAddress to colone-separated format
  char* convertMacAddrToColonFormat();
  
  // set the hostname
  void setHostname(const char* hostname);
  
  // Set macAddress 
  void setMacAddr(unsigned char* hwaddr);
  
  // Set macAddress from colon-separated format
  void setMacAddrFromColonFormat(const char* colon_seperated_macaddr);
  
  // clone this address
  Address *clone();
  
  //Compare whether the two normal "name+port" address is same or not  
  bool isSame(Address* addr);
  
  //Compare whether the two MAC addresses are the same or not  
  bool isSameMacAddr(Address* addr);
  
  //Check if an address has already been set or remain uninitialized  
  inline bool isSet() { return (hostname_[0] != '\0' && port_ >= 0);}
  
  // Set the port # of the Address
  inline void setPort(const short port) {   port_ = port;  }
  
  // get the port #
  inline short getPort() {   return port_;  }  
  
  // get the hostname string pointer
  inline char* getHostname() {  return hostname_;  }
  
  // get the MAC address
  inline unsigned char* getMacAddr() {  return macAddr_;  }

protected:
  // human readable hostname or IP address 
  char hostname_[MAX_HOSTNAME_LENGTH];
  // port number for TCP/UDP protocols (transport protocols in general)
  short port_; 
  //optional --- use... ignore....
  char *ipAddr_; 
  //optional-- use for Ethernet Socket
  unsigned char macAddr_[MAC_ADDR_LENGTH]; 
};

#endif // ADDRESS_H
