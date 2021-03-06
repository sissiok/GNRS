#include <click/config.h>
#include <click/error.hh>
#include <clicknet/udp.h>
#include <clicknet/ip.h>
#include <click/integers.hh>
#include "NetDelay.hh"

CLICK_DECLS
NetDelay::NetDelay(): sendTimer(this), recvCnt(0), sndCnt(0), sndFires(0)//,queueTop(-1)
{    
    delayTable = new HashTable<uint64_t,int>(0);
	pq_lock = false;	
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
#ifdef DEBUG_CFG
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
#ifdef DEBUG_CFG
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
#ifdef DEBUG_CFG
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
#ifdef DEBUG_CFG
        click_chatter("%llu -> %d@%d",hash, delay, &delay);
#endif
        newDelayTable->set(hash,delay);

        // Prep for next loop iteration
        it++;
    }
    const HashTable<uint64_t,int> *tmpTable = delayTable;
    delayTable = newDelayTable;
    delete tmpTable;
#ifdef DEBUG_CFG
    click_chatter("Iterating new delay table.");
    for(HashTable<uint64_t,int>::const_iterator it = delayTable->begin(); it!=delayTable->end(); ++it){
       click_chatter("%llu -> %d",it.key(),it.value());
    }
#endif
    return 0;
}

void NetDelay::push(int port, Packet *p) {
    Timestamp arrivalTs = Timestamp::now_steady();
    int64_t nowMsec = ((int64_t)arrivalTs.sec())*1000 + arrivalTs.msec();

	recvCnt++;
#ifdef DEBUG_PSH
    click_chatter("DMPsh: Received a packet @%lld.",nowMsec);
    click_chatter("DMPsh: Updated!.");
#endif
    uint64_t hash = 0;
    delayUnit.pkt=p;
    // Extract the IP address
    click_ip *iph = p->ip_header();
    uint32_t sourceIP = iph->ip_src.s_addr;
    hash = ((uint64_t)sourceIP) << 32;
#ifdef DEBUG_PSH
    IPAddress ip(iph->ip_src);
    click_chatter("DMPsh: Finished IP hash (%s)", ip.unparse().c_str());
#endif

    // Extract the src UDP port
    click_udp *udph = p->udp_header();
    uint16_t srcPort = net_to_host_order(udph->uh_sport);
    uint16_t dstPort = net_to_host_order(udph->uh_dport);
    hash |= ((uint64_t)srcPort)<<16;
#ifdef DEBUG_PSH
    click_chatter("DMPsh: Finished port hash (%i -> %i)", srcPort, dstPort);
#endif
#ifdef DEBUG_PSH
    click_chatter("DMPsh: H: %llu", hash);
#endif

    int pkt_delay=delayTable->get(hash); 
    
    if(pkt_delay > 0){
        // Calculate the actual delay for this packet
        delayUnit.clockTime = arrivalTs + Timestamp::make_msec(pkt_delay);

        //other threads spin while one enters critical section
        while(pq_lock.compare_and_swap(false, true) != true);

        bool empty = packetQueue.empty();
        packetQueue.push(delayUnit);
#ifdef DEBUG_PSH
        click_chatter("DMPsh: pkt queue size: %d", packetQueue.size());
#endif
        if(empty){
#ifdef DEBUG_PSH
        click_chatter("DMPsh: Scheduling for empty queue.");
#endif
            sendTimer.schedule_at_steady(arrivalTs+Timestamp::make_msec(pkt_delay));
        }
        else {
            Timestamp nextTs = packetQueue.top().clockTime;
            // This is probably wrong
            if(nextTs > Timestamp::now_steady()){
              sendTimer.unschedule();
              sendTimer.schedule_at_steady(nextTs);
#ifdef DEBUG_PSH
        click_chatter("DMPsh: Cancelling and rescheduling.");
#endif
            }
#ifdef DEBUG_PSH
        click_chatter("DMPsh: Finished non-empty queue.");
#endif
        }
        //leave critical section
        pq_lock = false;
    }
    else { // No delay configured
        output(0).push(p);
#ifdef DEBUG_PSH
        click_chatter("DMPsh: No delay configured.");
#endif
    }
}

/*
 * Called whenever this object's timer fires.
 * Checks the head of the priority queue; if it is ready to process it hands
 * it to the element's output, otherwise reschedules the timer.
 */
void NetDelay::run_timer(Timer *) {

	sndFires++;
    // Track the current time
    Timestamp nowTs = Timestamp::now_steady();
    int64_t currTime = ((int64_t)nowTs.sec())*1000 + nowTs.msec();

    Packet* deliverArr[DELIVER_BURST_LENGTH];
    int numPktsTx = 0;
#ifdef DEBUG_TIM
    click_chatter("DMRtim: Fired at %lld", currTime);
#endif
    //enter critical section
    while(pq_lock.compare_and_swap(false, true) != true);

    while(!packetQueue.empty() && (numPktsTx < DELIVER_BURST_LENGTH)){
      // Grab the head of the queue
      DelayUnit topUnit = packetQueue.top();
      if(topUnit.clockTime > nowTs){
        break;
      }
      deliverArr[numPktsTx] = topUnit.pkt;
      numPktsTx++;
      packetQueue.pop();
    }
    //leave critical section
    pq_lock = false;

    //push out burst packets
    for(int i = 0; i < numPktsTx; i++){
        output(0).push(deliverArr[i]);
	sndCnt++;
    }
    //enter critical section
    while(pq_lock.compare_and_swap(false, true) != true);
    // If the queue is not empty, then reschedule the timer
    if(!packetQueue.empty())  {
        sendTimer.schedule_at_steady(packetQueue.top().clockTime);
    }

    //leave critical section
    pq_lock = false;
	
	//periodic stats
        uint32_t div = sndCnt/10000;
        if(div*10000 == sndCnt){
		click_chatter("dmod: r %lu s %lu sf %lu", recvCnt, sndCnt, sndFires);
	}
}

CLICK_ENDDECLS
ELEMENT_REQUIRES(linuxmodule)
EXPORT_ELEMENT(NetDelay)

