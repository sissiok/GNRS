#include <pthread.h>
#include <time.h>
#include "gnrsclient.h"

extern struct send_recv_thread_args thread_args;
extern struct wait_ack_socket wait_socket;
//extern struct timeval starttime;
extern struct timespec starttime;
extern struct Client_Condition client_condition;

/////////////////////////////////////////////////////////////////
// timer function for waitForAckReceiver

/**
*  Create a seperate thread for this timer
*/
void* GNRSClient::timerProc(void* arg)
{
  GNRSClient *gnrsclient_=(GNRSClient *) arg;
  nanosleep(&(gnrsclient_->tdelay_), NULL);
  pthread_cancel(gnrsclient_->receivingThread);
  wait_socket.incConnection_->closeConnection();
  if(wait_socket.is_delete==false) {
	delete  wait_socket.clientListAddress;
	delete  wait_socket.incConnection_;
	}
  wait_socket.is_delete=true;
  if(DEBUG>=1) cout<<"GNRSClient: cancel waitForAckReceiver"<<endl;
  gnrsclient_->_is_ack_rev=false;
  return NULL;
}

 
/**
* Start a timer which will expire after a certain delay 
* for: waitForAckReceiver
* @param delay: the timing delay in seconds.
*/
void GNRSClient::startTimer(float delay)
{
  if(DEBUG>=1) cout<<"GNRSClient: start timer"<<endl;

   pthread_t tid_;
  tdelay_.tv_nsec = (long int)((delay - (int)delay)*1e9);
  tdelay_.tv_sec =  (int)delay; 

  int error = pthread_create(&tid_, NULL, &timerProc, this );
  thread_args.timingThread=tid_;
  if (error) 
    throw "GNRSClient: Timer thread creation failed...";
  pthread_detach(tid_);
 // if(DEBUG==1) cout<<"GNRSClient: timer thread joined!"<<endl;
}


/**
 * Stop a timer

void GNRSClient::stopTimer()
{
  pthread_cancel(tid_);
  if(DEBUG==1) cout<<"timer thread canceled!"<<endl;
}
*/

////////////////////////////////////////////////////////////////



GNRSClient::GNRSClient(char* filename, char* client_addr, char* server_addr, uint32_t _listen_port)
{
 GNRSConfig::init_defaults();
 GNRSConfig::read_from_file(filename);
 
 if(_listen_port>0)
	GNRSConfig::client_listen_port= _listen_port;
 //Init addresses  
 if(client_addr[0]!='\0')
	clientLocalAddress_ = new Address(client_addr, GNRSConfig::client_listen_port); 
 else
 	clientLocalAddress_ = new Address(GNRSConfig::client_addr.c_str(), GNRSConfig::client_listen_port); 
 thread_args.clientLocalAddress_ = *clientLocalAddress_;
 if(server_addr[0]!='\0')
 	daemonRemoteAddress_ = new Address(server_addr, GNRSConfig::daemon_listen_port); 
 else
 	daemonRemoteAddress_ = new Address(GNRSConfig::server_addr.c_str(), GNRSConfig::daemon_listen_port); 
 thread_args.daemonRemoteAddress_ = *daemonRemoteAddress_ ;
 //Init outgoing connection
 outConnection_ = new OutgoingConnnectionWithAck(); 
 outConnection_->setLocalAddress(clientLocalAddress_);
 outConnection_->setRemoteAddress(daemonRemoteAddress_);
 outConnection_->init();
 thread_args.outConnection_ = outConnection_;
}
/*Constructor that take daemonAddr from config file 
      + Load daemonAddress from config file
      + init clientAddress 
      + init outConnection_
*/
GNRSClient::GNRSClient(Address* clientAddr)
{
 GNRSConfig::init_defaults(); 
 GNRSConfig::read_from_file(DEFAULT_CONFIG_FILENAME);
 //Init addresses  
 clientLocalAddress_ = new Address(clientAddr->getHostname(), clientAddr->getPort()); 
 thread_args.clientLocalAddress_ = *clientLocalAddress_;
 daemonRemoteAddress_ = new Address(GNRSConfig::server_addr.c_str(), GNRSConfig::daemon_listen_port); 
 thread_args.daemonRemoteAddress_ = *daemonRemoteAddress_ ;
 //Init outgoing connection
 outConnection_ = new OutgoingConnnectionWithAck(); 
 outConnection_->setLocalAddress(clientLocalAddress_);
 outConnection_->setRemoteAddress(daemonRemoteAddress_);
 outConnection_->init();
 thread_args.outConnection_ = outConnection_;
}

// Constructor with given clientAddr and daemonAdrr
GNRSClient::GNRSClient(Address *clientAddr, Address *daemonAddr)
{
 GNRSConfig::init_defaults(); 
 GNRSConfig::read_from_file(DEFAULT_CONFIG_FILENAME); 
 //Init addresses  
 clientLocalAddress_ = new Address(clientAddr->getHostname(), clientAddr->getPort()); 
 thread_args.clientLocalAddress_ = *clientLocalAddress_;
 daemonRemoteAddress_ = new Address(daemonAddr->getHostname(), daemonAddr->getPort()); 
 thread_args.daemonRemoteAddress_ = *daemonRemoteAddress_ ;
 //Init outgoing connection
 outConnection_ = new OutgoingConnnectionWithAck(); 
 outConnection_->setLocalAddress(clientLocalAddress_);
 outConnection_->setRemoteAddress(daemonRemoteAddress_);
 outConnection_->init();
 thread_args.outConnection_ = outConnection_;
}


