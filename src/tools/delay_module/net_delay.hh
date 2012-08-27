/*
 * net_delay.hh
 *
 *  Created on: Aug 8, 2012
 *      Author: Feixiong Zhang
 */

#ifndef NET_DELAY_HH_
#define NET_DELAY_HH_

#include <click/element.hh>
#include <click/timer.hh>
#include "delay_struct.hh"
CLICK_DECLS

class net_delay : public Element {
public:
	net_delay();
	~net_delay();
	const char *class_name() const		{ return "net_delay"; }
	const char *port_count() const		{ return "1/1"; }
	const char *processing() const		{ return "h/h"; }

	int initialize(ErrorHandler *errh);
	int configure(Vector<String>&, ErrorHandler *);
	void push(int port, Packet *p);
	void run_timer(Timer *);

private:
        priority_queue<delay_unit,delay_compare> prio_q;
        delay_unit d;
        int q_top;  //top key of the queue
        struct timeval now;
        int pkt_delay;

        Timer _timer;
};

CLICK_ENDDECLS


#endif

