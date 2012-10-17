#include <unistd.h>
#include <ctime>
#include <math.h>
#include "gbench.h"
#include "../common/time_measure.h"
#include "../common/profiler/Timing.h"
#include "statistics.h"
//struct timeval starttime,endtime;
struct timespec starttime,endtime;

struct Client_Condition client_condition;
pthread_mutex_t lkup_pkt_sampling_mutex;

int snt_insert_num,snt_lookup_num;
int rec_insert_num,rec_lookup_num;
PKT_SAMPLE_MAP _pkt_sample;


struct timespec* us2delay(long int delay)  {
  struct timespec* tdelay_=new struct timespec;
  tdelay_->tv_nsec = delay*1e3;
  tdelay_->tv_sec =  0; 
  return(tdelay_);
}


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
	clientHost->insertProc(guid,insert_na_struct,na_num,req_ID);
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

	if(SAMPLING==1&&snt_lookup_num%STAT_STEP==1)  {
		pthread_mutex_lock(&lkup_pkt_sampling_mutex);
		//cout<<"lookup_num:"<<lookup_num<<"  req_id:"<<req_ID<<endl;
		clock_gettime(CLOCK_REALTIME, &_pkt_sample[req_ID].starttime);
		_pkt_sample[req_ID].endtime.tv_sec=0;
		_pkt_sample[req_ID].endtime.tv_nsec=0;
		pthread_mutex_unlock(&lkup_pkt_sampling_mutex);
	 }
	na_num=clientHost->lookupProc(guid, req_ID);

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

void gbench_exec(char* req_filename, GNRSClient* clientHost,int req_interval){

	//Readfile
	ifstream event_FHdlr(req_filename);
	string tempt_line; //iterator to walk through event file 
	//Command parameters
	char optCode; 
	//counter for debuging purpose
	u64b processedEvent =0; 

	/*	
	//output File
	ofstream LatFile("/var/log/request_latency.data"); 
	cout <<" Openning Event file !!!" <<endl;
	if (!LatFile.is_open()){
		cerr << "Can't open OUTPUT Latency File !!! Returning..." <<endl;
		return; 
	}
	*/
	time_t t;
	double r,lambda;
	char buff[10];
	int j,n,N;
//	n=(int)(1000000/req_interval);  //request rate
//	N=(int)(1000000/(n*0.00821));
	N=(int)req_interval/0.00821;
	unsigned long long _timestamp;
	if(SAMPLING==1)  startStatistics(0.1);
	if (event_FHdlr.is_open()){
		srand(time(&t));
		while (event_FHdlr.good()){
			START_TIMING("gbench: read_data");
			tempt_line.clear(); //init
			getline(event_FHdlr,tempt_line); //read from file	
			
			if(tempt_line.size()==0)
				break;  //jump out when nothing is read			
			//parse the command	
			vector<string> strTemp_v; 
			Common::str2StrArr(tempt_line,' ', strTemp_v); 
			optCode = toupper((unsigned char)(strTemp_v[1][0]));  //optCode
			processedEvent++;
			REGISTER_TIMING("gbench: read_data");

			//gettimeofday(&starttime,0x0);
			switch (optCode){
				case 'I':
					START_TIMING("gbench: insert");
					//usleep(800);
					usleep(100);
					insert_handler(strTemp_v, clientHost); 	
					snt_insert_num++;
					REGISTER_TIMING("gbench: insert");
					break;
				case 'Q':
					START_TIMING("gbench: lookup");
					
					/**exponential interval
					lambda=5000;
					r = ((double) rand() / (RAND_MAX));
					r = -log(1-r)/lambda;
					//usleep((int)(r*1000000));
					n=(int)(r*1000000/0.00821);
					for(j=0;j<n;j++)  {
				                asm volatile (".byte 0x0f, 0x31" : "=A" (_timestamp));
       					}
					*/	
					
					//constant interval
                                        for(j=0;j<N;j++)  {
                                                asm volatile (".byte 0x0f, 0x31" : "=A" (_timestamp));
                                        }

					
					//usleep(850);
					//usleep(1);
					//usleep(380);
					//nanosleep(us2delay(1),NULL);
					/*
					//randomize query guid
					sprintf(buff,"%d",processedEvent);
					strTemp_v[0]=buff;
					strTemp_v[1]='Q';
					sprintf(buff,"%d",rand()%10000);
					strTemp_v[2]=buff;
					cout<<"DEBUG:"<<strTemp_v[0]<<" "<<strTemp_v[2]<<endl;
					*/
					query_handler(strTemp_v, clientHost);
					snt_lookup_num++;
					REGISTER_TIMING("gbench: lookup");
					break;
				default:
					cout << "Some thing wrong with optCode !!!!!"; 
			}
		//gettimeofday(&endtime,0x0);
		//LatFile<<strTemp_v[0]<<' '<<timeval_diff(starttime,endtime)<<endl;
		}
	}
	else{
		cerr << "Can't open EVENT File !!! Returning..." <<endl;
		return; 
	}
	//cout << "Get query Latency " << endl; 
	//LatFile.close(); 
	
}

