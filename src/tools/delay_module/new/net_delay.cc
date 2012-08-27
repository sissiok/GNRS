#include <click/config.h>
#include <click/confparse.hh>
#include <click/error.hh>
#include <click/standard/scheduleinfo.hh>
#include "net_delay.hh"

CLICK_DECLS
net_delay::net_delay(): _timer(this)
{
	q_top.assign(-1);

}

net_delay::~net_delay() {
}

int
net_delay::initialize(ErrorHandler *errh)
{
 // ScheduleInfo::initialize_task(this,&_task,errh);
  _timer.initialize(this);

  return 0;
}


int net_delay::configure(Vector<String> &conf, ErrorHandler *errh) {
	if(cp_va_kparse(conf, this, errh,
                    cpEnd) < 0)
		return -1;
	return 0;
}

void net_delay::push(int port, Packet *p) {

	d.pkt=p;
	pkt_delay=5;  //delay for the incoming packet, need to be configured later. unit: ms
	//click_gettimeofday(&now);
	_ts=Timestamp::now_steady();
        //d.key=_ts.sec()*1000+_ts.usec() + pkt_delay;
	d.key.assign_usec(_ts.sec(),_ts.usec()+pkt_delay*1000);
	if(DEBUG>=1) click_chatter("got a packet with key value: %d ms",d.key.sec()*1000+d.key.msec());

	prio_q.push(d);
	//if(DEBUG>=1) click_chatter("pkt queue size: %d", prio_q.size());

	if(q_top.sec()==-1)  {
		q_top=d.key;
                //_timer.schedule_at_steady(q_top);
		_timer.schedule_after_msec(pkt_delay);
		if(DEBUG>=1) click_chatter("packet enters as queue top");
        }
        else if(prio_q.top().key!=q_top)  {
                q_top=prio_q.top().key;
                _timer.unschedule();
                //_timer.schedule_at_steady(q_top);
		_timer.schedule_after_msec(pkt_delay);
		if(DEBUG>=1) click_chatter("queue top change");
        }


/*	
	click_chatter( "testing!");

	struct timeval now;
	click_gettimeofday(&now);
	click_chatter( "time: %d",now.tv_sec);

	priority_queue<int> prio_q;
	prio_q.push(100);
	prio_q.push(50);
	prio_q.push(200);
	prio_q.push(20);
	click_chatter( "%d",prio_q.top());

	prio_q.pop();
	click_chatter( "%d",prio_q.top());
	
	output(0).push(p);
*/
}


void
net_delay::run_timer(Timer *) {

//	click_gettimeofday(&now);
	_ts=Timestamp::now_steady();
	if(DEBUG>=1) click_chatter("delay timer fires at: %d ms", _ts.sec()*1000+_ts.msec());
        Packet *_pkt;
        _pkt=prio_q.top().pkt;
        prio_q.pop();
//	if(DEBUG>=1) click_chatter("pkt queue size: %d", prio_q.size());

	if(prio_q.empty()==false)  {
	        q_top=prio_q.top().key;
		pkt_delay=q_top.sec()*1000+q_top.msec()-_ts.sec()*1000-_ts.msec();
//	       	click_gettimeofday(&now);
//	        pkt_delay=q_top-now.tv_sec*1000-now.tv_usec/1000;
//		if(DEBUG>=1) click_chatter("restart timer with with timer value: %d ms, timer will fire at: %d ms",pkt_delay,q_top);
	        //_timer.schedule_at_steady(q_top);
		if(pkt_delay<0)
			_timer.schedule_now();
		else
			_timer.schedule_after_msec(pkt_delay);
		if(DEBUG>=1) click_chatter("schedule timer to fire at: %d ms",q_top.sec()*1000+q_top.msec());
	}
	else
		//q_top=-1;	
		q_top.assign(-1);

	output(0).push(_pkt);
}


CLICK_ENDDECLS
EXPORT_ELEMENT(net_delay)
ELEMENT_REQUIRES(linuxmodule)

