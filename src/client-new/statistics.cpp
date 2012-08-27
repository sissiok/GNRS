#include <iostream>
#include <fstream>
#include <sys/time.h>
#include <time.h>
#include "pthread.h"

#include "../common/time_measure.h"
#include "statistics.h"
using namespace std;

extern int rec_insert_num,rec_lookup_num,snt_insert_num,snt_lookup_num;
extern PKT_SAMPLE_MAP _pkt_sample;


/**
*  Create a seperate thread for this timer
*  it will periodically print out statistical resuts to file
*/
void* statisticsProc(void* arg)
{
  ofstream ProcFile;
  char filename[256];
  sprintf(filename,"/var/log/gnrs_proc_statistics-%d.data",GNRSConfig::client_listen_port);
  ProcFile.open(filename); 
  if (!ProcFile.is_open()){	
	cerr << "Can't open OUTPUT gnrs statistics File !!! Returning..." <<endl;
	return NULL;
  } 
  struct timespec* tdelay_=(struct timespec*)arg;
  long long int i=0;
  while(1)  {
	nanosleep(tdelay_, NULL);
	ProcFile<<i<<" "<<snt_insert_num<<" "<<rec_insert_num<<" "<<snt_lookup_num<<" "<<rec_lookup_num<<endl;
	i++;
  }
  return NULL;
}

/**
* Start a thread which will print out statistical resuts every delay seconds
* @param delay: the timing delay in seconds.
*  statistical results now contains insert_num and lookup_num that's received
*/
int startStatistics(float delay)
{
  struct timespec* tdelay_=new struct timespec;
  pthread_t tid_;
  tdelay_->tv_nsec = (long int)((delay - (int)delay)*1e9);
  tdelay_->tv_sec =  (int)delay; 
  int error = pthread_create(&tid_, NULL, &statisticsProc, (void *)tdelay_ );
  if (error)   {
     printf("Timer thread creation failed...\n");
     return 1;
  }
  //pthread_join(tid_,NULL);
  pthread_detach(tid_);
  return 0;
  //if(DEBUG>=1) cout<<"timer thread joined!"<<endl;
}


int sampling_output()
{
	ofstream ProcFile_;
	ProcFile_.open("/var/log/pkt_sampling_output.data"); 
	if (!ProcFile_.is_open()){
		cerr << "Can't open OUTPUT gnrs statistics File !!! Returning..." <<endl;
		return -1;
	} 
       PKT_SAMPLE_MAP::iterator _it;
	for ( _it=_pkt_sample.begin() ; _it != _pkt_sample.end(); _it++)  {
		if((*_it).second.endtime.tv_sec>0)
	    		ProcFile_<< (*_it).first << " " <<timeval_diff((*_it).second.starttime,(*_it).second.endtime) << endl;
	}
	ProcFile_.close();
	return 0;

}


