/*
 * This class represents GNRS client object. 
 * 
 */
#ifndef GNRSCLIENT_H
#define GNRSCLIENT_H

#include "../common/outgoingconnectionwithack.h"
#include "../common/incomingconnection.h"
#include "../common/Messages.h"
#include "../common/gnrsconfig.h"
#include "../common/time_measure.h"

#define DEFAULT_CONFIG_FILENAME "./client.conf"

struct send_recv_thread_args{
  Address clientLocalAddress_ ; 
  Address daemonRemoteAddress_;
  OutgoingConnnectionWithAck *outConnection_; 
  uint16_t na_num;  //valid number of NA
  NA lookupResult[LOOKUP_MAX_NA];
  pthread_t timingThread;	
};

struct wait_ack_socket{
  IncomingConnection *incConnection_ ;
  Address *clientListAddress;
  bool is_delete;
};

struct Client_Condition {
        pthread_mutex_t mutex;
        pthread_cond_t condition;
        bool condition_set;
};


extern void* waitForAckReceiver(void* ptr);

class GNRSClient
{

public:
   GNRSClient(char filename[100], char client_addr[SIZE_OF_NET_ADDR],char server_addr[SIZE_OF_NET_ADDR],uint32_t _listen_port); 
	
   /*Constructor that take daemonAddr from config file 
   	+ Load daemonAddress from config file
        + init clientAddress 
   	+ init outConnection_
    */
   GNRSClient(Address* clientAddr);
   
   /*Constructor with given clientAddr and daemonAdrr
   	+ Load daemonAddress from config file
        + init clientAddress 
   	+ init outConnection_
    */
   GNRSClient(Address* clientAddr, Address* daemonAddr); 
   
   /* 
    * Insert operation 
    * @para: GUID
    * @return: 0 if success  and -1 otherwise  
    */
   int insert(char guid[SIZE_OF_GUID], NA *temp, uint16_t NA_num, uint32_t req_id);
   
   uint16_t lookup(char guid[SIZE_OF_GUID], NA* result_buf, uint32_t req_id);
   
   inline NA* getLookupResult(){ return lookupResult;};
   //void *waitForAckReceiver(void *ptr); 
   
     pthread_t receivingThread;
    struct timespec tdelay_; 
    void startTimer(float delay);
    //void stopTimer();
   static void *timerProc(void *arg);
    
private:
   int insertProc(char guid[SIZE_OF_GUID], NA *temp, uint16_t NA_num, uint32_t req_id);
   void lookupProc(char guid[SIZE_OF_GUID], uint32_t req_id); 
protected: 
  //connection to talk to GNRS daemon 
  OutgoingConnnectionWithAck *outConnection_; 
  IncomingConnection	*incConnection_; 

  Address *clientLocalAddress_ ; // for sending 
  Address *daemonRemoteAddress_; // for sending 

  uint16_t na_num;   //valid number of NA
  NA lookupResult[LOOKUP_MAX_NA];
  char configFileName[100]; 
  
  bool _is_ack_rev;  
};

#endif // GNRSCLIENT_H
