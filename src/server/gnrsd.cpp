#include <unistd.h>
#include <stdio.h>
#include <map>
#include "ThreadPool.h"
#include "db_cxx.h"
#include "gnrsd.h"
//#include "../common/time_measure.h"
#include "../common/profiler/Timing.h"
#include "statistics.h"
ofstream ProcFile;
ofstream ProcFile_;
int rec_insert_num,rec_lookup_num,proc_insert_num,proc_lookup_num;
PKT_SAMPLE_MAP _pkt_sample;
int pool_size;
int serv_req_num;


unsigned long long _timestamp;
map<uint32_t, insert_msg_element*> * gnrsd::insert_table = NULL;
map<uint32_t,lookup_msg_element*> * gnrsd::lookup_table = NULL;

gnrsd::gnrsd():g_hm()
{
  prev_time_index=0;
  prev_index_num=0;
  total_time=0;
  cache = new guid_cache_t(1024);

}

gnrsd::~gnrsd(){
  delete cache;
}

int gnrsd::timingStat(int index,double time_)
{
  if(index==prev_time_index)  {
    prev_index_num++;
    total_time+=time_;
  }	
  else {
    ProcFile_<<prev_time_index<<" "<<total_time/prev_index_num<<endl;
    prev_time_index=index;
    prev_index_num=0;
    total_time=0;
  }
}


//return with the AS number when given a GUID
//GUID-->IP-->AS-->server node
string gnrsd::GUID2Server(char* GUID, uint8_t hashIndex)
{
  Hash128 h;
  u32b destIP = h.HashG2IP(GUID, hashIndex); 

  unsigned char iplookup[4];
  IPAddress addr;

  vector<u8b> temp=Common::num2ip(destIP);
  iplookup[0]=temp[0];
  iplookup[1]=temp[1];
  iplookup[2]=temp[2];
  iplookup[3]=temp[3];
  memcpy(addr.data(),iplookup,4);

  u8b tryNum = 0;
  asNum destAS = -1; 
  Cidr longestCidrPref; 
  //loop to find destination AS
  while ((tryNum <MAX_TRY_NUM) && (destAS == -1 )) {
    destAS = radixlookup->lookup_route(addr,addr);

#ifdef DEBUG
    if (DEBUG >=1){
      cout<<"destIP:"<<(unsigned int)iplookup[0]<<"."<<(unsigned int)iplookup[1]<<"."<<(unsigned int)iplookup[2]<<"."<<(unsigned int)iplookup[3]<<" for GUID: "<< GUID <<endl;
      cout<<"AS number:	" << destAS<< endl; 
    }
#endif

    if (destAS == -1){	//no prefix is found 
      destIP = h.HashIP2IP(destIP, hashIndex); 
      tryNum++; 	
      temp=Common::num2ip(destIP);
      iplookup[0]=temp[0];
      iplookup[1]=temp[1];
      iplookup[2]=temp[2];
      iplookup[3]=temp[3];
      memcpy(addr.data(),iplookup,4);
    }
  }		//end loop 

  if (destAS == -1) {   //no AS is found after MAX_TRY
    //find the closest prefix
    u32b minDistance = 4294967295U; 
    asNum asNumber = 0;
    cout<<"no AS found for GUID: "<<GUID<<endl; 
    //Walk through all prefixes PREFIX to find the closest prefix 
    for(u32b i=0; i<curr_prefix.entryList.size();i++){
      if ( minDistance > Common::ipDistanceIPtoPrefix(curr_prefix.entryList[i],destIP) ){
        minDistance = Common::ipDistanceIPtoPrefix(curr_prefix.entryList[i],destIP); 
        asNumber =  curr_prefix.entryList[i].orgin_AS; 
        longestCidrPref.prefix =  curr_prefix.entryList[i].prefix; 
        longestCidrPref.mask_bit =  curr_prefix.entryList[i].mask_bit; 
      }
    }
#ifdef DEBUG
    if (DEBUG >=1) cout << "used ipDistance !!! Distance is : "<< (unsigned int) minDistance << ", AS Number:  " << (unsigned int)asNumber << endl; 
#endif
    destAS = asNumber; 
  }
#ifdef DEBUG
  if (DEBUG >=1){
    cout<<"destIP:	" << (int)(Common::num2ip(destIP))[0]<<"."<<(int)(Common::num2ip(destIP))[1]<<"."<<(int)(Common::num2ip(destIP))[2]<<"."<<(int)(Common::num2ip(destIP))[3] << endl; 
    cout<<"AS Number:  " << (unsigned int)destAS << endl; 
  }
#endif
  return(h.MapAS2Server(destAS));
}

