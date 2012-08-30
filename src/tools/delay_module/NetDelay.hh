/*
 * NetDelay.hpp
 *
 *  Created on: Aug 8, 2012
 *      Author: Feixiong Zhang
 *  2012/08/30: Migrated from net_delay.hh
 */

#ifndef __NETDELAY_HPP__
#define __NETDELAY_HPP__

#include <click/element.hh>
#include <click/timer.hh>
#include "DelayUnit.hh"
CLICK_DECLS

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

private:
        PriorityQueue<delay_unit,delay_compare> prio_q;
        DelayUnit d;
        int q_top;  //top key of the queue
        struct timeval now;
        int pkt_delay;

        Timer _timer;
};

CLICK_ENDDECLS
#endif

