#include <unistd.h>
#include <stdio.h>
#include <map>
#include "ThreadPool.h"
#include "db_cxx.h"
#include "GNRS_daemon.h"
//#include "../common/time_measure.h"
#include "../common/profiler/Timing.h"
#include "statistics.h"
//struct timeval starttime,endtime,starttime_,endtime_;
//struct timespec starttime,endtime,starttime_,endtime_;
ofstream ProcFile;
ofstream ProcFile_;
int rec_insert_num,rec_lookup_num,proc_insert_num,proc_lookup_num;
PKT_SAMPLE_MAP _pkt_sample;
int pool_size;
int serv_req_num;


unsigned long long _timestamp;

GNRS_daemon::GNRS_daemon():g_hm()
{
	prev_time_index=0;
	prev_index_num=0;
	total_time=0;

/*        DbEnv* env=new DbEnv(DB_CXX_NO_EXCEPTIONS);
        Db* pdb;

        int ret=env.open(NULL,DB_CREATE | DB_INIT_LOCK | DB_INIT_MPOOL | DB_PRIVATE, 0);
        if (ret != 0) {
                cout<<"db environment open fails!"<<endl;
                exit(0);
        }

        pdb=new Db(&env, DB_CXX_NO_EXCEPTIONS);
        pdb->open(NULL,NULL,NULL,DB_BTREE,DB_CREATE | DB_THREAD, 0);

	g_hm(env,pdb);
*/
}

int GNRS_daemon::timingStat(int index,double time_)
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
string GNRS_daemon::GUID2Server(char* GUID, uint8_t hashIndex)
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

		if (DEBUG >=1){
			cout<<"destIP:"<<(unsigned int)iplookup[0]<<"."<<(unsigned int)iplookup[1]<<"."<<(unsigned int)iplookup[2]<<"."<<(unsigned int)iplookup[3]<<" for GUID: "<< GUID <<endl;
			cout<<"AS number:	" << destAS<< endl; 
		}

		if (destAS == -1){	//no prefix is found 
			destIP = h.HashIP2IP(destIP); 
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
		if (DEBUG >=1) cout << "used ipDistance !!! Distance is : "<< (unsigned int) minDistance << ", AS Number:  " << (unsigned int)asNumber << endl; 
		destAS = asNumber; 
	}
	if (DEBUG >=1){
		cout<<"destIP:	" << (int)(Common::num2ip(destIP))[0]<<"."<<(int)(Common::num2ip(destIP))[1]<<"."<<(int)(Common::num2ip(destIP))[2]<<"."<<(int)(Common::num2ip(destIP))[3] << endl; 
		cout<<"AS Number:  " << (unsigned int)destAS << endl; 
		}
	return(h.MapAS2Server(destAS));
}