//async timer for the insert msg
void* gnrsd::InsertTimerProc(void *arg)  {

  while(1)  {
    usleep(WAKEUP_INTERVAL);
    struct timeval _cur_time;
    gettimeofday(&_cur_time, NULL);
    unsigned long long _cur_time_us = (unsigned long long)_cur_time.tv_sec*1000000 + _cur_time.tv_usec;

    map<uint32_t,insert_msg_element*>::iterator _it;
    for(_it = insert_table->begin(); _it != insert_table->end(); _it++)  {
#ifdef DEBUG
      if(DEBUG >= 2)
        cout<<"current time: "<<_cur_time_us<<" us, expiring time: "<<(*_it).second->expire_ts<<" us for req_id: "<<(*_it).first<<endl;
#endif

      //resend if expire
      if((*_it).second->expire_ts < _cur_time_us) {
        for(int i=0; i<K_NUM; i++)  
          if((*_it).second->_dstInfo[i].ack_flag == false)  {
            OutgoingConnection *GNRS_sport=new OutgoingConnection();
            GNRS_sport->init();

            Address * GNRS_server_sendtoaddr;
            GNRS_server_sendtoaddr = new Address((*_it).second->_dstInfo[i].dst_addr, (*_it).second->_dstInfo[i].dst_listen_port);
            GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);

#ifdef DEBUG
            if (DEBUG >=1){
              cout<<"timer fire! re-forward insert packet to IP:"<<(*_it).second->_dstInfo[i].dst_addr<<endl;
            }
#endif

            GNRS_sport->sendPack((*_it).second->pkt);

            delete GNRS_sport;
            delete GNRS_server_sendtoaddr;
          }
        //update the expiring timestamp
        (*_it).second->expire_ts = _cur_time_us + INSERT_TIMEOUT;
      }
    }
  }
}



//hash_ip: Hashed Server IP for INSERT
//FromServer: true: the msg is redirected from another gnrs server; false: the msg comes from a client
void gnrsd::insert_msg_handler(const char* hash_ip, HashMap& _hm, Packet* recvd_pkt, bool FromServer)
{
  common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();
  insert_message_t *ins = (insert_message_t*)recvd_pkt->getPayloadPointer();
  Address * GNRS_server_sendtoaddr;
  OutgoingConnection *GNRS_sport=new OutgoingConnection();
  GNRS_sport->init();

  if(strcmp(hash_ip,GNRSConfig::server_addr.c_str())==0)
  {
#ifdef DEBUG
    if (DEBUG >=1) cout << "Inserting GNRS locally." << endl;
#endif

    insert_handler( _hm, ins,0);

    //send ack to the server that forwards the insert msg
    if(FromServer == true)  {
      GNRS_server_sendtoaddr= new Address(hdr->sender_addr,ntohl(hdr->sender_listen_port));
      GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);

      insert_ack_message_t *ack = (insert_ack_message_t*)malloc(sizeof(insert_ack_message_t));
      strcpy(ack->c_hdr.sender_addr, GNRSConfig::server_addr.c_str());
      ack->c_hdr.req_id = ins->c_hdr.req_id;
      ack->c_hdr.type = INSERT_ACK;
      ack->c_hdr.sender_listen_port=htonl(GNRSConfig::daemon_listen_port+1);
      ack->resp_code = SUCCESS;

      Packet *p = new Packet();
      p->setPayload((char*)ack, sizeof(insert_ack_message_t));

      GNRS_sport->sendPack(p);
#ifdef DEBUG
      if (DEBUG >=1) cout<<"ACK FOR INSERT SENT sent to redirecting server!"<<endl;
#endif
      delete p;
      delete GNRS_server_sendtoaddr;
    }
  }
  else
  {
    //send insert packet to Hashed loc
    GNRS_server_sendtoaddr = new Address(hash_ip, GNRSConfig::daemon_listen_port+1);
    GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);

    //update the sender address to the gnrs server's address, will be used for insert-ack
    strcpy(hdr->sender_addr, GNRSConfig::server_addr.c_str());
    hdr->sender_listen_port = htonl(GNRSConfig::daemon_listen_port+1);

    GNRS_sport->sendPack(recvd_pkt);

#ifdef DEBUG
    if (DEBUG >=1){
      cout<<"forward insert packet to IP:"<<hash_ip<<endl;
      cout<<"packet type:"<<(int)((common_header_t*)recvd_pkt->getPayloadPointer())->type<<endl;
      cout<<"sender address:"<<((common_header_t*)recvd_pkt->getPayloadPointer())->sender_addr<<endl;
      cout<<"sender listen port:"<<ntohl(hdr->sender_listen_port)<<endl;
    }
#endif
    delete GNRS_server_sendtoaddr;

  }
  delete GNRS_sport;
}

guid_cache_t* gnrsd::getCache(){
  return this->cache;
}

