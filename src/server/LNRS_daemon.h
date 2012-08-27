#include "daemon.h"

Driver *driver;
Connection *con;
Statement *stmt;
ResultSet *res;
PreparedStatement *prep_stmt;
int updatecount=0;

pthread_mutex_t mysql_mutex;

class LNRS_daemon: public daemon{
public:
	
Address * LNRS_server_raddr;
Address * LNRS_server_saddr;
Address * LNRS_server_sendtoaddr;
IncomingConnection *my_local_rport;
OutgoingConnection *LNRS_sport;
HashMap l_hm;    //local hashmap


void local_INSERT_packet_handler (Packet *recvd_pkt,common_header_t *hdr,OutgoingConnection *LNRS_sport);


void local_LOOKUP_packet_handler(Packet *recvd_pkt,common_header_t *hdr,OutgoingConnection *LNRS_sport);

void *l_receiver();
void read_mappings_from_store();

void print_usage();
};