void GNRS_daemon::global_INSERT_packet_handler(GNRS_Para *gnrs_para)
{
START_TIMING("GNRS_daemon:global_insert_packet_handler");	
	/*
	pthread_mutex_lock( &(gnrs_condition.mutex) );
	Packet *recvd_pkt=gnrs_para->recvd_pkt;
	gnrs_condition.condition_set = false;
	pthread_cond_signal(&(gnrs_condition.condition));
	pthread_mutex_unlock( &(gnrs_condition.mutex) );
	*/	

	Packet *recvd_pkt=gnrs_para->recvd_pkt;
	common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();
	OutgoingConnection *GNRS_sport=new OutgoingConnection();
        GNRS_sport->init();
       if (DEBUG >=1) cout<<"insert packet received at GNRS"<<endl;
	insert_message_t *ins = (insert_message_t*)recvd_pkt->getPayloadPointer();
	if (DEBUG >=1)    cout <<"Mapping info in packet is : guid: " << ins->guid << " netaddr: " <<ins->NAs[0].net_addr<<endl;

	string hashed_ip;

	//tell whether the destination AS for the GUID mapping has been computed or not
	if(ins->dest_flag==0)  {
		if(GNRSConfig::hash_func==0){
			  Hash128 h;
		         hashed_ip=h.HashG2Server(ins->guid);
			}
		else
	       		hashed_ip=gnrs_para->gnrs_daemon->GUID2Server(ins->guid);
		ins->dest_flag=1;
	}
	else
		hashed_ip=GNRSConfig::server_addr;
	
	if (DEBUG >=1)    cout<<"Hashed Server IP for INSERT: " << hashed_ip<<endl; 
        const char * hash_ip = hashed_ip.c_str(); 
        Address * GNRS_server_sendtoaddr;

	if(strcmp(hash_ip,GNRSConfig::server_addr.c_str())==0)
               {  							
			if (DEBUG >=1) cout << "Inserting GNRS locally." << endl;
                              	 //cout<<"Reached in here"<<endl;   
            	  	 //GNRS_server_sendtoaddr= new Address(hdr->sender_addr,GNRSConfig::client_listen_port);
			 GNRS_server_sendtoaddr= new Address(hdr->sender_addr,ntohl(hdr->sender_listen_port));
            	  	 GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);
							 
			insert_handler( gnrs_para->gnrs_daemon->g_hm, ins,0);
			 
			 insert_ack_message_t *ack = (insert_ack_message_t*)malloc(sizeof(insert_ack_message_t));
			 strcpy(ack->c_hdr.sender_addr, GNRSConfig::server_addr.c_str());
			 ack->c_hdr.req_id = ins->c_hdr.req_id;
			 ack->c_hdr.type = INSERT_ACK;
			 ack->c_hdr.sender_listen_port=htonl(GNRSConfig::daemon_listen_port+1);
			 ack->resp_code = SUCCESS;

			 Packet *p = new Packet();
			 p->setPayload((char*)ack, sizeof(insert_ack_message_t)); 

             		 GNRS_sport->sendPack(p);
                    	 if (DEBUG >=1) cout<<"ACK FOR INSERT SENT"<<endl;
			 delete p;
                 	  } // end of tyep 0 pkt handler
           else
                      {
                              //send insert packet to Hashed loc
                             //GNRS_server_sendtoaddr = new Address(hash_ip,7000);
                             GNRS_server_sendtoaddr = new Address(hash_ip, GNRSConfig::daemon_listen_port+1); 
                             GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);

                             GNRS_sport->sendPack(recvd_pkt);

				if (DEBUG >=1){
					cout<<"forward insert packet to IP:"<<hash_ip<<endl;
					cout<<"packet type:"<<(int)((common_header_t*)recvd_pkt->getPayloadPointer())->type<<endl;
					cout<<"sender address:"<<((common_header_t*)recvd_pkt->getPayloadPointer())->sender_addr<<endl; 
					cout<<"sender listen port:"<<ntohl(hdr->sender_listen_port)<<endl;       
					}

                      }
	delete GNRS_server_sendtoaddr;	
	delete GNRS_sport;
	delete(recvd_pkt);
REGISTER_TIMING("GNRS_daemon:global_insert_packet_handler");


        if(SAMPLING==1)  {
                //uint32_t _req_id=ntohl(hdr->req_id);
                pthread_mutex_lock(&ins_pkt_sampling_mutex);
                proc_insert_num++;
                pthread_mutex_unlock(&ins_pkt_sampling_mutex);
        }

	delete gnrs_para;
    return (void)0;
}