//this is the working thread for insert pool: called by the listening thread
void gnrsd::global_INSERT_msg_handler(MsgParameter *gnrs_para)
{
  START_TIMING((char *)"gnrsd:global_insert_msg_handler");


  struct timeval _req_time;
  gettimeofday(&_req_time, NULL);

  Packet *recvd_pkt=gnrs_para->recvd_pkt;
  OutgoingConnection *GNRS_sport=new OutgoingConnection();
  GNRS_sport->init();
  if (DEBUG >=1) cout<<"insert packet received at GNRS"<<endl;
  insert_message_t *ins = (insert_message_t*)recvd_pkt->getPayloadPointer();
  /* CACHE CODE */
  gnrsd* server = gnrs_para->gnrs_daemon;
  guid_cache_t* cache = server->getCache();

  char* guid = ins->guid;
  uint16_t numNetAddrs = ins->na_num;
  NA* netAddrs = ins->NAs;
  value* someValue = new value;
  // Allocate the vectors needed
  someValue->locator = new vector<string*>();
  someValue->expire = new vector<unsigned int*>();
  someValue->weight = new vector<unsigned short*>();
  for(NA* someNetAddr = netAddrs; 
      someNetAddr < netAddrs+numNetAddrs; 
      ++someNetAddr){
    someValue->locator->push_back((new string(someNetAddr->net_addr)));
    unsigned int* expireTime = new unsigned int(_req_time.tv_sec +
        ntohl(someNetAddr->ttlMsec)/1000);
    someValue->expire->push_back(expireTime);
    unsigned short* weight = new unsigned short((ntohs(someNetAddr->weight)));
    someValue->weight->push_back(weight);
  }

  // Insert the new value into the cache.
  cache->insert(guid,someValue);

  /* END CACHE */

  if (DEBUG >=1) {
    cout <<"Mapping info in packet is : guid: " << ins->guid << " netaddr: "
      << ins->NAs[0].net_addr<<endl; 
  }

  string hashed_ip;

  //tell whether the destination AS for the GUID mapping has been computed or not
  if(ins->dest_flag==0)  {
    common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();

    //reply a insert-ack to the client who sends out the insert request
    OutgoingConnection *GNRS_sport=new OutgoingConnection();
    GNRS_sport->init();
    Address * GNRS_server_sendtoaddr;
    GNRS_server_sendtoaddr= new Address(hdr->sender_addr,ntohl(hdr->sender_listen_port));
    GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);
    insert_ack_message_t *ack = (insert_ack_message_t*)malloc(sizeof(insert_ack_message_t));
    strcpy(ack->c_hdr.sender_addr, GNRSConfig::server_addr.c_str());
    ack->c_hdr.req_id = ins->c_hdr.req_id;
    ack->c_hdr.type = INSERT_ACK;
    ack->c_hdr.sender_listen_port=htonl(GNRSConfig::daemon_listen_port+1);
    ack->resp_code = SUCCESS;

    Packet *p = new Packet();
    p->setPayload((char*)ack, sizeof(insert_ack_message_t));

    GNRS_sport->sendPack(p);
#ifdef DEBUG
    if (DEBUG >=1) cout<<"ACK FOR INSERT SENT to CLIENT!"<<endl;
#endif

    //insert cache for acknowledgement checking
    insert_msg_element* _temp = new insert_msg_element;
    //_temp->req_id = ntohl(hdr->req_id);
    _temp->pkt = recvd_pkt;
    _temp->expire_ts = (unsigned long long)_req_time.tv_sec*1000000 + _req_time.tv_usec + INSERT_TIMEOUT;
    _temp->ack_num = 0;

    uint32_t index = ntohl(hdr->req_id);
    insert_table->insert( pair<uint32_t,insert_msg_element*>(index,_temp) ); //TODO: need mutex here
#ifdef DEBUG
    cout<<"insert an entry into insert table with req_id: "<<index<<endl;
#endif

    //K-replica and calculate destination AS for each replica
    for(int i=0;i<K_NUM;i++)  {
      if(GNRSConfig::hash_func==0){
        Hash128 h;
        hashed_ip=h.HashG2Server(ins->guid, i);
      }
      else
        hashed_ip=gnrs_para->gnrs_daemon->GUID2Server(ins->guid, i);
      ins->dest_flag=1;
      const char * hash_ip = hashed_ip.c_str(); 

      strcpy(_temp->_dstInfo[i].dst_addr, hash_ip);
      _temp->_dstInfo[i].dst_listen_port = GNRSConfig::daemon_listen_port+1;  //TODO: the dst listen port might be a variable when multiple gnrs server running on the same node
      if(strcmp(hash_ip,GNRSConfig::server_addr.c_str())==0)  {
        _temp->_dstInfo[i].ack_flag = true;  //no ack is needed if it will be inserted locally
        _temp->ack_num++;
      }
      else
        _temp->_dstInfo[i].ack_flag = false;


      if (DEBUG >=1)    cout<<"Hashed Server IP for INSERT: " << hashed_ip<<endl;
      insert_msg_handler(hash_ip, gnrs_para->gnrs_daemon->g_hm, recvd_pkt, false);
    }

    delete p;
    delete GNRS_server_sendtoaddr;
    delete GNRS_sport;

    //when all replicas are stored locally, directly remove the entry in the insert table. normally only take place when K_NUM=1
    if(_temp->ack_num == K_NUM)  {
#ifdef DEBUG
      cout<<"remove an entry in the insert table with req_id: "<<index<<endl;
#endif

      delete((*insert_table)[index]->pkt);
      delete((*insert_table)[index]);
      insert_table->erase(index);
    }
  }
  else	{
    hashed_ip=GNRSConfig::server_addr;

    if (DEBUG >=1)    cout<<"Hashed Server IP for INSERT: " << hashed_ip<<endl; 
    insert_msg_handler(hashed_ip.c_str(), gnrs_para->gnrs_daemon->g_hm, recvd_pkt, true);

    delete recvd_pkt;
  }

  REGISTER_TIMING((char *)"gnrsd:global_insert_msg_handler");


  if(SAMPLING==1)  {
    pthread_mutex_lock(&ins_pkt_sampling_mutex);
    proc_insert_num++;
    pthread_mutex_unlock(&ins_pkt_sampling_mutex);
  }

  //the received pkt will be removed when all ack(s) are received.
  //delete(recvd_pkt);
  delete gnrs_para;
  return (void)0;
}

