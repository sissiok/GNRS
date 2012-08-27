#include <stdio.h>
#include <string.h>
#include <string>
#include <iostream>
#include <vector>
#include <fstream>
#include <pthread.h>
#include "../common/common.h"
#include "../common/packetheader.h"
#include "../common/packet.h"
#include "../common/address.h"
#include "../client/gnrsclient.h"
#include "NAS.h"
#include "content_message.h"

using namespace std; 

struct timeval starttime,endtime;


void *cont_receiver(void *arg)  {
    IncomingConnection *incConnection_ = new IncomingConnection(); 
    Address *clientListAddress_ = new Address(GNRSConfig::client_addr.c_str(),20000);
    incConnection_->setLocalAddress(clientListAddress_);
    incConnection_->init();

    ofstream cont_file("smiling_face.txt",ios_base::binary);	
    uint8_t ending_flag=0;
    Packet* rev_pkt;
    cout<<"start to request the content!"<<endl;
    while((int)ending_flag==0)  {
	rev_pkt  = incConnection_->receivePacketDirectly();
	//cout<<"receive a packet"<<endl;
	content_pkt* cont_pkt=(content_pkt *)(rev_pkt->getPayloadPointer());
	if((int)cont_pkt->file_ending_flag==1)  ending_flag=1;
	cout<<(int)cont_pkt->file_ending_flag<<endl;
	cont_file.write((char*)(cont_pkt+1),ntohl(cont_pkt->pld_size));
    }
    cout<<"finish receiving the content!"<<endl;
    cont_file.close();
    delete incConnection_;
    delete clientListAddress_;
}

int main(int argc, char **argv) 
{
    char filename[100]; 
    char client_addr[SIZE_OF_NET_ADDR];
    char server_addr[SIZE_OF_NET_ADDR];
    if(argc > 1){
    	strcpy(filename, argv[1]); 
    }else{
    	strcpy(filename,"./content.conf"); 
    }
    if(argc >2)
		strcpy(client_addr,argv[2]);
    else
		memset(client_addr,'\0',SIZE_OF_NET_ADDR);
    if(argc >3)
		strcpy(server_addr,argv[3]);
    else
		memset(server_addr,'\0',SIZE_OF_NET_ADDR);
    GNRSClient* clientHost = new GNRSClient(filename,client_addr,server_addr);

    cout<<"enter the file that you want to retrieve: ";
    char content_name[100];
    char content_guid[SIZE_OF_GUID];
    cin.getline(content_name,100);
    NameAssignServ NAserv;
    strcpy(content_guid,NAserv.get_guid(content_name).c_str());
    cout<<"corresponding content GUID is: "<<content_guid<<endl;

    cout<<"--------------------------------------------------------"<<endl;
    cout<<"start to query GNRS to get the network address...."<<endl;
    NA result[LOOKUP_MAX_NA];
    uint16_t na_num;
    na_num=clientHost->lookup(content_guid, result,0);
    vector<string> strTemp_v; 
    Common::str2StrArr(result[0].net_addr,'.', strTemp_v); 
    if((strTemp_v[3].c_str())[0]!='0')
	    cout<<"Lookup result: " <<result[0].net_addr <<endl;
    else  {
	delete clientHost;
	//cout<<"requery gnrs"<<endl;
	//clientHost = new GNRSClient(filename,client_addr,"192.168.1.100");
	clientHost = new GNRSClient(filename,client_addr,(char*)(strTemp_v[0]+'.'+strTemp_v[1]+'.'+strTemp_v[2]+'.'+"100").c_str());
	//cout<<(strTemp_v[0]+strTemp_v[1]+strTemp_v[2]+"100").c_str()<<endl;
	na_num=clientHost->lookup(content_guid, result,0);
	cout<<"Lookup result: " <<result[0].net_addr <<endl;
    }
    cout<<"--------------------------------------------------------"<<endl;

    pthread_t receivingThread;
    if(pthread_create(&receivingThread,NULL,&cont_receiver,NULL)){
          cout<<"Error creating receive thread, aborting"<<endl;
	  return -1;
	}

    //send out request packet for a content
     Address* clientOutAddress_ = new Address(GNRSConfig::client_addr.c_str(), 20001); 
     Address* ServerAddress_ = new Address(result[0].net_addr, 10000);

     OutgoingConnection* outConnection_ = new OutgoingConnection(); 
     outConnection_->setLocalAddress(clientOutAddress_);
     outConnection_->setRemoteAddress(ServerAddress_);
     outConnection_->init();

     Packet* contReqPacket=new Packet;
     init_request* req_pkt=new init_request;
     req_pkt->sender_receiving_port=htonl(20000);
     strcpy(req_pkt->sender_addr,GNRSConfig::client_addr.c_str());
     //cout<<"sender address: "<<req_pkt->sender_addr<<endl;
     strcpy(req_pkt->guid,content_guid);

     contReqPacket->setPayload((char*)req_pkt,sizeof(init_request));
     outConnection_->sendPack(contReqPacket);

    pthread_join(receivingThread,NULL);
    return 0;
}