void GNRS_daemon::global_LOOKUP_packet_handler(GNRS_Para *gnrs_para)
{
	//int local_lookup_num=lookup_num;
START_TIMING("GNRS_daemon:global_lookup_packet_handler");
	//clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime);
/*
START_TIMING("GNRS_daemon:mutex");
	pthread_mutex_lock( &(gnrs_condition.mutex) );
	Packet *recvd_pkt=gnrs_para->recvd_pkt;
	gnrs_condition.condition_set = false;
	pthread_cond_signal(&(gnrs_condition.condition));
	pthread_mutex_unlock( &(gnrs_condition.mutex) );
REGISTER_TIMING("GNRS_daemon:mutex");
*/	
/*
        for(int j=0;j<1000000;j++)  {
                asm volatile (".byte 0x0f, 0x31" : "=A" (_timestamp));
        }
*/

	//START_TIMING("GNRS_daemon:preprocess");
	Packet *recvd_pkt=gnrs_para->recvd_pkt;
	common_header_t *hdr=(common_header_t*)recvd_pkt->getPayloadPointer();
	OutgoingConnection *GNRS_sport=new OutgoingConnection();
        GNRS_sport->init();
		if (DEBUG >=1) cout<<"Packet Recieved for Lookup at GNRS"<<endl;
                        	//* Handle Lookup Packet */
		lookup_message_t *lkup = (lookup_message_t*)recvd_pkt->getPayloadPointer();

		string hashed_ip;

		//tell whether the destination AS for the GUID mapping has been computed or not
		if(lkup->dest_flag==0)  {
			if(GNRSConfig::hash_func==0){				
		              Hash128 h;
		              hashed_ip=h.HashG2Server(lkup->guid);  
				}
			else
				hashed_ip=gnrs_para->gnrs_daemon->GUID2Server(lkup->guid);
			lkup->dest_flag==1;
		}
		else
			hashed_ip=GNRSConfig::server_addr;
			  
              const char * hash_ip = hashed_ip.c_str();
            	if (DEBUG >=1)    cout<<"Hashed Server IP for LOOKUP: " << hashed_ip<<endl; 
              Address * GNRS_server_sendtoaddr;
		
		Packet *p;
	//REGISTER_TIMING("GNRS_daemon:preprocess");

	//START_TIMING("GNRS_daemon:usleep");
	//usleep(20);
	//REGISTER_TIMING("GNRS_daemon:usleep");

                 if(strcmp(hash_ip,GNRSConfig::server_addr.c_str())==0)
                  { 
			 if (DEBUG >=1) cout << "LOOKing up GNRS locally" << endl;
			  lookup_response_message_t *resp;
			  
			  //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime_);
			  //START_TIMING("GNRS_daemon:lookup_handler");
	      		  lookup_handler(gnrs_para->gnrs_daemon->g_hm,lkup,resp,0);
			  //REGISTER_TIMING("GNRS_daemon:lookup_handler");
			  //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &endtime_);
			  //ProcLagFile<<timeval_diff(starttime_,endtime_)<<' ';

			  //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime_);
			   //START_TIMING("GNRS_daemon:send_out_lookup_response");
			  //GNRS_server_sendtoaddr= new Address(hdr->sender_addr, GNRSConfig::client_listen_port);
			  GNRS_server_sendtoaddr= new Address(hdr->sender_addr, ntohl(hdr->sender_listen_port));
                       GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);
			 
			  p = new Packet();
			  p->setPayload((char*)resp, sizeof(lookup_response_message_t)+ntohs(resp->na_num)*sizeof(NA)); 
                       GNRS_sport->sendPack(p);
			  //REGISTER_TIMING("GNRS_daemon:send_out_lookup_response");
			  //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &endtime_);
			  //ProcLagFile<<timeval_diff(starttime_,endtime_)<<' ';
						
                        if (DEBUG >=1) cout<<"lookup response packet sent from GNRS"<<endl;
			   if (DEBUG >=1){
				cout<<"packet req_id:"<<ntohl(((common_header_t*)p->getPayloadPointer())->req_id)<<endl;
				cout<<"packet type:"<<(int)((common_header_t*)p->getPayloadPointer())->type<<endl;
				cout<<"number of locators in the packet:"<<ntohs(((lookup_response_message*)p->getPayloadPointer())->na_num)<<endl;
				for(int i=0;i<ntohs(((lookup_response_message*)p->getPayloadPointer())->na_num);i++)
					cout<<"locator "<<i+1<<":"<<((lookup_response_message*)p->getPayloadPointer())->NAs[i].net_addr<<endl;
			   	}
			delete p;
                    }
                    else
                     {
                                        //send lookup packet to Hashed location
                                        //GNRS_server_sendtoaddr = new Address(hash_ip,7000);
                                        GNRS_server_sendtoaddr = new Address(hash_ip,GNRSConfig::daemon_listen_port+1); 
                                        GNRS_sport->setRemoteAddress(GNRS_server_sendtoaddr);
                                        //GNRS_sport->sendPack(recvd_pkt);
			 if (DEBUG >=1)    {
				cout << "Forwarding lookup: pkt type: " << (int)hdr->type << endl;
				cout<<"sender listen port:"<<ntohl(hdr->sender_listen_port)<<endl;       
			 }
			 /*lookup_message_t *lkup_f = (lookup_message_t*)malloc(sizeof(lookup_message_t));
			 strcpy(lkup_f->c_hdr.sender_addr, lkup->c_hdr.sender_addr);
			 lkup_f->c_hdr.req_id = lkup->c_hdr.req_id;
			 lkup_f->c_hdr.type = LOOKUP;
			 strcpy(lkup_f->guid, lkup->guid);
			 p = new Packet();
			 p->setPayload((char*)lkup_f, sizeof(lookup_message_t)); 
			 GNRS_sport->sendPack(p);  */
	
			 GNRS_sport->sendPack(recvd_pkt);
                                }

	uint32_t _req_id;
        if(SAMPLING==1) _req_id=ntohl(hdr->req_id);

	//delete p;
	delete(recvd_pkt);
	delete GNRS_server_sendtoaddr;
	delete GNRS_sport;
	//clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &endtime);
	//ProcLagFile<<timeval_diff(starttime,endtime)<<endl;