//hash_ip: the ip address for the server that will serve the lookup request
void gnrsd::lookup_msg_handler(const char* hash_ip, HashMap& _hm, Packet* recvd_pkt)
{
  common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();
  lookup_message_t *lkup = (lookup_message_t*)recvd_pkt->getPayloadPointer();
  OutgoingConnection *GNRS_sport=new OutgoingConnection();
  GNRS_sport->init();
  Address * GNRS_server_sendtoaddr;

  Packet *p;

  if(strcmp(hash_ip,GNRSConfig::server_addr.c_str())==0)
  {
    if (DEBUG >=1) cout << "LOOKing up GNRS locally" << endl;
    lookup_response_message_t *resp;

    lookup_handler(_hm,lkup,resp,0);

    GNRS_server_sendtoaddr= new Address(hdr->sender_addr, ntohl(hdr->sender_listen_port));
    GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);

    p = new Packet();
    p->setPayload((char*)resp, sizeof(lookup_response_message_t)+ntohs(resp->na_num)*sizeof(NA));
    GNRS_sport->sendPack(p);

#ifdef DEBUG
    if (DEBUG >=1) cout<<"lookup response packet sent from GNRS"<<endl;
    if (DEBUG >=1){
      cout<<"packet req_id:"<<ntohl(((common_header_t*)p->getPayloadPointer())->req_id)<<endl;
      cout<<"packet type:"<<(int)((common_header_t*)p->getPayloadPointer())->type<<endl;
      cout<<"number of locators in the packet:"<<ntohs(((lookup_response_message*)p->getPayloadPointer())->na_num)<<endl;
      for(int i=0;i<ntohs(((lookup_response_message*)p->getPayloadPointer())->na_num);i++)
        cout<<"locator "<<i+1<<":"<<((lookup_response_message*)p->getPayloadPointer())->NAs[i].net_addr<<endl;
    }
#endif
    delete p;
  }
  else
  {
    //send lookup packet to Hashed location
    GNRS_server_sendtoaddr = new Address(hash_ip,GNRSConfig::daemon_listen_port+1);
    GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);

    //add entry to lookup table
    lookup_msg_element* _temp = new lookup_msg_element;
    strcpy(_temp->src_addr, hdr->sender_addr);
    _temp->src_listen_port = ntohl(hdr->sender_listen_port);
    lookup_table->insert( pair<uint32_t,lookup_msg_element*>(ntohl(hdr->req_id),_temp) ); //TODO: need mutex here

    //update the sender address to the gnrs server's address, will be used for lookup response
    strcpy(hdr->sender_addr, GNRSConfig::server_addr.c_str());
    hdr->sender_listen_port = htonl(GNRSConfig::daemon_listen_port+1);

#ifdef DEBUG
    if (DEBUG >=1)    {
      cout << "Forwarding lookup: pkt type: " << (int)hdr->type << endl;
      cout<<"sender listen port:"<<ntohl(hdr->sender_listen_port)<<endl;
    }
#endif

    GNRS_sport->sendPack(recvd_pkt);
  }

  delete GNRS_server_sendtoaddr;
  delete GNRS_sport;

}


