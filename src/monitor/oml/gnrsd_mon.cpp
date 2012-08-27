/*
 * OML Monitor for tracking GNRS daemon stats
 */

#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <unistd.h>
#include <oml2/omlc.h>
#include <ocomm/o_log.h>


#define MON_PERIOD_SECS 5

using namespace std;

struct running_stats{
	long insert_cnt;
	long update_cnt;
	long query_cnt;
};

static OmlMPDef running_stats_mpd[] = {
    {"insert_cnt", OML_LONG_VALUE},
    {"update_cnt", OML_LONG_VALUE},
    {"query_cnt", OML_LONG_VALUE},
    {NULL, (OmlValueT)0}
};

static OmlMP* stats_mp;


struct running_stats stats;
struct running_stats* stats_ptr;

void set_stats_ptr(struct running_stats* ptr){
	stats_ptr = ptr;
}

void run(){

	unsigned int count = 1;
	for(; ; count++){
		OmlValueU v[3];
		omlc_set_long(v[0], stats_ptr->insert_cnt);
		omlc_set_long(v[1], stats_ptr->update_cnt);
		omlc_set_long(v[2], stats_ptr->query_cnt);
		omlc_inject(stats_mp, v);

		char out[200];
		sprintf(out, "I:%lu U:%lu Q:%lu\n", 
			stats_ptr->insert_cnt, stats_ptr->update_cnt, 
			stats_ptr->query_cnt);
		cout << out;
		sleep(MON_PERIOD_SECS);
	}
}

int main(int argc, const char *argv[]){

	set_stats_ptr(&stats);

	//register OML measurement point
	if(omlc_init("gnrsd_mon", &argc, argv, NULL) == -1){
		cout << "FATAL: " << "Unable to init OML client" << endl;
		exit(1);
	}
	if((stats_mp = omlc_add_mp("running_stats", running_stats_mpd))
								 == NULL){
		cout << "FATAL: " << "Unable to add OML measurement pt." 
			<< endl;
		exit(1);
	}
	if(omlc_start() == -1){
		cout << "FATAL: " << "Unable start OML stream" << endl;
		exit(1);
	}

	run();
	omlc_close();

	return(0);
}
