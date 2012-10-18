#ifndef _STATISTICS_H
#define _STATISTICS_H

#include <map>
#include <stdint.h>
#include "../common/gnrsconfig.h"
using namespace std;

#define STAT_STEP 1


struct pkt_sample_data {
	struct timespec starttime;
	struct timespec endtime;
};

typedef map<uint32_t,pkt_sample_data> PKT_SAMPLE_MAP;   //key is req_id

void* statisticsProc(void* arg);
int startStatistics(float delay);
int sampling_output();


#endif