//this is the working thread for lookup pool: called by the listening thread
void gnrsd::global_LOOKUP_msg_handler(MsgParameter *gnrs_para)
{
  START_TIMING((char *)"gnrsd:global_lookup_msg_handler");

  //START_TIMING("gnrsd:preprocess");
  Packet *recvd_pkt=gnrs_para->recvd_pkt;
  common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();
  if (DEBUG >=1) cout<<"Packet Recieved for Lookup at GNRS"<<endl;
  //* Handle Lookup Packet */
  lookup_message_t *lkup = (lookup_message_t*)recvd_pkt->getPayloadPointer();

  /* CACHE CODE */
  gnrsd* server = gnrs_para->gnrs_daemon;
  guid_cache_t* cache = server->getCache();
  char* guid = lkup->guid;
  const value* cachedValue = NULL;
  cachedValue = cache->fetch(guid);

  bool cacheMiss = false;
  // Got a cache entry, so check expiry
  if(cachedValue != NULL){
    struct timeval _cur_time;
    gettimeofday(&_cur_time, NULL);
    string* netAddr;
    unsigned int expire = 0;
    unsigned short* weight = 0;
    vector<string*>::iterator netIter = cachedValue->locator->begin();
    vector<unsigned int*>::iterator expireIter = cachedValue->expire->begin();
    vector<unsigned short*>::iterator weightIter = cachedValue->weight->begin();
    vector<NA> naVector;
    // Step through each cached value
    for(; netIter != cachedValue->locator->end(); netIter++){
      expire = **expireIter;

      if(expire > _cur_time.tv_sec){
        cacheMiss = true;
      }else {
        // Prep some output for the response message
        struct NA newNA;
        strncpy(newNA.net_addr,(*netIter)->c_str(),SIZE_OF_NET_ADDR);
        newNA.ttlMsec = (uint32_t)(htonl(**expireIter));
        newNA.weight = (uint16_t)(htons(**weightIter));
        naVector.push_back(newNA);
      }
      

      // Increment for next round
      expireIter++;
      weightIter++;
    }
    // Send the packet
    if(!naVector.empty()){
      // Build a response message
      int responseLength =
        sizeof(lookup_response_message_t)+naVector.size()*sizeof(NA);
      lookup_response_message_t* responseMsg =
        (lookup_response_message_t*)
          malloc(responseLength);

      // Build the common header info
      responseMsg->c_hdr.req_id = hdr->req_id;
      responseMsg->c_hdr.type = LOOKUP_RESP;
      responseMsg->c_hdr.sender_listen_port = htonl(GNRSConfig::daemon_listen_port);

      // Indicate a success
      // TODO: Return CACHED response code
      responseMsg->resp_code = htons(SUCCESS);
      // Number of NAs in message
      responseMsg->na_num = htonl(naVector.size());
      // Step through each NA and put it in the response
      int i = 0;
      while(i < responseMsg->na_num){
        responseMsg->NAs[i] = naVector[i];
        ++i;
      }

      // Allocate a socket to send the response message
      OutgoingConnection sendSocket;
      sendSocket.init();
      // Grab the address from the request
      Address dstAddress(hdr->sender_addr,ntohl(hdr->sender_listen_port));
      sendSocket.setRemoteAddress(&dstAddress);
      
      // Create a packet to send the message in
      Packet p;
      p.setPayload((char*)responseMsg,responseLength);

      // Send the packet
      sendSocket.sendPack(&p);
      delete responseMsg;
    }
  }else{
    cacheMiss = true;
  }
  
  if(!cacheMiss){
    // TODO: Use cached value
    delete(recvd_pkt);
    delete gnrs_para;
    return;
  }

  /* END CACHE */

  //tell whether the destination AS for the GUID mapping has been computed or not
  if(lkup->dest_flag==0)  {
    string hashed_ip[K_NUM];
    for(int i=0;i<K_NUM;i++)  {
      if(GNRSConfig::hash_func==0){				
        Hash128 h;
        hashed_ip[i]=h.HashG2Server(lkup->guid, i);  
      }
      else
        hashed_ip[i]=gnrs_para->gnrs_daemon->GUID2Server(lkup->guid, i);
    }
    lkup->dest_flag==1;
    if (DEBUG >=1)    cout<<"Hashed Server IP for LOOKUP: " << hashed_ip[0] <<endl;
    lookup_msg_handler(hashed_ip[0].c_str(), gnrs_para->gnrs_daemon->g_hm, recvd_pkt);
  }
  else  {
    string hashed_ip=GNRSConfig::server_addr;

    if (DEBUG >=1)    cout<<"Hashed Server IP for LOOKUP: " << hashed_ip<<endl; 
    lookup_msg_handler(hashed_ip.c_str(), gnrs_para->gnrs_daemon->g_hm, recvd_pkt);
  }

  double sample_time=REGISTER_TIMING((char *)"gnrsd:global_lookup_msg_handler");

  uint32_t _req_id;
  if(SAMPLING==1) _req_id=ntohl(hdr->req_id);

  if(SAMPLING==1)  {
    pthread_mutex_lock(&lkup_pkt_sampling_mutex);
    proc_lookup_num++;
    if(proc_lookup_num%STAT_STEP<STAT_RANGE) gnrs_para->gnrs_daemon->timingStat(proc_lookup_num-proc_lookup_num%STAT_STEP,sample_time);
    PKT_SAMPLE_MAP::iterator _it=_pkt_sample.find(_req_id);
    if(_it!=_pkt_sample.end())  {
      clock_gettime(CLOCK_REALTIME, &_it->second.endtime);
    }  
    pthread_mutex_unlock(&lkup_pkt_sampling_mutex);
  }

  delete(recvd_pkt);
  delete gnrs_para;
  return (void)0;
}


//this is the working thread for insert ack pool: called by the listening thread
void gnrsd::global_INSERT_ACK_handler(MsgParameter *msg_para)
{
  Packet *recvd_pkt=msg_para->recvd_pkt;
  common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();

  uint32_t index = ntohl(hdr->req_id);

  if(insert_table->find(index)!= insert_table->end())  {
    if((*insert_table)[index]->ack_num == K_NUM-1)  {
#ifdef DEBUG
      cout<<"all insert ack received for req_id: " << index<<endl;
#endif
      delete((*insert_table)[index]->pkt);
      delete((*insert_table)[index]);
      insert_table->erase(index);
    }
    else  {
#ifdef DEBUG
      cout<<"insert ack received for req_id: " << index<<"from server: "<<hdr->sender_addr<<endl;
#endif

      (*insert_table)[index]->ack_num++;
      int i;
      for (i=0;i<K_NUM;i++)  {
        if(strcmp((*insert_table)[index]->_dstInfo[i].dst_addr,hdr->sender_addr)==0)
          break;
      }
      (*insert_table)[index]->_dstInfo[i].ack_flag = true;
    }
  }
  else
    cout<<"duplicate insert ack received for req_id: "<< index<<endl;

  delete(recvd_pkt);
}


