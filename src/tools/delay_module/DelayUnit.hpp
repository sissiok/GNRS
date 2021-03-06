#ifndef __DELAYUNIT_HPP__
#define __DELAYUNIT_HPP__

#include <click/element.hh>
#include "PriorityQueue.hpp"

CLICK_DECLS

struct DelayUnit {
	Timestamp clockTime;   //clockTime is real time: ms. double type is not supported under kernel
	Packet* pkt;
};

struct DelayComparator {
	bool operator()(const DelayUnit &x, const DelayUnit &y) {
		return x.clockTime < y.clockTime;
	}
};

CLICK_ENDDECLS
#endif

