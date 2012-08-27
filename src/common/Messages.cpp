#include <netinet/in.h>
#include "Messages.h"

	void* nacpy(NA& d_NA, NA& s_NA)
	{
	  	strcpy(d_NA.net_addr, s_NA.net_addr);
		d_NA.TTL=s_NA.TTL;
		d_NA.weight= s_NA.weight;
	}

	void* hton_nacpy(NA& d_NA, NA& s_NA)
	{
	  	strcpy(d_NA.net_addr, s_NA.net_addr);
		d_NA.TTL=htonl(s_NA.TTL);
		d_NA.weight= htons(s_NA.weight);
	}


	void* ntoh_nacpy(NA& d_NA, NA& s_NA)
	{
	  	strcpy(d_NA.net_addr, s_NA.net_addr);
		d_NA.TTL=ntohl(s_NA.TTL);
		d_NA.weight= ntohs(s_NA.weight);
	}

