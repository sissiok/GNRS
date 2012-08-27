#include "LNRS_daemon.h"
//#include "../common/profiler/Timing.h"
#include "statistics.h"

ofstream ProcFile;
ofstream ProcFile_;
int rec_insert_num,rec_lookup_num,proc_insert_num,proc_lookup_num;
PKT_SAMPLE_MAP _pkt_sample;


void LNRS_daemon::local_INSERT_packet_handler (Packet *recvd_pkt,common_header_t *hdr,OutgoingConnection *LNRS_sport)
{
//TODO: release memory allocated by "new"
//recvd_pkt->setPayloadSize(sizeof(insert_message_t));
                                  	if (DEBUG >=1) cout<<"insert packet received at LNRS"<<endl;

					insert_message_t *ins = (insert_message_t*)recvd_pkt->getPayloadPointer();
              			if (DEBUG >=1) cout <<"Mapping info in packet is : guid: " << ins->guid << " netaddr: " <<ins->NAs[0].net_addr<<endl;
					insert_handler( l_hm, ins,1);

	//retransmit the insert message to global level
         LNRS_server_sendtoaddr = new Address("127.0.0.1", GNRSConfig::daemon_listen_port+1); 
         LNRS_sport->setRemoteAddress(LNRS_server_sendtoaddr);
	  LNRS_sport->sendPack(recvd_pkt);	

	  delete LNRS_server_sendtoaddr;
	return (void)0;
}



void LNRS_daemon::local_LOOKUP_packet_handler(Packet *recvd_pkt,common_header_t *hdr,OutgoingConnection *LNRS_sport)
{
        if (DEBUG >=1) cout<<"Packet Recieved for Lookup at LNRS"<<endl;
                	//* Handle Lookup Packet */
	lookup_message_t *lkup = 	(lookup_message_t*)recvd_pkt->getPayloadPointer();

	lookup_response_message_t *resp;
	bool valid_flag=lookup_handler(l_hm,lkup,resp,1);
    
	if(valid_flag== true)
	{
		LNRS_server_sendtoaddr= new Address(hdr->sender_addr, GNRSConfig::client_listen_port);
                 LNRS_sport->setRemoteAddress(LNRS_server_sendtoaddr);							

		 Packet *p = new Packet();
		 p->setPayload((char*)resp, sizeof(lookup_response_message_t)+ntohs(resp->na_num)*sizeof(NA)); 
		 LNRS_sport->sendPack(p);
                   if (DEBUG >=1) cout<<"lookup response packet sent from LNRS"<<endl;

		delete LNRS_server_sendtoaddr;
		delete p;
		return (void)0;
	}

	//retransmit the lookup message to global level when LNRS lookup fails
         LNRS_server_sendtoaddr = new Address("127.0.0.1", GNRSConfig::daemon_listen_port+1); 
         LNRS_sport->setRemoteAddress(LNRS_server_sendtoaddr);
	  LNRS_sport->sendPack(recvd_pkt);		

	delete resp;
	delete LNRS_server_sendtoaddr;
	return (void)0;
}




void* LNRS_daemon::l_receiver()
{
      // setting up the port
       //cout<<ip_server<<endl;
       cout<< "Receiver starting... server self addr:"<< GNRSConfig::server_addr << endl;
       LNRS_server_raddr = new Address(GNRSConfig::server_addr.c_str(), GNRSConfig::daemon_listen_port); 
       //LNRS_server_saddr=new Address(ip_server,9000);
        my_local_rport = new IncomingConnection();
        // node 1 receiver port : must be same in the GNRS script aswell
        my_local_rport->setLocalAddress(LNRS_server_raddr);
        my_local_rport->init();
		
        cout<<"LNRS : Receiver started"<<endl;

	LNRS_server_saddr=new Address(GNRSConfig::server_addr.c_str(), 9001);	
        LNRS_sport = new OutgoingConnection();
        LNRS_sport->setLocalAddress(LNRS_server_saddr);
        LNRS_sport->init();
        Packet *recvd_pkt;
        while(1){
                 try{
                         recvd_pkt = my_local_rport->receivePacketDirectly();
                         if(recvd_pkt != NULL)
                         {
				common_header_t *hdr = (common_header_t*)recvd_pkt->getPayloadPointer();
                                   //int flag1=1;
                                  /* driver = get_driver_instance();
			            build db url for connection 
			           char db_url[200];
			           sprintf(db_url, "tcp://%s:%d/%s", GNRSConfig::db_host.c_str(), GNRSConfig::db_port, GNRSConfig::db_name.c_str());
			           con = driver -> connect(db_url, GNRSConfig::db_user, GNRSConfig::db_passwd);
                                   stmt = con -> createStatement();   */
                              if (DEBUG >=1){
					cout<<"packet request ID:"<<ntohl(hdr->req_id)<<endl;
					cout<<"packet type:"<<(int)hdr->type<<endl;
					cout<<"sender address:"<<hdr->sender_addr<<endl;                
                              	}

                                   if(hdr->type==INSERT)
						local_INSERT_packet_handler(recvd_pkt,hdr,LNRS_sport);
                                   else if(hdr->type == LOOKUP) //LOOKUP
                                   	local_LOOKUP_packet_handler(recvd_pkt,hdr,LNRS_sport);   
                                   } 
                                 //usleep(100);
				 //free received packet	
				 delete(recvd_pkt);
                              }
           
                   catch(const char *reason)
                  {
                           cout<<"exception in the LNRS Receiver :"<<reason<<endl;

                   }
          }
	pthread_mutex_destroy(&mysql_mutex);
	delete(LNRS_server_raddr);
	delete(my_local_rport);
	delete(LNRS_server_saddr);
	delete(LNRS_sport);
        return (void *)0;

}  // end of LNRS receiver



/**
 * Update in-memory structure with mappings from persistent store
 *
 */
void LNRS_daemon::read_mappings_from_store()
{

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
	while(1)
	{
		char buff[10];
		string query("SELECT * FROM local_guid_locators_map LIMIT ");
		sprintf(buff, "%d", i);
		query += buff;
		query += ",";
		sprintf(buff, "%d", batch_size);
		query += buff;
		cout << "execing local query:" << query << endl;
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

	       expires->push_back(new unsigned int(atoi(TTL->c_str())));
		weights_->push_back(new unsigned short(atoi(weights->c_str())));
			v->push_back(locators);
			l_hm.put(guid, v, expires,weights_);
		cout<<"guid="<<guid.c_str()<<",locators="<<v->at(0)->c_str()<<endl;
			//delete(locators);
			//delete(v);
		}
		i += read_cnt;
		/* if rows fewer than batch size were read, we're done */
		if(read_cnt < batch_size)break;
	}
	cout << "Read in " << i << " mappings from local store " << endl;
}

void LNRS_daemon::print_usage()
{
	cout << "Usage: ./lnrsd <config file> [<server_self_addr>] [servers_list_file]" << endl;
}

int main(int argc,const char * argv[]) {

	LNRS_daemon lnrsd;

	if(argc < 2){
		lnrsd.print_usage();
		exit(0);
	}
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

	/**
	 * Override server self address from config with command line param
	 * if specified
	 */
	if(argc > 2){
		GNRSConfig::server_addr = argv[2];
	}
	
	/* override server list file */
	if(argc > 3){
		GNRSConfig::servers_list_file = argv[3];
	}

	pthread_mutex_init(&mysql_mutex,NULL);
	
	/* bring in any guid-locator mappings stored from prior execution */
	lnrsd.read_mappings_from_store();

	lnrsd.l_receiver();
	cout<<"LNRS server terminate!"<<endl;
	return 0;
}

