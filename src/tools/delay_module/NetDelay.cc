#include <click/config.h>
#include <click/error.hh>
#include "NetDelay.hh"

CLICK_DECLS
NetDelay::NetDelay(): sendTimer(this),queueTop(-1)
{    
    delayTable = new HashTable<IPAddress,int>(0);
}

NetDelay::~NetDelay() {
  delete delayTable;
}

int NetDelay::initialize(ErrorHandler *errh)
{
  sendTimer.initialize(this);

  return 0;
}


int NetDelay::configure(Vector<String> &conf, ErrorHandler *errh) { 
    // Does nothing
    return 0;
}

int NetDelay::live_reconfigure(Vector<String> &conf, ErrorHandler *errh) {
  click_chatter("Reconfiguring GNRS delay module.");
  HashTable<IPAddress,int> newDelayTable(0);

  // Iterator increment happens within the loop 3-at-a-time
  for(Vector<String>::iterator it = conf.begin(); it != conf.end();)
  {
    click_chatter("Parsing IP addr: %s", (*it).c_str());
    // First item is the IP address
    IPAddress ipaddr = new IPAddress((*it));
    // Increment to the port #
    it++;
    click_chatter("Parsing port value: %s", (*it).c_str());
    int port;
    int success = sscanf((*it).c_str(),"%d",&port);
    if(!success) {
        click_chatter("Unable to parse port from %s", (*it).c_str());
        break;
    }
    // Increment to the delay #
    it++;
    click_chatter("Parsing delay value: %s", (*it).c_str());
    int delay;
    success = sscanf((*it).c_str(),"%d",&delay);
    if(!success){
        click_chatter("Unable to parse port from %s",(*it).c_str());
        break;
    }

    newDelayTable.set(ipaddr,delay);

    // Prep for next loop iteration
    it++;
  }
  delayTable = &newDelayTable;
    return 0;
}

void NetDelay::push(int port, Packet *p) {

	delayUnit.pkt=p;
	int pkt_delay=500;  //delay for the incoming packet, need to be configured later. unit: ms
	click_gettimeofday(&now);
    delayUnit.clockTime=now.tv_sec*1000+now.tv_usec/1000 + pkt_delay;
#ifdef DEBUG
click_chatter("got a packet with time value: %d ms",d.clockTime);
#endif

	packetQueue.push(delayUnit);
#ifdef DEBUG
click_chatter("pkt queue size: %d", packetQueue.size());
#endif

	if(queueTop==-1)  {
		queueTop=delayUnit.clockTime;
                sendTimer.schedule_after_msec(pkt_delay);
        }
        else if(packetQueue.top().clockTime!=queueTop)  {
                queueTop=packetQueue.top().clockTime;
                sendTimer.unschedule();
                sendTimer.schedule_after_msec(pkt_delay);
        }
}


void NetDelay::run_timer(Timer *) {
	
	click_gettimeofday(&now);
#ifdef DEBUG
click_chatter("delay timer fires at: %d ms", now.tv_sec*1000+now.tv_usec/1000);
#endif
        Packet *somePacket;
        somePacket = packetQueue.top().pkt;
        packetQueue.pop();
#ifdef DEBUG
click_chatter("pkt queue size: %d", prio_q.size());
#endif

	if(packetQueue.empty()==false)  {
	        queueTop=packetQueue.top().clockTime;
	       	//click_gettimeofday(&now);
	        int pkt_delay=queueTop-now.tv_sec*1000-now.tv_usec/1000;
#ifdef DEBUG
click_chatter("restart timer with with timer value: %d ms, timer will fire at: %d ms",pkt_delay,queueTop);
#endif
	        sendTimer.reschedule_after_msec(pkt_delay);
	}
	else
		queueTop=-1;	

	output(0).push(somePacket);
}


CLICK_ENDDECLS
ELEMENT_REQUIRES(linuxmodule)
EXPORT_ELEMENT(NetDelay)