/* 
* Insert operation 
* @para: GUID, net_address
* @return: 0 if success  and -1 otherwise  
*/
int GNRSClient::insertProc(char guid[SIZE_OF_GUID], NA *temp, uint16_t NA_num, uint32_t req_id)
{
  
  //clear shared state
  thread_args.outConnection_->setACKflag(false); 
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
  //strncpy(insertPacket->message.insert_m.guid,insertMessage->guid,SIZE_OF_GUID);
  //strncpy(insertPacket->message.insert_m.net_addr,ptr->net_addr,sizeof(ptr->net_addr)); 
  //insertPacket->message.type=INSERT;
  //strcpy(insertPacket->message.insert_m.my_ip,clientLocalAddress_->getHostname());
  insertPacket->setPayload((char*)insertMessage, sizeof(insert_message_t)+NA_num*sizeof(NA));

  //gettimeofday(&starttime,0x0);
  //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime);
  clock_gettime(CLOCK_REALTIME, &starttime);
  outConnection_->timer_.startTimer(2.5); 
  //send the packet

  pthread_mutex_lock(&(client_condition.mutex));
  if(client_condition.condition_set)
	pthread_cond_wait(&(client_condition.condition), &(client_condition.mutex));
  pthread_mutex_unlock(&(client_condition.mutex));

  if(DEBUG>=1) cout<<"send out insert packet"<<endl;
  try {
  	outConnection_->sendPack(insertPacket);  }
  catch(const exception &ex) {
  	return(1);  }
	
  //then wait for Ack  
  outConnection_->lastPkt_ = insertPacket;
  outConnection_->setACKflag(false);
  //outConnection_->timer_.startTimer(2.5); 
  
  //delete the packet
  //delete(insertPacket);
  
  return 0;
}

void GNRSClient::lookupProc(char guid[SIZE_OF_GUID],uint32_t req_id)
{
  //clear shared state
  thread_args.outConnection_->setACKflag(false); 

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

  //gettimeofday(&starttime,0x0);
  //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime);
  clock_gettime(CLOCK_REALTIME, &starttime);
  //GUIDlookup->message.type=LOOKUP; 
  //strcpy(GUIDlookup->message.lookup_m.my_ip,clientLocalAddress_->getHostname());
  outConnection_->timer_.startTimer(2.5); 

  pthread_mutex_lock(&(client_condition.mutex));
  if(client_condition.condition_set)
        pthread_cond_wait(&(client_condition.condition), &(client_condition.mutex));
  pthread_mutex_unlock(&(client_condition.mutex));

  if(DEBUG>=1) cout<<"send out lookup packet"<<endl;
  outConnection_->sendPack(GUIDlookup);
  outConnection_->lastPkt_ = GUIDlookup;
  outConnection_->setACKflag(false);
  //outConnection_->timer_.startTimer(2.5); 

  //delete(GUIDlookup);

  /* signal to client 01 receiver to wait for ACK and time out  */
}



/*
 * Insert call
 */

int GNRSClient::insert(char guid[SIZE_OF_GUID], NA *temp, uint16_t NA_num, uint32_t req_id)
{
  //pthread_t sendingThread; 
  //pthread_t receivingThread; 
  try{
		client_condition.condition_set=true;
		if(pthread_create(&receivingThread,NULL,&waitForAckReceiver,(void*)&thread_args)){
	  		cout<<"Error creating receive thread, aborting"<<endl;
			return -1;
		}
		//int sendReturnThrd  = pthread_create(&sendingThread, NULL,insertProc(guid,net_addr),NULL);
		startTimer(15);   //timer for waitFor AckReceiver
		insertProc(guid, temp, NA_num, req_id);
		pthread_join(receivingThread,NULL);  //a bug exists here when no reponse comes back even if timer fires
		//pthread_join(sendingThread, NULL);
	   }
  catch (const char *message)
  {
	  cout<<"Exception in Insert:"<<message<<endl;
	  return -1;
  }
  return 0;
}

/*
 * Lookup call
 */
uint16_t GNRSClient::lookup(char guid[SIZE_OF_GUID], NA* result_buf,uint32_t req_id)
{
  //pthread_t sendingThread; 
 
  try{
		client_condition.condition_set=true;
		if(pthread_create(&receivingThread,NULL,&waitForAckReceiver,(void*)&thread_args)){
	  		cout<<"Error creating receive thread, aborting"<<endl;
			return -1;
		}
		//int sendReturnThrd  = pthread_create(&sendingThread, NULL,lookupProc(guid),NULL);
		startTimer(15);   //timer for waitFor AckReceiver
		lookupProc(guid, req_id);
		pthread_join(receivingThread,NULL);   //a bug exists here when no reponse comes back even if timer fires
		//pthread_join(sendingThread, NULL);
  }
  catch (const char *message)
  {
	  cout<<"Exception in lookup:"<<message<<endl;
	return -1;
  }
  
  for(int i=0;i<thread_args.na_num;i++)
  	nacpy(lookupResult[i],thread_args.lookupResult[i]);  
  na_num=thread_args.na_num;
  for(int i=0;i<na_num;i++)
  	nacpy(result_buf[i],lookupResult[i]);  
  
   return(na_num);
}
