#ifndef __DELAYUNIT_HPP__
#define __DELAYUNIT_HPP__

#include <click/element.hh>
#include "priority_queue.hh"

CLICK_DECLS

struct DelayUnit {
	int clockTime;   //clockTime is real time: ms. double type is not supported under kernel
	Packet* pkt;
};

struct DelayComparator {
	bool operator()(const DelayUnit &x, const DelayUnit &y) {
		return x.clockTime < y.clockTime;
	}
};

CLICK_ENDDECLS
#endif

