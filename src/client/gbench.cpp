#include "gbench.h"
#include "../common/time_measure.h"
#include "../common/profiler/Timing.h"
//struct timeval starttime,endtime;
struct timespec starttime,endtime;

struct Client_Condition client_condition;


/*
 * Format of requests:
 * <req_ID> INSERT <GUID> <NA-list>
 */
void* insert_handler(vector<string> str_v, GNRSClient* clientHost)
{
	char guid[SIZE_OF_GUID];
	uint32_t req_ID;

	req_ID=(uint32_t)atoi(str_v[0].c_str());
	strcpy(guid, str_v[2].c_str());
	uint16_t na_num=str_v.size()-3;
	NA insert_na_struct[na_num];
	for(int i=0;i<na_num;i++)
	{
		vector<string> strTemp_v; 
		Common::str2StrArr(str_v[i+3],',', strTemp_v); 
		strcpy(insert_na_struct[i].net_addr,strTemp_v[0].c_str());
		insert_na_struct[i].TTL=atol(strTemp_v[1].c_str());
		insert_na_struct[i].weight=atoi(strTemp_v[2].c_str());
	}
	if(DEBUG>=1) cout<<"guid=="<<guid<<"weights:"<<insert_na_struct[0].weight<<endl;
	clientHost->insert(guid,insert_na_struct,na_num,req_ID);
}



/*
 * Format of requests:
* <req_ID> LOOKUP <GUID>
 */
void* query_handler(vector<string> str_v, GNRSClient* clientHost)
{
	char guid[SIZE_OF_GUID];
	uint32_t req_ID;
	req_ID=(uint32_t)atoi(str_v[0].c_str());
	strcpy(guid, str_v[2].c_str());
	if(DEBUG>=1) cout<<"guid=="<<guid<<endl;
	
	NA query_na_struct[LOOKUP_MAX_NA];
	uint16_t na_num;

	na_num=clientHost->lookup(guid, query_na_struct, req_ID);

}


/**
 * Read in GNRS requests from a file and excecute those requests
 *
 * Format of requests:
 * <req_ID> INSERT <GUID> <NA-list>
 * <req_ID> LOOKUP <GUID>
 *
 * Field definitions: 
 * req_ID - request identifier, works as a sequence number for the request message
 * operation-type - {INSERT, LOOKUP}
 * GUID - upto 40 digits in hex
 * NA-list - space separated, a NA is a struct of net_addr, TTL and weight with ',' as delimiter
 *
 *
 * Fields are space separated
 *
 */

void gbench_exec(char* req_filename, GNRSClient* clientHost){

	//Readfile
	ifstream event_FHdlr(req_filename);
	string tempt_line; //iterator to walk through event file 
	//Command parameters
	char optCode; 
	//counter for debuging purpose
	u64b processedEvent =0; 
	
	//output File
	ofstream LatFile("/var/log/request_latency.data"); 
	cout <<" Openning Event file !!!" <<endl;
	if (!LatFile.is_open()){
		cerr << "Can't open OUTPUT Latency File !!! Returning..." <<endl;
		return; 
	}

	if (event_FHdlr.is_open()){
		while (event_FHdlr.good()){
			tempt_line.clear(); //init
			getline(event_FHdlr,tempt_line); //read from file	
			
			if(tempt_line.size()==0)
				break;  //jump out when nothing is read			
			//parse the command	
			vector<string> strTemp_v; 
			Common::str2StrArr(tempt_line,' ', strTemp_v); 
			optCode = toupper((unsigned char)(strTemp_v[1][0]));  //optCode
			processedEvent++;

			//gettimeofday(&starttime,0x0);
			switch (optCode){
				case 'I':
					START_TIMING("gbench: insert");
					insert_handler(strTemp_v, clientHost); 	
					REGISTER_TIMING("gbench: insert");
					break;
				case 'Q':
					//execute the query
					START_TIMING("gbench:lookup");
					query_handler(strTemp_v, clientHost);
					REGISTER_TIMING("gbench:lookup");
					break;
				default:
					cout << "Some thing wrong with optCode !!!!!"; 
			}
		//gettimeofday(&endtime,0x0);
		LatFile<<strTemp_v[0]<<' '<<timeval_diff(starttime,endtime)<<endl;
		}
	}
	else{
		cerr << "Can't open EVENT File !!! Returning..." <<endl;
		return; 
	}
	//cout << "Get query Latency " << endl; 
	LatFile.close(); 
	
}

void print_usage(){

	cout << "Usage: ./gbench <config_file> <request_file>  ( optional: <client_address>, <server_address>, <client_listen_port>)" 
		<< endl;
}

int main(int argc, char* argv[]){

	/* process input parameters */
	
	if(argc < 3){
		print_usage();
		exit(0);
	}

    char* conf_filename=argv[1]; 
    char* req_filename=argv[2];
    char client_addr[SIZE_OF_NET_ADDR];
    char server_addr[SIZE_OF_NET_ADDR];

    pthread_mutex_init(&(client_condition.mutex),NULL);
    pthread_cond_init(&(client_condition.condition),NULL);
    client_condition.condition_set=true;

    if(argc >3)
		strcpy(client_addr,argv[3]);
    else
		memset(client_addr,'\0',SIZE_OF_NET_ADDR);
    if(argc >4)
		strcpy(server_addr,argv[4]);
    else
		memset(server_addr,'\0',SIZE_OF_NET_ADDR);

    char listen_port[10];
    uint32_t _listen_port=0;
    if(argc>5)    {
        strcpy(listen_port,argv[5]);
        _listen_port=Common::port_str2num(listen_port);
    }
    //if(DEBUG>=1) cout<<"client_listen_port:"<<GNRSConfig::client_listen_port<<endl;

    GNRSClient* clientHost = new GNRSClient(conf_filename,client_addr,server_addr,_listen_port);

    DECLARE_TIMING_THREAD("tester");
    gbench_exec(req_filename,clientHost);
    cout << "FINISHED !!!!!!!!"<<endl; 

	pthread_mutex_destroy(&client_condition.mutex);
	pthread_cond_destroy(&client_condition.condition);

	return 0; 
}

