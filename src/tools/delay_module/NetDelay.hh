/*
 * NetDelay.hpp
 *
 *  Created on: Aug 8, 2012
 *      Author: Feixiong Zhang
 *  2012/08/30: Migrated from net_delay.hh
 */

#ifndef __NETDELAY_HH__
#define __NETDELAY_HH__
// Number of milliseconds needed to schedule the packet delay timer
#define TIMER_TOLERANCE_MSEC 0

#define DELIVER_BURST_LENGTH 4

// Click includes
#include <click/element.hh>
#include <click/timer.hh>
#include <click/ipaddress.hh>
#include <click/hashtable.hh>
// Custom includes
#include "DelayUnit.hpp"
CLICK_DECLS

//#define DEBUG_CFG
//#define DEBUG_WRN
//#define DEBUG_PSH
//#define DEBUG_TIM


struct ipaddr_cmp {
   bool operator() (IPAddress const *ip1, IPAddress const *ip2) {
       return ip1->addr() < ip2->addr();
   }
};

class NetDelay : public Element {
public:
	NetDelay();
	~NetDelay();
	const char *class_name() const		{ return "NetDelay"; }
	const char *port_count() const		{ return "1/1"; }
	const char *processing() const		{ return "h/h"; }

	int initialize(ErrorHandler *errh);
	int configure(Vector<String>&, ErrorHandler *);
	void push(int port, Packet *p);
	void run_timer(Timer *);
    int live_reconfigure(Vector<String>& conf, ErrorHandler* errh);
    bool can_live_reconfigure() const { return true; }
    void freeTable(const HashTable<uint64_t,int> *table);

private:
    PriorityQueue<DelayUnit,DelayComparator> packetQueue;
    DelayUnit delayUnit;

    Timer sendTimer;
    const HashTable<uint64_t, int> *delayTable;

    //lock for multithread access (incl any separate enq and deq threads)
    atomic_uint32_t pq_lock;

	//stats
	volatile uint32_t recvCnt, sndCnt, sndFires;
};

CLICK_ENDDECLS
#endif

