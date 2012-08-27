#ifndef _DELAY_STRUCT_HH
#define _DELAY_STRUCT_HH

#include <click/element.hh>
#include <click/timestamp.hh>
#include "priority_queue.hh"

CLICK_DECLS

#ifndef DEBUG
#define DEBUG 1
#endif

struct delay_unit {
	Timestamp key;   //key is wall time: ms. double type is not supported under kernel
	Packet* pkt;
};

struct delay_compare {
	bool operator()(const delay_unit &x, const delay_unit &y) {
		return x.key < y.key;
	}
};

CLICK_ENDDECLS
#endif