//this is the working thread for lookup response pool: called by the listening thread
//it will check the lookup_table to retrieve the client network address and listening port number which it can use for forwarding
void gnrsd::global_LOOKUP_RESP_handler(MsgParameter *msg_para)
{
  Packet *recvd_pkt=msg_para->recvd_pkt;
  common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();

  uint32_t index = ntohl(hdr->req_id);

#ifdef DEBUG
  cout<<"lookup response received for req_id: " << index<<"from server: "<<hdr->sender_addr<<endl;
#endif

  Address * GNRS_server_sendtoaddr;
  OutgoingConnection *GNRS_sport=new OutgoingConnection();
  GNRS_sport->init();

  GNRS_server_sendtoaddr = new Address((*lookup_table)[index]->src_addr, (*lookup_table)[index]->src_listen_port);
  GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);

  GNRS_sport->sendPack(recvd_pkt);
#ifdef DEBUG
  cout<<"forward pkt to address: " << (*lookup_table)[index]->src_addr <<"and port number: " << (*lookup_table)[index]->src_listen_port << endl;
#endif

  delete (*lookup_table)[index];
  lookup_table->erase(index);

  delete GNRS_sport;
  delete GNRS_server_sendtoaddr;
  delete recvd_pkt;
}


/*
 *   GNRS RECEIVER SCRIPT :
 *   Receive insert and perform put : type 0
 *   Recieve lookup and perform get : type 1
 *   Recieve insert ack: type 2
 *   Recieve lookup response: type 3
 */
int gnrsd::g_receiver()
{

  insert_table = new map<uint32_t, insert_msg_element*>;
  lookup_table = new map<uint32_t, lookup_msg_element*>;
  //GNRSConfig::daemon_listen_port is previously reserved for LNRS while GNRSConfig::daemon_listen_port+1 for GNRS
  GNRS_server_raddr = new Address(GNRSConfig::server_addr.c_str(), GNRSConfig::daemon_listen_port+1); 
  my_global_rport = new IncomingConnection();
  my_global_rport->setLocalAddress(GNRS_server_raddr);
  my_global_rport->init();

  cout<<"GNRS : Receiver started"<<endl;

  pthread_t AsncTimingThread;
  if(pthread_create(&AsncTimingThread, NULL, &InsertTimerProc, NULL))  {
    cout<<"Error creating the async timing thread, aborting"<<endl;
    return(-1);
  }

  ThreadPool<MsgParameter*> insert_pool (global_INSERT_msg_handler, 
      pool_size, //minimum threads
      pool_size, //maximum threads
      ThreadPool<MsgParameter*>::UnlimitedLifetime  //thread lifetime
      );

  ThreadPool<MsgParameter*> lookup_pool (global_LOOKUP_msg_handler, 
      pool_size, 
      pool_size, 
      ThreadPool<MsgParameter*>::UnlimitedLifetime
      );

  ThreadPool<MsgParameter*> insert_ack_pool (global_INSERT_ACK_handler,
      pool_size,
      pool_size,
      ThreadPool<MsgParameter*>::UnlimitedLifetime
      );

  ThreadPool<MsgParameter*> lookup_resp_pool (global_LOOKUP_RESP_handler,
      pool_size,
      pool_size,
      ThreadPool<MsgParameter*>::UnlimitedLifetime
      );

  //Packet *recvd_pkt;
  common_header_t *hdr;
  MsgParameter *gnrs_para;
  int i=0,j=0;
  int thres;
  if(serv_req_num>0)
    thres=serv_req_num;
  else
    thres=-serv_req_num; 
  while(i<thres){
    try{
      recvd_pkt = my_global_rport->receivePacketDirectly();					

      gnrs_para=new MsgParameter;
      gnrs_para->recvd_pkt=recvd_pkt;
      gnrs_para->gnrs_daemon=this;

      if(SAMPLING==1&&i==0&&j==0)  {
        startStatistics(0.1);		
        j++;
      }
      if(recvd_pkt != NULL)
      {    
        hdr = (common_header_t*)recvd_pkt->getPayloadPointer();

        if(SAMPLING==1&&rec_lookup_num%1000==1)  {  //potential bug lies: here we assume if a lookup pkt comes, the following pkts are all lookup. but the packet might be insert packet, don't need to be counted.
          uint32_t _req_id=ntohl(hdr->req_id);
          pthread_mutex_lock(&lkup_pkt_sampling_mutex);
          clock_gettime(CLOCK_REALTIME, &_pkt_sample[_req_id].starttime);
          _pkt_sample[_req_id].endtime.tv_sec=0;
          _pkt_sample[_req_id].endtime.tv_nsec=0;
          pthread_mutex_unlock(&lkup_pkt_sampling_mutex);
        }

        //int flag1=1;
#ifdef DEBUG
        if (DEBUG >=1) {
          cout<<"packet request ID:"<<ntohl(hdr->req_id)<<endl;
          cout<<"packet type:"<<(int)hdr->type<<endl;
          cout<<"sender address:"<<hdr->sender_addr<<endl;       
          cout<<"sender listen port:"<<ntohl(hdr->sender_listen_port)<<endl;       
        }
#endif
        //ProcLagFile<<ntohl(hdr->req_id)<<' ';

        // gnrs_condition.condition_set = true;
        if(hdr->type==INSERT)  {
          insert_pool.Launch(gnrs_para);
          rec_insert_num++;
        }
        else if(hdr->type == LOOKUP) {
          lookup_pool.Launch(gnrs_para);
          rec_lookup_num++;
        }
        else if(hdr->type == INSERT_ACK)  {
          insert_ack_pool.Launch(gnrs_para);			    
        }
        else if(hdr->type == LOOKUP_RESP)  {
          lookup_resp_pool.Launch(gnrs_para);
        }
      }
      if(serv_req_num>0) i++;
    }

    catch(const char *reason)
    {
      cout<<"exception in the GNRS Receiver :"<<reason<<endl;

    }

  }
  //cout<<"DEBUG: get here!"<<endl;
  sleep(120);  //PAY ATTENTION: should sleep for enough time here to empty the parameter queue.
  pthread_mutex_destroy(&mysql_mutex);
  pthread_mutex_destroy(&gnrs_condition.mutex);
  pthread_cond_destroy(&gnrs_condition.condition);
  delete(GNRS_server_raddr);
  delete(my_global_rport);
  return 0;

}  // end of GNRS receiver




