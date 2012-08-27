/*
 * This class represents GNRS client object. 
 * 
 */
#ifndef GNRSCLIENT_H
#define GNRSCLIENT_H

#include "../common/outgoingconnection.h"
#include "../common/incomingconnection.h"
#include "../common/Messages.h"
#include "../common/gnrsconfig.h"
#include "../common/time_measure.h"

#define DEFAULT_CONFIG_FILENAME "./client.conf"

class GNRSClient
{

public:
   GNRSClient(char client_addr[SIZE_OF_NET_ADDR],char server_addr[SIZE_OF_NET_ADDR]); 

   int insertProc(char guid[SIZE_OF_GUID], NA *temp, uint16_t NA_num, uint32_t req_id);
   int lookupProc(char guid[SIZE_OF_GUID], uint32_t req_id); 

protected: 
  //connection to talk to GNRS daemon 
  OutgoingConnection *outConnection_; 
  IncomingConnection	*incConnection_; 

  Address *clientLocalAddress_ ; // for sending 
  Address *daemonRemoteAddress_; // for sending 

};

#endif // GNRSCLIENT_H
