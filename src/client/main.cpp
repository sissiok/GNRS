#include <stdio.h>
#include <string.h>
#include <iostream>
#include "../common/packetheader.h"
#include "../common/packet.h"
#include "../common/address.h"
#include "gnrsclient.h"

using namespace std; 

struct timeval starttime,endtime;

struct Client_Condition client_condition;


//Usage: ./gnrs_client <config_file>  ( optional: <client_address>, <server_address>, <client_listen_port>)
int main(int argc, char **argv) 
{
    char filename[100]; 
    char client_addr[SIZE_OF_NET_ADDR];
    char server_addr[SIZE_OF_NET_ADDR];
    if(argc > 1){
    	strcpy(filename, argv[1]); 
    }else{
    	strcpy(filename,"./client.conf"); 
    }
    if(argc >2)
		strcpy(client_addr,argv[2]);
    else
		memset(client_addr,'\0',SIZE_OF_NET_ADDR);
    if(argc >3)
		strcpy(server_addr,argv[3]);
    else
		memset(server_addr,'\0',SIZE_OF_NET_ADDR);

    char listen_port[10];
    uint32_t _listen_port=0;
    if(argc>4)    {
        strcpy(listen_port,argv[4]);
        _listen_port=Common::port_str2num(listen_port);
    }

    pthread_mutex_init(&(client_condition.mutex),NULL);
    pthread_cond_init(&(client_condition.condition),NULL);
    client_condition.condition_set=true;

    GNRSClient* clientHost = new GNRSClient(filename,client_addr,server_addr,_listen_port);
    char guid[SIZE_OF_GUID] = "xx123";
    NA result[LOOKUP_MAX_NA];
	uint16_t na_num;
    na_num=clientHost->lookup(guid, result,0);
    cout << "Lookup result: " <<result[0].net_addr <<endl; 

    NA *temp=new NA;
	//temp->net_addr= "192.168.88.0:88" ;
	strcpy(temp->net_addr,"192.168.88.0:88");
	temp->weight=1;
	temp->ttlMsec=99999;
    //char  net_addr[SIZE_OF_NET_ADDR] = "192.168.88.0:88" ;
    cout << "Insert guid: " << guid << " netaddr: " <<temp->net_addr << endl;  
    if(clientHost->insert(guid,temp,1,0)) {
	cout << "Insert failed!" << endl;
    }else{
	cout << "Insert done!" << endl;
    }   
	delete temp;
	
    cout << "Lookup guid: " << guid << endl; 
    na_num=clientHost->lookup(guid, result,0);
    cout << "Lookup result: " << result[0].net_addr << endl;
   // char guid1[SIZE_OF_GUID] ="notfound";
   // clientHost->lookup(guid1,&result[0],SIZE_OF_NET_ADDR);
   // cout << "Lookup result: " << result << endl; 

    pthread_mutex_destroy(&client_condition.mutex);
    pthread_cond_destroy(&client_condition.condition);

    return 0;
}