void gnrsd::read_prefix_table(const char *pref_Filename)
{
  //build prefix table data structure
  u64b act_time=0;
  curr_prefix.init(pref_Filename,act_time); 
  cout <<"Number of Prefix in prefix table: "<<curr_prefix.entryList.size()<<endl;  


  //build radixIPlookup data structure used for longest prefix matching
  IPRoute  route;
  IPRoute *root;

  radixlookup = new RadixIPLookup();

  IPAddress addr;
  IPAddress mask;

  FILE *fd;
  fd = fopen ( pref_Filename,"a+" );
  char buffer[100];
  char temp[50];
  int i;
  char port[10];
  int _port;
  char _mask[4];
  char _ip[4];
  while (fgets (buffer,50,fd) != NULL){
    for (i = 0; i < strlen(buffer); i++)
      if(buffer[i] == ' ')	break;

    strncpy(temp,buffer,i);
    temp[i] = '\0';

    int j;
    for (j=0; j < strlen(buffer) - i;j++){
      port[j] = buffer[i + 1 + j];

    }
    _port = atoi(port);
    makemask(temp,_mask,_ip);

    memcpy(addr.data(),_ip,4);
    memcpy(mask.data(),_mask,4);
    route.addr = addr;
    route.mask = mask;
    route.port = _port;

    radixlookup->add_route(route,true,root,NULL);     
  }
}  


void* gnrsd::read_server_list()
{
  int count=0;
  string line;
  ifstream myfile(GNRSConfig::servers_list_file.c_str());
  cout << "Reading servers list from file: " << GNRSConfig::servers_list_file << endl;
  if(myfile.is_open())
  {
    cout << "Initial Count: " << count << endl; 

    //while(myfile.good())
    while(!myfile.eof() && getline(myfile,line))
    {
      server_list.push_back(line);
      count++;
      cout << "Read server list entry: " << line << " count: " << count << endl;
    }
  }
  cout << "Number of entries in servers list: " << count << endl; 

}


/**
 * Update in-memory structure with mappings from persistent store
 *
 */
void gnrsd::read_mappings_from_store(){

  Driver *driver = get_driver_instance();
  /* build db url for connection */
  char db_url[200];
  sprintf(db_url, "tcp://%s:%d/%s", GNRSConfig::db_host.c_str(), GNRSConfig::db_port, GNRSConfig::db_name.c_str());
  Connection *con = driver->connect(db_url, GNRSConfig::db_user, GNRSConfig::db_passwd);
  stmt = con->createStatement();

  /* 
   * read mappings in batches of batch_size and update in-memory
   * data structures 
   */
  int batch_size = 1000;
  int i = 0; /* row index */
  while(1){
    char buff[10];
    string query("SELECT * FROM global_guid_locators_map LIMIT ");
    sprintf(buff, "%d", i);
    query += buff;
    query += ",";
    sprintf(buff, "%d", batch_size);
    query += buff;
    cout << "execing global query:" << query << endl;
    ResultSet *rs = stmt->executeQuery(query);
    int read_cnt = 0; 
    while(rs->next())
    {
      read_cnt++;
      string guid = rs->getString("guid");
      string* locators = new string(rs->getString("locators"));
      string* TTL=new string(rs->getString("TTLs"));
      string* weights=new string(rs->getString("weights"));

      vector<string*> *v = new vector<string*>();
      vector<unsigned int*>* expires=new vector<unsigned int*>;
      vector<unsigned short*>* weights_=new vector<unsigned short*>;

      string temp_locator,temp_ttl,temp_weight;
      stringstream _ss_locator(*locators);
      stringstream _ss_ttl(*TTL);
      stringstream _ss_weight(*weights);
      while(getline(_ss_locator,temp_locator,' '))  {
        getline(_ss_ttl,temp_ttl,' ');
        getline(_ss_weight,temp_weight,' ');
        expires->push_back(new unsigned int(atoi(temp_ttl.c_str()))); //reading expiring timestamp
        weights_->push_back(new unsigned short(atoi(temp_weight.c_str())));
        v->push_back(new string(temp_locator)); 
      }
      g_hm.put(guid, v,expires,weights_);
      cout<<"guid="<<guid.c_str()<<",locators="<<v->at(0)->c_str()<<endl;
      delete locators;
      delete TTL;
      delete weights;
    }
    i += read_cnt;
    /* if rows fewer than batch size were read, we're done */
    if(read_cnt < batch_size)break;
  }
  cout << "Read in " << i << " mappings from global store " << endl;
}



