#include <click/config.h>
#include <click/error.hh>
#include <clicknet/udp.h>
#include <clicknet/ip.h>
#include <click/integers.hh>
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
#ifdef DEBUG
        click_chatter("%llu -> %d@%d",hash, delay, &delay);
#endif
        newDelayTable->set(hash,delay);

        // Prep for next loop iteration
        it++;
    }
    const HashTable<uint64_t,int> *tmpTable = delayTable;
    delayTable = newDelayTable;
    delete tmpTable;
#ifdef DEBUG
    click_chatter("Iterating new delay table.");
    for(HashTable<uint64_t,int>::const_iterator it = delayTable->begin(); it!=delayTable->end(); ++it){
       click_chatter("%llu -> %d",it.key(),it.value());
    }
#endif
    return 0;
}

void NetDelay::push(int port, Packet *p) {
#ifdef DEBUG
    click_chatter("DelayMod: Received a packet.");
#endif
    uint64_t hash = 0;
    delayUnit.pkt=p;
    // Extract the IP address
    click_ip *iph = p->ip_header();
    uint32_t sourceIP = iph->ip_src.s_addr;
    hash = ((uint64_t)sourceIP) << 32;
#ifdef DEBUG
    IPAddress ip(iph->ip_src);
    click_chatter("Created IPAddress.");
    click_chatter("DelayMod: Finished IP hash (%s)", ip.unparse().c_str());
#endif

    // Extract the src UDP port
    click_udp *udph = p->udp_header();
    uint16_t srcPort = net_to_host_order(udph->uh_sport);
    uint16_t dstPort = net_to_host_order(udph->uh_dport);
    hash |= ((uint64_t)srcPort)<<16;
#ifdef DEBUG
    click_chatter("DelayMod: Finished port hash (%i -> %i)", srcPort, dstPort);
#endif
#ifdef DEBUG
    click_chatter("DelayMod: H: %llu", hash);
#endif

    int pkt_delay=delayTable->get(hash); 
#ifdef DEBUG
    click_chatter("Mapped delay: %d@%d",pkt_delay,&pkt_delay);
#endif
    if(pkt_delay > 0){
#ifdef DEBUG
        click_chatter("Delaying packet by %dms.",pkt_delay);
#endif
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

/*
 * Called whenever this object's timer fires.
 * Checks the head of the priority queue; if it is ready to process it hands
 * it to the element's output, otherwise reschedules the timer.
 */
void NetDelay::run_timer(Timer *) {
    // Track the current time
    click_gettimeofday(&now);
#ifdef DEBUG
    click_chatter("delay timer fires at: %d ms", now.tv_sec*1000+now.tv_usec/1000);
#endif
    // Grab the head of the queue
    DelayUnit topUnit = packetQueue.top();
    Packet *somePacket = topUnit.pkt;
    int pktTime = topUnit.clockTime;
    int currTime = now.tv_sec*1000 + now.tv_usec/1000;
    /*
     * Difference from scheduled send and now. A negative value indicates
     * we've waited too long, a positive value means we're early
     */ 
    int timeDifference = pktTime-currTime;   

    // The timer fired too early, so let's reschedule
    if(timeDifference > TIMER_TOLERANCE_MSEC) {
        queueTop = pktTime;
        sendTimer.reschedule_after_msec(timeDifference);
    }
    // We're on time or late, so send the packet on its way
    else {
    #ifdef DEBUG
        if(timeDifference < -TIMER_TOLERANCE_MSEC) {
            click_chatter("Packet was delayed more than expected.");
        }
    #endif
        // Remove the top of the queue
        packetQueue.pop();
    #ifdef DEBUG
        click_chatter("pkt queue size: %d", packetQueue.size());
    #endif
        // If the queue is not empty, then reschedule the timer
        if(!packetQueue.empty())  {
            queueTop=packetQueue.top().clockTime;
            //click_gettimeofday(&now);
            int pkt_delay=queueTop-now.tv_sec*1000-now.tv_usec/1000;
    #ifdef DEBUG
            click_chatter("restart timer with with timer value: %d ms, timer will fire at: %d ms",pkt_delay,queueTop);
    #endif
            sendTimer.reschedule_after_msec(pkt_delay);
        }
        // Queue is empty, invalidate the top pointer
        else {
            queueTop=-1;	
        }

        output(0).push(somePacket);
    }
}


CLICK_ENDDECLS
ELEMENT_REQUIRES(linuxmodule)
EXPORT_ELEMENT(NetDelay)