void print_usage(){

	cout << "Usage: ./gbench <config_file> <request_file> <request_interval> ( optional: <client_address>, <server_address>, <client_listen_port>)" 
		<< endl;
}

int main(int argc, char* argv[]){

	/* process input parameters */
	
	if(argc < 4){
		print_usage();
		exit(0);
	}

    snt_insert_num=snt_lookup_num=rec_insert_num=rec_lookup_num=0;

    char* conf_filename=argv[1]; 
    char* req_filename=argv[2];
    char client_addr[SIZE_OF_NET_ADDR];
    char server_addr[SIZE_OF_NET_ADDR];

    pthread_mutex_init(&lkup_pkt_sampling_mutex,NULL);
    pthread_mutex_init(&(client_condition.mutex),NULL);
    pthread_cond_init(&(client_condition.condition),NULL);
    client_condition.condition_set=true;

    char req_interval[10];
    int _req_interval;
    strcpy(req_interval,argv[3]);
    _req_interval=Common::port_str2num(req_interval);
    //cout<<_req_interval<<endl;

    if(argc >4)
		strcpy(client_addr,argv[4]);
    else
		memset(client_addr,'\0',SIZE_OF_NET_ADDR);
    if(argc >5)
		strcpy(server_addr,argv[5]);
    else
		memset(server_addr,'\0',SIZE_OF_NET_ADDR);

    char listen_port[10];
    uint32_t _listen_port=0;
    if(argc>6)    {
        strcpy(listen_port,argv[6]);
        _listen_port=Common::port_str2num(listen_port);
		 
    }
    if(DEBUG>=1) cout<<"client_listen_port:"<<GNRSConfig::client_listen_port<<endl;

   GNRSConfig::init_defaults();
   GNRSConfig::read_from_file(conf_filename);

    if(client_addr[0]!='\0')
		GNRSConfig::client_addr=client_addr;
    if(DEBUG>=1)	cout<<"client address"<<GNRSConfig::client_addr<<endl;

   if(_listen_port>0)	GNRSConfig::client_listen_port= _listen_port;

   pthread_t receivingThread;
   if(pthread_create(&receivingThread,NULL,&waitForAckReceiver,NULL)){
	  	cout<<"Error creating receive thread, aborting"<<endl;
		return 1;
	}

    GNRSClient* clientHost = new GNRSClient(client_addr,server_addr);

    DECLARE_TIMING_THREAD("tester");

    pthread_mutex_lock(&(client_condition.mutex));
    if(client_condition.condition_set)
	  pthread_cond_wait(&(client_condition.condition), &(client_condition.mutex));
    pthread_mutex_unlock(&(client_condition.mutex));

    gbench_exec(req_filename,clientHost,_req_interval);

    sleep(140);
    pthread_cancel(receivingThread);
    pthread_join(receivingThread,NULL);

    if(SAMPLING==1)	
	sampling_output();
   
    printf("snt_insert_num:%d, snt_lookup_num:%d, rec_insert_num:%d, rec_lookup_num:%d\n",snt_insert_num, snt_lookup_num, rec_insert_num, rec_lookup_num);
 
    cout << "FINISHED !!!!!!!!"<<endl; 

	pthread_mutex_destroy(&lkup_pkt_sampling_mutex);
	pthread_mutex_destroy(&client_condition.mutex);
	pthread_cond_destroy(&client_condition.condition);

	return 0; 
}

