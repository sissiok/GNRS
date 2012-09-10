#include <click/config.h>
//#include <click/confparse.hh>
#include <click/error.hh>
#include "NetDelay.hh"

CLICK_DECLS
NetDelay::NetDelay(): _timer(this),q_top(-1)
{    
}

NetDelay::~NetDelay() {
}

int NetDelay::initialize(ErrorHandler *errh)
{
  _timer.initialize(this);

  return 0;
}


int NetDelay::configure(Vector<String> &conf, ErrorHandler *errh) {
  for(Vector<String>::iterator it = conf.begin(); it != conf.end(); ++it)
  {
    click_chatter((*it).c_str());
  }
    return 0;
}

int NetDelay::live_reconfigure(Vector<String> &conf, ErrorHandler *errh) {
  click_chatter("Reconfiguring GNRS delay module.");
  for(Vector<String>::iterator it = conf.begin(); it != conf.end(); ++it)
  {
    click_chatter((*it).c_str());
  }
    return 0;
}

void NetDelay::push(int port, Packet *p) {

	d.pkt=p;
	pkt_delay=500;  //delay for the incoming packet, need to be configured later. unit: ms
	click_gettimeofday(&now);
        d.clockTime=now.tv_sec*1000+now.tv_usec/1000 + pkt_delay;
#ifdef DEBUG
click_chatter("got a packet with time value: %d ms",d.clockTime);
#endif

	prio_q.push(d);
#ifdef DEBUG
click_chatter("pkt queue size: %d", prio_q.size());
#endif

	if(q_top==-1)  {
		q_top=d.clockTime;
                _timer.schedule_after_msec(pkt_delay);
        }
        else if(prio_q.top().clockTime!=q_top)  {
                q_top=prio_q.top().clockTime;
                _timer.unschedule();
                _timer.schedule_after_msec(pkt_delay);
        }
}


void NetDelay::run_timer(Timer *) {
	
	click_gettimeofday(&now);
#ifdef DEBUG
click_chatter("delay timer fires at: %d ms", now.tv_sec*1000+now.tv_usec/1000);
#endif
        Packet *_pkt;
        _pkt=prio_q.top().pkt;
        prio_q.pop();
#ifdef DEBUG
click_chatter("pkt queue size: %d", prio_q.size());
#endif

	if(prio_q.empty()==false)  {
	        q_top=prio_q.top().clockTime;
	       	//click_gettimeofday(&now);
	        pkt_delay=q_top-now.tv_sec*1000-now.tv_usec/1000;
#ifdef DEBUG
click_chatter("restart timer with with timer value: %d ms, timer will fire at: %d ms",pkt_delay,q_top);
#endif
	        _timer.reschedule_after_msec(pkt_delay);
	}
	else
		q_top=-1;	

	output(0).push(_pkt);
}


CLICK_ENDDECLS
ELEMENT_REQUIRES(linuxmodule)
EXPORT_ELEMENT(NetDelay)

