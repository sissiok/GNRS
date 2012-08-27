#include <stdio.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include "../common/packetheader.h"
#include "../common/packet.h"
#include "../common/address.h"
#include "../client/gnrsclient.h"
#include "NAS.h"
#include "content_message.h"

using namespace std; 

struct timeval starttime,endtime;

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

    cout<<"-----------------------------------------------------------"<<endl;
    cout<<"content name: smiling_face.txt"<<endl<<"corresponding GUID: xy12wfx345fda4289fd"<<endl;

    cout<<"-----------------------------------------------------------"<<endl;
    cout<<"insert content GUID to GNRS"<<endl;
	char content_guid[SIZE_OF_GUID]="xy12wfx345fda4289fd";
        NA *temp=new NA;
	//temp->net_addr= "192.168.88.0:88" ;
	//strcpy(temp->net_addr,"192.168.1.0:192.168.1.88");
	strcpy(temp->net_addr,"192.168.1.0:192.168.1.88");
	temp->weight=1;
	temp->TTL=99999;

    if(clientHost->insert(content_guid,temp,1,0)) {
	cout << "GUID Insert failed!" << endl;
    }else{
	cout << "GUID Insert done!" << endl;
    } 
    cout<<"-----------------------------------------------------------"<<endl;

    IncomingConnection *incConnection_ = new IncomingConnection();
    Address *serverListAddress_ = new Address(GNRSConfig::client_addr.c_str(),10000);
    incConnection_->setLocalAddress(serverListAddress_);
    incConnection_->init();

     Address* ServerOutAddress_ = new Address(GNRSConfig::client_addr.c_str(), 10001);
     Address* RequestorAddress_;
     OutgoingConnection *outConnection_ = new OutgoingConnection();
     outConnection_->setLocalAddress(ServerOutAddress_);
     outConnection_->init();


    ifstream cont_file("smiling_face.txt",ios_base::binary);

    char a[1000];
    Packet *recevPacket,*my_pkt; 
    content_pkt* cont_pkt;
    while(1)   {
	recevPacket = incConnection_->receivePacketDirectly();
        init_request* req_pkt=(init_request *)recevPacket->getPayloadPointer();
	cout<<"content GUID that is requested: "<<req_pkt->guid<<endl;
	cout<<"network address of the requestor: "<<req_pkt->sender_addr<<endl;

	RequestorAddress_ = new Address(req_pkt->sender_addr, 20000);
	outConnection_->setRemoteAddress(RequestorAddress_);
        //outConnection_->init();

	cont_file.seekg(0,ios::beg);
	cout<<"start to send the file!"<<endl;
	while(!cont_file.eof())  {
		   my_pkt = new Packet();
       		   cont_file.read(a,DEFAULT_PLD_SIZE);
		   cont_pkt = (content_pkt *)malloc(sizeof(content_pkt)+cont_file.gcount());
		   
		   //cout<<a<<endl<<cont_file.gcount()<<endl;

		   if(cont_file.gcount()<DEFAULT_PLD_SIZE)  
			cont_pkt->file_ending_flag=1;
		   else
			cont_pkt->file_ending_flag=0;

	           //cout<<(int) cont_pkt->file_ending_flag<<endl;
		   
		   cont_pkt->pld_size=htonl(cont_file.gcount());
		   char* pld=(char *)(cont_pkt+1);
		   strncpy(pld,a,cont_file.gcount());
		   my_pkt->setPayload((char*)cont_pkt,sizeof(content_pkt)+cont_file.gcount());
		   outConnection_->sendPack(my_pkt);
		}
	cout<<"finish sending file!"<<endl;
	cont_file.clear();
	}
   cont_file.close();
   return 0;
}
