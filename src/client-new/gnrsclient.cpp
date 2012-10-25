#include <time.h>
#include "gnrsclient.h"

GNRSClient::GNRSClient( char* client_addr, char* server_addr)
{
 //Init addresses  
 //sending port = listening port + 1000
 if(client_addr[0]!='\0')
	clientLocalAddress_ = new Address(client_addr, GNRSConfig::client_listen_port+1000); 
 else
 	clientLocalAddress_ = new Address(GNRSConfig::client_addr.c_str(), GNRSConfig::client_listen_port+1000); 

 if(server_addr[0]!='\0')
 	daemonRemoteAddress_ = new Address(server_addr, GNRSConfig::daemon_listen_port); 
 else
 	daemonRemoteAddress_ = new Address(GNRSConfig::server_addr.c_str(), GNRSConfig::daemon_listen_port); 

 //Init outgoing connection
 outConnection_ = new OutgoingConnection(); 
 outConnection_->setLocalAddress(clientLocalAddress_);
 outConnection_->setRemoteAddress(daemonRemoteAddress_);
 outConnection_->init();

}

/* 
* Insert operation 
* @para: GUID, net_address
* @return: 0 if success  and -1 otherwise  
*/
int GNRSClient::insertProc(char guid[SIZE_OF_GUID], NA *temp, uint16_t NA_num, uint32_t req_id)
{
 
  //construct the insert message
  insert_message_t *insertMessage = (insert_message_t *)malloc(sizeof(insert_message_t)+NA_num*sizeof(NA)) ;
  //common header fields
  insertMessage->c_hdr.type = INSERT;
  insertMessage->c_hdr.req_id = htonl(req_id); 
  strcpy(insertMessage->c_hdr.sender_addr,clientLocalAddress_->getHostname());
  insertMessage->c_hdr.sender_listen_port=htonl(GNRSConfig::client_listen_port);

  //copy guid
  strncpy(insertMessage->guid,guid,SIZE_OF_GUID );   
  insertMessage->dest_flag=0;
  insertMessage->na_num=htons(NA_num);
  
  //copy NA
  NA * nas=(NA *)(insertMessage+1);
  int i;
  for (i=0;i<NA_num;i++)
  {
  	strncpy(nas[i].net_addr,temp->net_addr,SIZE_OF_NET_ADDR);
	nas[i].ttlMsec=htonl(temp->ttlMsec);
  	nas[i].weight=htons(temp->weight);
  }
  //put into a packet
  Packet* insertPacket = new Packet;   

  insertPacket->setPayload((char*)insertMessage, sizeof(insert_message_t)+NA_num*sizeof(NA));


  if(DEBUG>=1) cout<<"send out insert packet"<<endl;
  try {
	  outConnection_->sendPack(insertPacket);  }
  catch (const char *message) {
	cout<<"Exception in insertProc:"<<message<<endl;
  	delete(insertPacket);
	return 1;
  	}

    //delete the packet
  delete(insertPacket);
  
  return 0;
}



int GNRSClient::lookupProc(char guid[SIZE_OF_GUID],uint32_t req_id)
{

  //Building the Lookup message
  lookup_message_t *lookupMessage = (lookup_message_t *)malloc(sizeof(lookup_message_t)) ;
  
  //common header fields
  lookupMessage->c_hdr.type = LOOKUP;
  lookupMessage->c_hdr.req_id = htonl(req_id);
  strcpy(lookupMessage->c_hdr.sender_addr, clientLocalAddress_->getHostname());
  lookupMessage->c_hdr.sender_listen_port=htonl(GNRSConfig::client_listen_port);

  strncpy(lookupMessage->guid,guid,SIZE_OF_GUID); 
  lookupMessage->dest_flag=0;
  //strncpy(GUIDlookup->message.lookup_m.guid,ptr->guid,SIZE_OF_GUID); 
  
  Packet * GUIDlookup = new Packet();
  GUIDlookup->setPayload((char*)lookupMessage, sizeof(lookup_message_t));

  if(DEBUG>=1) cout<<"send out lookup packet"<<endl;
  try{
  outConnection_->sendPack(GUIDlookup);  }
  catch (const char *message) {
	cout<<"Exception in insertProc:"<<message<<endl;
  	delete(GUIDlookup);
	return 1;
  	}


  delete(GUIDlookup);
  return 0;

}