/****************************************************/
/*
   initialize the MASK array
   (UTILITY FUNCTION)
 */
void gnrsd::initMASK(){
  u32b tmp =0;	 
  for (u8b i=0; i<32; i++){
    u32b tmp2 = (1); 
    tmp2 = tmp2 << (31-i) ; 
    tmp = tmp2 | tmp;
    if (DEBUG >=2) cout << tmp <<endl; 
    MASK[i]=tmp; 
  }
}



void gnrsd::print_usage()
{
  cout << "Usage: ./gnrsd <config file> [ <thread_pool_size> <service_req_num> <server_self_addr>] [servers_list_file ]" << endl;
}


// we have three output files here:
// gnrs_proc_statistics.data: print out the number of insert and lookup pkts processed per 0.1s
// gnrs_time_statistics.data: print out the processing time of global_lookup_handler for certain sampled pkt.
// pkt_sampling_output.data: print out the total service time of sampled pkt
int main(int argc,const char * argv[]) {

  gnrsd gnrsd;
  if(argc < 2){
    gnrsd.print_usage();
    exit(0);
  }
  if(SAMPLING==1) {
    ProcFile.open("/var/log/gnrs_proc_statistics.data"); 
    if (!ProcFile.is_open()){
      cerr << "Can't open OUTPUT gnrs statistics File !!! Returning..." <<endl;
      return 1;
    } 

    ProcFile_.open("/var/log/gnrs_time_statistics.data"); 
    if (!ProcFile_.is_open()){
      cerr << "Can't open OUTPUT gnrs statistics File !!! Returning..." <<endl;
      return 1;
    }
  }

  rec_insert_num=rec_lookup_num=0;
  proc_insert_num=proc_lookup_num=0;

  const char* conf_filename = argv[1];
  /**
   * Read configuration settings from file and update any
   * default settings.
   *
   * Exits program with error code either on i/o or parse errors while
   * reading the file, or when a required setting hasn't been specified
   */
  GNRSConfig::init_defaults();
  GNRSConfig::read_from_file(conf_filename);

  if(argc > 2) {
    char _pool_size[5];
    strcpy(_pool_size,argv[2]);
    pool_size=atoi(_pool_size);
    if(DEBUG>=1) cout<<"pool_size: "<<pool_size<<endl;
  }
  else
    pool_size = GNRSConfig::thread_pool_size;

  if(argc > 3) {
    char _serv_req_num[10];
    strcpy(_serv_req_num,argv[3]);
    serv_req_num=atoi(_serv_req_num);
    if(DEBUG>=1) cout<<"server terminate after receiving: "<<serv_req_num<<endl;
  }
  else
    serv_req_num = GNRSConfig::service_req_num;

  /**
   * Override server self address from config with command line param
   * if specified
   */
  if(argc > 4){
    GNRSConfig::server_addr = argv[4];
  }

  /* override server list file */
  if(argc > 5){
    GNRSConfig::servers_list_file = argv[5];
  }

  pthread_mutex_init(&mysql_mutex,NULL);
  pthread_mutex_init(&lkup_pkt_sampling_mutex,NULL);
  pthread_mutex_init(&ins_pkt_sampling_mutex,NULL);
  pthread_cond_init( &(gnrs_condition.condition) , NULL);
  pthread_mutex_init( &(gnrs_condition.mutex), NULL);
  gnrs_condition.condition_set = false;

  gnrsd.read_server_list();

  /* bring in any guid-locator mappings stored from prior execution */
  gnrsd.read_mappings_from_store();

  if(GNRSConfig::hash_func==1){
    gnrsd.initMASK();
    gnrsd.read_prefix_table("/usr/local/mobilityfirst/code/prototype/gnrsd/src/server/prefix2_1_11.data");
  }

  DECLARE_TIMING_THREAD((char *)"tester");

  gnrsd.g_receiver();

  if(SAMPLING==1)	{
    ProcFile.close();
    ProcFile_.close();
    sampling_output();
  }

  pthread_mutex_destroy(&lkup_pkt_sampling_mutex);
  pthread_mutex_destroy(&ins_pkt_sampling_mutex);

  cout<<"GNRS server terminate!"<<endl;
  return 0;
}

