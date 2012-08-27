#ifndef OUTGOINGCONNNECTIONWITHACK_H
#define OUTGOINGCONNNECTIONWITHACK_H

#define MAX_RESEND_NUMBER  5

#include "outgoingconnection.h"

class OutgoingConnnectionWithAck :public OutgoingConnection
{

public:
  inline OutgoingConnnectionWithAck(){numbOfResend =0;};
  inline ~OutgoingConnnectionWithAck(){delete lastPkt_;};
  inline void setACKflag(bool flag) {ackflag_ =flag;}
  inline bool isACKed(){return ackflag_;}
  inline void set_numbofresend(int n)  {numbOfResend = n;};
  inline void timerHandler()
  {
    if(DEBUG==1) cout<<"outgoingconnectionwithack timer hander starts"<<endl;
    if (!ackflag_)
      {       
       cerr << "Last PKT was not ACK.... " << endl; 
       if (numbOfResend < MAX_RESEND_NUMBER)
       {
	  numbOfResend++; 
	  cerr << " Re-sending.... " << numbOfResend << " th attempt " << endl; 
	  sendPack(lastPkt_);       
	  //schedule a timer again
	  timer_.startTimer(2.5);
       }
       else  {
	    numbOfResend=0;
	    cerr << " Stop retrying ... drop the packet " << endl;        
	}
      }              
  }  
private:
  bool ackflag_;
  int numbOfResend; 
public:
  //save the packet for resend
  Packet * lastPkt_;
};

#endif // OUTGOINGCONNNECTIONWITHACK_H
