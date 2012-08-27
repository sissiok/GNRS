#include "receiver.h"

extern struct Client_Condition client_condition;
extern int rec_insert_num,rec_lookup_num;

extern pthread_mutex_t lkup_pkt_sampling_mutex;
extern PKT_SAMPLE_MAP _pkt_sample;

/*
 * client wait for Ack at specified listending port
 */
void* waitForAckReceiver(void* ptr){
  
   IncomingConnection *incConnection_ = new IncomingConnection(); 
   Address* clientListAddress = new Address(GNRSConfig::client_addr.c_str(), GNRSConfig::client_listen_port); 
   if(DEBUG>=1) cout<<"client_listen_port:"<<GNRSConfig::client_listen_port<<endl;
   incConnection_->setLocalAddress(clientListAddress); 
   incConnection_->init();
  
  Packet *recevPacket; 
  
  pthread_mutex_lock(&(client_condition.mutex)); 
  if(DEBUG>=1) cout<<"waitForAckReceiver starts!"<<endl;  
  client_condition.condition_set=false;
  pthread_cond_signal(&(client_condition.condition));
  pthread_mutex_unlock(&(client_condition.mutex));
  
  while (1)
  {
    recevPacket = incConnection_->receivePacketDirectly();

    if (recevPacket != NULL)
    {
	common_header_t *hdr = (common_header_t*)recevPacket->getPayloadPointer();
	//cout<<"received packet type:"<<(int)hdr->type<<endl;
      if(hdr->type==INSERT_ACK)
	{ 
	    if(DEBUG>=1) cout <<"ACK for Insert Recieved!"<<endl;
	    rec_insert_num++;
	}
      else if(hdr->type == LOOKUP_RESP)//means this is a returning from lookup
	{
		lookup_response_message_t *resp = (lookup_response_message_t*)recevPacket->getPayloadPointer();
	  if(DEBUG>=1) cout<<"lookup response message received!"<< endl; 
	  NA* nas=(NA *)(resp+1);
	  uint16_t na_num=ntohs(resp->na_num);
	if(SAMPLING==1)  {
		uint32_t _req_id=ntohl(resp->c_hdr.req_id);
		pthread_mutex_lock(&lkup_pkt_sampling_mutex);
		//uint32_t _req_id=ntohl(hdr->req_id);
		PKT_SAMPLE_MAP::iterator _it=_pkt_sample.find(_req_id);
		if(_it!=_pkt_sample.end())  {
			//cout<<"req_id:"<<_req_id<<endl;
			clock_gettime(CLOCK_REALTIME, &_it->second.endtime);
		}
		pthread_mutex_unlock(&lkup_pkt_sampling_mutex);
	}	  

	  rec_lookup_num++;
	}
	  delete recevPacket;
    }
  }
   incConnection_->closeConnection(); 
  delete  clientListAddress;
  delete  incConnection_;

  pthread_exit(NULL);
}
