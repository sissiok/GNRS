#include <click/config.h>
#include <click/error.hh>
#include <clicknet/udp.h>
#include <clicknet/ip.h>
#include "NetDelay.hh"

CLICK_DECLS
NetDelay::NetDelay(): sendTimer(this),queueTop(-1)
{    
    delayTable = new HashTable<uint64_t,int>(0);
}

NetDelay::~NetDelay() {
  freeTable(delayTable);
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

void NetDelay::freeTable(const HashTable<uint64_t,int>* table) {
  delete table;
}

int NetDelay::live_reconfigure(Vector<String> &conf, ErrorHandler *errh) {
  click_chatter("Reconfiguring GNRS delay module.");
  const HashTable<uint64_t,int> *newDelayTable = new HashTable<uint64_t,int>(0);

  // Iterator increment happens within the loop 3-at-a-time
  for(Vector<String>::iterator it = conf.begin(); it != conf.end();)
  {
    uint64_t hash = 0;
    #ifdef DEBUG
    click_chatter("Parsing IP addr: %s", (*it).c_str());
    #endif
    // First item is the IP address
    IPAddress ipaddr((*it));
    // Shift the 32-bit IP address into the upper 32 bits
    hash = ((uint64_t)(ipaddr.addr())) << 32;
    // Increment to the port #
    it++;
    if(it == conf.end()){
        click_chatter("Reached end of configuration file before parsing port!");
        freeTable(newDelayTable);
        return 1;
    }
    #ifdef DEBUG
    click_chatter("Parsing port value: %s", (*it).c_str());
    #endif
    int port;
    int success = sscanf((*it).c_str(),"%d",&port);
    if(!success) {
        click_chatter("Unable to parse port from %s", (*it).c_str());
        freeTable(newDelayTable);
        return 1;
    }
    // Put the port into bits [
    hash |= ((uint64_t)port) << 16;
    // Increment to the delay #
    it++;
    if(it == conf.end()){
        click_chatter("Reached end of configuration file before parsing delay!");
        freeTable(newDelayTable);
        return 1;
    }
    #ifdef DEBUG
    click_chatter("Parsing delay value: %s", (*it).c_str());
    #endif
    int delay;
    success = sscanf((*it).c_str(),"%d",&delay);
    if(!success){
        click_chatter("Unable to parse delay from %s",(*it).c_str());
        freeTable(newDelayTable);
        return 1;
    }

    if(newDelayTable->get(hash)){
        click_chatter("Delay table collision!");
        freeTable(newDelayTable);
        return 1;
    }

    newDelayTable->set(hash,delay);

    // Prep for next loop iteration
    it++;
  }
  const HashTable<uint64_t,int> *tmpTable = delayTable;
  delayTable = newDelayTable;
  delete tmpTable;
  return 0;
}

void NetDelay::push(int port, Packet *p) {
    uint64_t hash = 0;
	delayUnit.pkt=p;
    // Extract the IP address
    click_ip *iph = p->ip_header();
    uint32_t sourceIP = iph->ip_src.s_addr;
    hash = ((uint64_t)sourceIP) << 32;

    // Extract the src UDP port
    click_udp *udph = p->udp_header();
    uint16_t srcPort = udph->uh_sport;
    hash |= ((uint64_t)srcPort)<<16;

	int pkt_delay=delayTable->get(hash); 
    #ifdef DEBUG
    IPAddress ipa(iph->ip_src.s_addr);
    click_chatter("%s:%d %dms",ipa.s(), srcPort, pkt_delay);
    #endif
    if(pkt_delay > 0){
        click_gettimeofday(&now);
        delayUnit.clockTime=now.tv_sec*1000+now.tv_usec/1000 + pkt_delay;
        #ifdef DEBUG
        click_chatter("got a packet with time value: %d ms",delayUnit.clockTime);
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
    else { // No delay configured
        output(0).push(p);
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
click_chatter("pkt queue size: %d", packetQueue.size());
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

