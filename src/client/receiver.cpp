#include "gnrsclient.h"

extern struct timespec endtime;
//extern struct timeval endtime;

extern struct Client_Condition client_condition;

struct send_recv_thread_args thread_args;

struct wait_ack_socket wait_socket;    

/*
 * client wait for Ack at default client_listen_port
 */
void* waitForAckReceiver(void* ptr){
  struct send_recv_thread_args *args = (struct send_recv_thread_args *)ptr;
  
   wait_socket.incConnection_ = new IncomingConnection(); 
   wait_socket.clientListAddress = new Address(args->clientLocalAddress_.getHostname(), GNRSConfig::client_listen_port); 
   if(DEBUG>=1) cout<<"client_listen_port:"<<GNRSConfig::client_listen_port<<endl;
   wait_socket.incConnection_->setLocalAddress(wait_socket.clientListAddress); 
   wait_socket.incConnection_->init();
   wait_socket.is_delete=false;
  
  Packet *recevPacket; 
  bool notReceived =true;
  
  pthread_mutex_lock(&(client_condition.mutex)); 
  if(DEBUG>=1) cout<<"waitForAckReceiver starts!"<<endl;  
  client_condition.condition_set=false;
  pthread_cond_signal(&(client_condition.condition));
  pthread_mutex_unlock(&(client_condition.mutex));
  
  while (notReceived)
  {
    recevPacket = wait_socket.incConnection_->receivePacketDirectly();
	pthread_cancel(args->timingThread);
	if(DEBUG>=1) cout<<"cancel gnrsclient timingthread"<<endl;
    //gettimeofday(&endtime,0x0);
    //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &endtime);
    clock_gettime(CLOCK_REALTIME, &endtime);
    if (recevPacket != NULL)
    {
	common_header_t *hdr = (common_header_t*)recevPacket->getPayloadPointer();
	//cout<<"received packet type:"<<(int)hdr->type<<endl;
      if(hdr->type==INSERT_ACK)
	{ 
	    if(DEBUG>=1) cout <<"ACK for Insert Recieved!"<<endl;
	    while(!args->outConnection_->isACKed())
	    {
		  if(notReceived==true)
		  {
		    args->outConnection_->setACKflag(true);
		    args->outConnection_->timer_.stopTimer();
		    args->outConnection_->set_numbofresend(0);
		    delete args->outConnection_->lastPkt_;
		    notReceived=false;
		  }
	    }
	}
      else if(hdr->type == LOOKUP_RESP)//means this is a returning from lookup
	{
		lookup_response_message_t *resp = 	(lookup_response_message_t*)recevPacket->getPayloadPointer();
	  if(DEBUG>=1) cout<<"lookup response message received!"<< endl; 
	  NA* nas=(NA *)(resp+1);
	  for(int i=0;i<ntohs(resp->na_num);i++)
	  	ntoh_nacpy(args->lookupResult[i],nas[i]);
	  args->na_num=ntohs(resp->na_num);
	  while(!args->outConnection_->isACKed())
	    {
		  if(notReceived==true)
		  {
		    args->outConnection_->setACKflag(true);
		    args->outConnection_->timer_.stopTimer();
		    args->outConnection_->set_numbofresend(0);
		    delete args->outConnection_->lastPkt_;
		    notReceived=false;
		  }
	    }
	}
	  delete recevPacket;
    }
  }
   wait_socket.incConnection_->closeConnection(); 
  delete  wait_socket.clientListAddress;
  delete  wait_socket.incConnection_;
  wait_socket.is_delete=true;
  pthread_exit(NULL);
}
