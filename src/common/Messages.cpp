#include <netinet/in.h>
#include "Messages.h"

	void* nacpy(NA& d_NA, NA& s_NA)
	{
	  	strcpy(d_NA.net_addr, s_NA.net_addr);
		d_NA.ttlMsec=s_NA.ttlMsec;
		d_NA.weight= s_NA.weight;
	}

	void* hton_nacpy(NA& d_NA, NA& s_NA)
	{
	  	strcpy(d_NA.net_addr, s_NA.net_addr);
		d_NA.ttlMsec=htonl(s_NA.ttlMsec);
		d_NA.weight= htons(s_NA.weight);
	}


	void* ntoh_nacpy(NA& d_NA, NA& s_NA)
	{
	  	strcpy(d_NA.net_addr, s_NA.net_addr);
		d_NA.ttlMsec=ntohl(s_NA.ttlMsec);
		d_NA.weight= ntohs(s_NA.weight);
	}

