#ifndef TRANSMISSIONTIMER_H
#define TRANSMISSIONTIMER_H

#include <time.h>
#include <pthread.h>
#include "common.h"

using namespace std; 

class OutgoingConnection; 

class TransmissionTimer
{
  friend class OutgoingConnection;
public:
    /*
     * Initialize: Associate a timer with a connection
     */
    TransmissionTimer(OutgoingConnection *txConnection);
    
    /**
    * Function to start a timer which will expire after a certain delay
    * @param delay: the timing delay in seconds.
    */
    void startTimer(float delay);
    
    /**
      * Function to stop a timer
      */
    void stopTimer();
public:
    /** Function to create a seperate thread for this timer
      * it will call timerHandler() function of the port_
      */
    static void *timerProc(void *arg);
protected:
    /**
    * port the timer belongs to
    */
    OutgoingConnection *outgoingConn_;
    /**
    * delay variable used by nanosleep()
    */
    struct timespec tdelay_;
    /**
    * thread id variable
    */
    pthread_t tid_;
};

#endif // TRANSMISSIONTIMER_H
