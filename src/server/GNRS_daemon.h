#include "daemon.h"
#include "Hash128.h"
#include "announced_prefix_table.h"
#include "./LPM/iproutetable.hh"
#include "./LPM/radixiplookup.hh"

Driver *driver;
Connection *con;
Statement *stmt;
ResultSet *res;
PreparedStatement *prep_stmt;
int updatecount=0;

pthread_mutex_t mysql_mutex;
pthread_mutex_t lkup_pkt_sampling_mutex;
pthread_mutex_t ins_pkt_sampling_mutex;

vector<string> server_list;
u32b MASK[32]; 
PrefixTable curr_prefix;

struct GNRS_Condition {
		pthread_mutex_t mutex;
		pthread_cond_t condition;
		bool condition_set;
	} gnrs_condition;

class GNRS_daemon: public daemon{
public:

GNRS_daemon();

struct GNRS_Para  {
        Packet *recvd_pkt;
        GNRS_daemon *gnrs_daemon;
};

static void insert_msg_handler(const char* hash_ip, HashMap _hm, Packet* recvd_pkt, bool redirect_flag);

//static void global_INSERT_packet_handler(GNRS_daemon * gnrs_daemon);
static void global_INSERT_msg_handler(GNRS_Para *gnrs_para);


static void lookup_msg_handler(const char* hash_ip, HashMap _hm, Packet* recvd_pkt);

//static void global_LOOKUP_packet_handler(GNRS_daemon * gnrs_daemon);
static void global_LOOKUP_msg_handler(GNRS_Para *gnrs_para);

void* g_receiver();

string GUID2Server(char* GUID, uint8_t hashIndex);
void longestPrefixMatching(u32b IP, u8b &maxPrefLen, asNum &asNumber, Cidr &maxPrefCidr);
void initMASK();

void read_prefix_table(const char *pref_Filename);
void* read_server_list();
void read_mappings_from_store();
void print_usage();

int timingStat(int index,double time_);
int prev_time_index,prev_index_num;
double total_time;

private:
	Address * GNRS_server_raddr;
	//Address * GNRS_server_saddr;
	IncomingConnection *my_global_rport;
	//OutgoingConnection *GNRS_sport;
	HashMap g_hm;    //global hashmap 

	RadixIPLookup *radixlookup;

	Packet *recvd_pkt;
};


#define SEC2NANO 1000000000

char chr[][50]=
  {
    "0.0.0.0",
    "128.0.0.0",
    "192.0.0.0",
    "224.0.0.0",
    "240.0.0.0",
    "248.0.0.0",
    "252.0.0.0",
    "254.0.0.0",
    "255.0.0.0",
    "255.128.0.0",
    "255.192.0.0",
    "255.224.0.0",
    "255.240.0.0",
    "255.248.0.0",
    "255.252.0.0",
    "255.254.0.0",
    "255.255.0.0",
    "255.255.128.0",
    "255.255.192.0",
    "255.255.224.0",
    "255.255.240.0",
    "255.255.248.0",
    "255.255.252.0",
    "255.255.254.0",
    "255.255.255.0",
    "255.255.255.128",
    "255.255.255.192",
    "255.255.255.224",
    "255.255.255.240",
    "255.255.255.248",
    "255.255.255.252",
    "255.255.255.254",
    "255.255.255.255",

  }; 


inline int getoctet(int index,char *str)
{
	int count;

	int i;
	count = 0;

	for (i = 0; i < strlen(str);i++)
	    if (count == index)	break;
	    else 
	       if (str[i] == '.') 	count++;


	char temp[4];

	int j;

	for (j = 0; j < 4; j++)
	{
		temp[j]=str[i + j];
		if (str[i+j] == '.' || str[i+j] == '\0'){
		 temp[j] = '\0';
		 break;
	}
}

	int ret = atoi(temp);

	return ret;

}



inline void makemask(char *str, char *mask,char *ip)
{
	int i;

	for (i = 0; i < strlen(str); i++)
	if(str[i] == '/')	break;

	char _mask[4];

	int j;

	for (j = 0; j < strlen(str) - i;j++)
	_mask[j] = str[i + j + 1];

	_mask[j]='\0';


	int n = atoi(_mask);

	//strcpy(mask,chr[n]);


	char _ip[20];

	strncpy(_ip,str,i);

	_ip[i]='\0';

	int digit;
	for (i = 0; i < 4; i++){
		 digit = getoctet(i,_ip);
		 ip[i] = digit;
		 digit = getoctet(i,chr[n]);
		 mask[i] = digit;
		 //printf("\ndigit = %d",digit);
		 
	}
}