double sample_time=REGISTER_TIMING("GNRS_daemon:global_lookup_packet_handler");

	if(SAMPLING==1)  {
		//uint32_t _req_id=ntohl(hdr->req_id);
		pthread_mutex_lock(&lkup_pkt_sampling_mutex);
		proc_lookup_num++;
		if(proc_lookup_num%STAT_STEP<STAT_RANGE) gnrs_para->gnrs_daemon->timingStat(proc_lookup_num-proc_lookup_num%STAT_STEP,sample_time);
		PKT_SAMPLE_MAP::iterator _it=_pkt_sample.find(_req_id);
		if(_it!=_pkt_sample.end())  {
			clock_gettime(CLOCK_REALTIME, &_it->second.endtime);
			//printf("index: %d, endtime: %llu\n ",_req_id,(unsigned long long)_it->second.endtime.tv_sec*1000000000 + _it->second.endtime.tv_nsec );
		}  
		pthread_mutex_unlock(&lkup_pkt_sampling_mutex);
	}
	delete gnrs_para;
	return (void)0;
}



/*
*   GNRS RECEIVER SCRIPT :
*   Receive insert and perform put : type 0
*   Recieve lookup and perform get : type 1
*/
void* GNRS_daemon::g_receiver()
{
        GNRS_server_raddr = new Address(GNRSConfig::server_addr.c_str(), GNRSConfig::daemon_listen_port+1); 
        my_global_rport = new IncomingConnection();
        my_global_rport->setLocalAddress(GNRS_server_raddr);
        my_global_rport->init();
		
        cout<<"GNRS : Receiver started"<<endl;

	/*ProcLagFile.open("/var/log/processing_lag.data"); 
	if (!ProcLagFile.is_open()){
		cerr << "Can't open OUTPUT Latency File !!! Returning..." <<endl;
		return (void *)0;
	}  */

	ThreadPool<GNRS_Para*> insert_pool (global_INSERT_packet_handler, 
		pool_size, //minimum threads
		pool_size, //maximum threads
		ThreadPool<GNRS_Para*>::UnlimitedLifetime  //thread lifetime
		);

	ThreadPool<GNRS_Para*> lookup_pool (global_LOOKUP_packet_handler, 
		pool_size, 
		pool_size, 
		ThreadPool<GNRS_Para*>::UnlimitedLifetime
		);

	/*ThreadPool<GNRS_daemon*> insert_pool (global_INSERT_packet_handler, 
		1, //minimum threads
		1, //maximum threads
		ThreadPool<GNRS_daemon*>::UnlimitedLifetime  //thread lifetime
		);

	ThreadPool<GNRS_daemon*> lookup_pool (global_LOOKUP_packet_handler, 
		1, 
		1, 
		ThreadPool<GNRS_daemon*>::UnlimitedLifetime
		);  */
	
	//Packet *recvd_pkt;
	common_header_t *hdr;
	GNRS_Para *gnrs_para;
	int i=0,j=0;
	int thres;
	if(serv_req_num>0)
		thres=serv_req_num;
	else
		thres=-serv_req_num; 
        while(i<thres){
                 try{
			   /* 	pthread_mutex_lock( &(gnrs_condition.mutex) );
				if (gnrs_condition.condition_set) {   //side effect: limit the number of para in the para queue of thread pool to at most 1.
	 				 pthread_cond_wait(&(gnrs_condition.condition), &(gnrs_condition.mutex));
				}
				pthread_mutex_unlock( (&gnrs_condition.mutex) );  */

                         recvd_pkt = my_global_rport->receivePacketDirectly();					
		
			 gnrs_para=new GNRS_Para;
			 gnrs_para->recvd_pkt=recvd_pkt;
			 gnrs_para->gnrs_daemon=this;

			 if(SAMPLING==1&&i==0&&j==0)  {
				startStatistics(0.1);		
				j++;
			  }
			    //gettimeofday(&starttime,0x0);
			    //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime);
                         if(recvd_pkt != NULL)
                         {    
				    hdr = (common_header_t*)recvd_pkt->getPayloadPointer();

				    if(SAMPLING==1&&rec_lookup_num%1000==1)  {  //potential bug lies: here we assume if a lookup pkt comes, the following pkts are all lookup. but the packet might be insert packet, don't need to be counted.
						uint32_t _req_id=ntohl(hdr->req_id);
						//cout<<"lookup_num:"<<lookup_num<<"  req_id:"<<_req_id<<endl;
						pthread_mutex_lock(&lkup_pkt_sampling_mutex);
						clock_gettime(CLOCK_REALTIME, &_pkt_sample[_req_id].starttime);
        				        _pkt_sample[_req_id].endtime.tv_sec=0;
				                _pkt_sample[_req_id].endtime.tv_nsec=0;
						//printf("index: %d, starttime: %llu\n ",_req_id,(unsigned long long)_pkt_sample[_req_id].starttime.tv_sec*1000000000 + _pkt_sample[_req_id].starttime.tv_nsec );
						pthread_mutex_unlock(&lkup_pkt_sampling_mutex);
				    }

                                   //int flag1=1;
                                if (DEBUG >=1) {
					cout<<"packet request ID:"<<ntohl(hdr->req_id)<<endl;
					cout<<"packet type:"<<(int)hdr->type<<endl;
					cout<<"sender address:"<<hdr->sender_addr<<endl;       
					cout<<"sender listen port:"<<ntohl(hdr->sender_listen_port)<<endl;       
                           		}
				    //ProcLagFile<<ntohl(hdr->req_id)<<' ';

				     // gnrs_condition.condition_set = true;
	                           if(hdr->type==INSERT)  {
					    insert_pool.Launch(gnrs_para);
					    rec_insert_num++;
	                           	}
					    //g_thread = boost::thread(&GNRS_daemon::global_INSERT_packet_handler, this, recvd_pkt,hdr,GNRS_sport);  
					    //global_INSERT_packet_handler(recvd_pkt,hdr,GNRS_sport);
	                           else if(hdr->type == LOOKUP) {
					    //lookup_pool.Launch(this);
					    lookup_pool.Launch(gnrs_para);
					    rec_lookup_num++;
	                           	}
					    //g_thread = boost::thread(&GNRS_daemon::global_LOOKUP_packet_handler, this, recvd_pkt,hdr,GNRS_sport);  
					    //global_LOOKUP_packet_handler(recvd_pkt,hdr,GNRS_sport);   
					    
					   //gettimeofday(&endtime,0x0);
					   //clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &endtime);
			 		   //ProcLagFile<<timeval_diff(starttime,endtime)<<endl;
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
		//ProcLagFile.close(); 
        return (void *)0;

}  // end of GNRS receiver



/****************************************************

	
	Do longest prefix matching for an IP 
	return: Maxlenght (which can be ZERO) 
			and the coresponding asNumber

void GNRS_daemon::longestPrefixMatching(u32b IP, u8b &maxPrefLen, asNum &asNumber, Cidr &maxPrefCidr){
	maxPrefLen =0; 
	asNumber =0; 
	//Walk through all prefixes PREFIX to find the longest one 
	for(u32b i=0; i<curr_prefix.entryList.size();i++){
		u32b bitMask = curr_prefix.entryList[i].mask_bit-1;
		u32b left = curr_prefix.entryList[i].prefix & MASK[bitMask];
		u32b right = IP & MASK[bitMask]; 
		if ((left == right) && (maxPrefLen < bitMask+1)){
			maxPrefLen = bitMask + 1; 
			asNumber = curr_prefix.entryList[i].orgin_AS; 
			maxPrefCidr.mask_bit = curr_prefix.entryList[i].mask_bit;
			maxPrefCidr.prefix = curr_prefix.entryList[i].prefix; 
		}
	}
}
*************************************************/



void GNRS_daemon::read_prefix_table(const char *pref_Filename)
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


void* GNRS_daemon::read_server_list()
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
void GNRS_daemon::read_mappings_from_store(){

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
void GNRS_daemon::initMASK(){
	u32b tmp =0;	 
	for (u8b i=0; i<32; i++){
		u32b tmp2 = (1); 
			tmp2 = tmp2 << (31-i) ; 
		tmp = tmp2 | tmp;
		if (DEBUG >=2) cout << tmp <<endl; 
		MASK[i]=tmp; 
	}
}



void GNRS_daemon::print_usage()
{
	cout << "Usage: ./gnrsd <config file> <thread_pool_size> <service_req_num> [<server_self_addr>] [servers_list_file]" << endl;
}


// we have three output files here:
// gnrs_proc_statistics.data: print out the number of insert and lookup pkts processed per 0.1s
// gnrs_time_statistics.data: print out the processing time of global_lookup_handler along time. results might not be accurate as it outreaches timing library's precision limit
// pkt_sampling_output.data: print out the total service time of sampled pkt
int main(int argc,const char * argv[]) {

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
	GNRS_daemon gnrsd;

	if(argc < 4){
		gnrsd.print_usage();
		exit(0);
	}
	const char* conf_filename = argv[1];

        char _pool_size[5];
        strcpy(_pool_size,argv[2]);
        pool_size=Common::port_str2num(_pool_size);
	if(DEBUG>=1) cout<<"pool_size: "<<pool_size<<endl;

        char _serv_req_num[10];
        strcpy(_serv_req_num,argv[3]);
        serv_req_num=Common::port_str2num(_serv_req_num);
	if(DEBUG>=1) cout<<"server terminate after receiving: "<<serv_req_num<<endl;

	/**
	 * Read configuration settings from file and update any
	 * default settings.
	 * 
	 * Exits program with error code either on i/o or parse errors while 
	 * reading the file, or when a required setting hasn't been specified
	 */ 
	GNRSConfig::init_defaults();
	GNRSConfig::read_from_file(conf_filename);

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
	
	DECLARE_TIMING_THREAD("tester");
	
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

