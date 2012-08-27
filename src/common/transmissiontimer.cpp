#include "transmissiontimer.h"
#include "outgoingconnection.h"

class OutgoingConnection; 

TransmissionTimer::TransmissionTimer(OutgoingConnection* txConnection)
{
  outgoingConn_ = txConnection;  
  tdelay_.tv_nsec = 0;
  tdelay_.tv_sec =  0; 
}

/**
*  Create a seperate thread for this timer
*  it will call timerHandler() function of the connection
*/
void* TransmissionTimer::timerProc(void* arg)
{
  if(DEBUG>=1) cout<<"timerProc starts"<<endl;
  TransmissionTimer *th = (TransmissionTimer *)arg;
  nanosleep(&(th->tdelay_), NULL);
  th->outgoingConn_->timerHandler(); 
  return NULL;
}
 
/**
* Start a timer which will expire after a certain delay
* @param delay: the timing delay in seconds.
*/
void TransmissionTimer::startTimer(float delay)
{
  if(DEBUG>=1) cout<<"start timer"<<endl;
  tdelay_.tv_nsec = (long int)((delay - (int)delay)*1e9);
  tdelay_.tv_sec =  (int)delay; 
  int error = pthread_create(&tid_, NULL, &timerProc, this );
  if (error) 
    throw "Timer thread creation failed...";
  //pthread_join(tid_,NULL);
  pthread_detach(tid_);
  //if(DEBUG>=1) cout<<"timer thread joined!"<<endl;
}
/**
 * Stop a timer
 */
void TransmissionTimer::stopTimer()
{
  pthread_cancel(tid_);
  if(DEBUG>=1) cout<<"timer thread canceled:"<<tid_<<endl;
}
