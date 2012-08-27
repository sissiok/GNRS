/**
 * Benchmarking client to test performance of GNRS daemon.
 *
 */
#include <iostream>
#include <fstream>
#include <string>
#include <boost/asio.hpp>
#include <boost/thread.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/filesystem.hpp>
#include <boost/regex.hpp>

#ifndef GBENCH_H
#define GBENCH_H
#include "gbench.h"
#endif

#include "../common/gnrsconfig.h"


#ifndef REQUEST_H
#define REQUEST_H
#include "Request.h"
#endif

#ifndef RECEIVER_H
#define RECEIVER_H
#include "Receiver.h"
#endif

using namespace std;

/**
 * Distribute requests among the available senders by posting
 * to their queue. If insertion fails due queue being full or 
 * other errors, move onto the next available sender.
 * 
 * Current implementation follows a Round Robin scheme posting
 * STRIDE number of requests at a time to each sender
 */

void distribute_requests(){
	
	int num_senders = senders.size();
	unsigned long num_reqs = req_tab.size();	
	unsigned long r_cnt = 0;
	int STRIDE = 5;
	//RR distribution, STRIDE at a time
	cout << "DEBUG: " << "Distributing " << num_reqs 
		<< " requests among " << num_senders << " sender(s)..." << endl;
	while(r_cnt < num_reqs){
		int i;
		for(i = 0; i < num_senders && r_cnt < num_reqs; i++){
			int j;
			for(j = 0; j < STRIDE && r_cnt < num_reqs; 
								j++, r_cnt++){
				/* 
				 * discontinue adding to current sender 
				 * if queue is full or add fails
				 */
				if(senders[i]->add_req(req_tab[r_cnt]))break;
			}
		}
	}
}

/**
 * Read in GNRS requests from a file 
 *
 * Format of requests:
 * <ts> INSERT <sGUID> <sNA-list>
 * <ts> UPDATE <sGUID> <sNA-list>
 * <ts> LOOKUP <sGUID> <sNA-list> <dGUID> <dNA-list>
 *
 * Field definitions: 
 * relative-timestamp(ts) - integer
 * operation-type - {INSERT, UPDATE, LOOKUP}
 * GUID - upto 40 digits in hex
 * sGUID - GUID of source where request originates
 * dGUID - GUID of destination being looked up
 * NA-list - comma separated strings of network address
 * sNA-list, dNA-list - source and destination network bindings
 *
 * Fields are TAB separated
 *
 */

void read_in_requests(const char* filename){

	string line; 
	/* to serve as a transaction index */
	unsigned long req_no = 0;
	ifstream file(filename);
	boost::regex skip_line_exp("^\\w*#.*$|^\\w*$");
	if(file.is_open()){
		unsigned long line_no = 0;
		boost::cmatch what;
		while(file.good()){
			line_no++;
			getline(file, line);
			/* skip comment and blank lines using exp above */
			if(boost::regex_match(line.c_str(), what, 
							skip_line_exp)){
				continue;
			}
			/* valid line, parse the request */
			//cout << "DEBUG: " << line_no << ": " << line << endl;
			try{
				Request* r = Request::parse(line);
				r->set_id(req_no);
				req_no++;
				/* insert into global struct for bookkeeping */	
				req_tab.push_back(r);
				//cout << "DEBUG: " << "Added req: " << r->to_string() << endl;
			}catch(exception& e){
				cout << "FATAL: " <<  "Parse error at line: " 
					<< line_no << " in file: " 
					<< filename << endl;
				exit(1);
			}
		}
		file.close();
	}else{
		cout << "FATAL: " <<  "Unable to open file: " 
			<< filename << endl;
		exit(1);
	}
}

void print_usage(){

	cout << "Usage: ./gbench <config_file> <request_file> [<parallelism>]" 
		<< endl;
}

int main(int argc, const char* argv[]){

	/* process input parameters */
	
	if(argc < 3){
		print_usage();
		exit(0);
	}else{

		/* init config defaults */
		GNRSConfig::init_defaults();
			
		/* read, and update configuration settings from file */
		const char* conf_filename = argv[1];
		GNRSConfig::read_from_file(conf_filename);

		/* read in requests  from specified file */
		const char* req_filename = argv[2];
		boost::filesystem::path p(req_filename);
		try{
			if(exists(p)){
				read_in_requests(req_filename);
			}
		}catch(const boost::filesystem::filesystem_error& ex){
			cout << "FATAL: " << "Error opening request file: " 
				<< ex.what() << endl;	
			exit(1);
		}
	}
	int num_senders = DEFAULT_NUM_SENDERS;
	if(argc > 3){
		/* parallelism - number of sender threads */
		num_senders = atoi(argv[3]);
		if(num_senders > MAX_NUM_SENDERS){
			num_senders = MAX_NUM_SENDERS;
		}
		exit(0);
	}

	boost::asio::io_service io;
	
	/* create and start the sender/receiver threads */
	vector<boost::thread*> send_threads; 
	int i;
	for(i = 0; i < num_senders; i++){
		Sender* sender = new Sender(i, &io);
		boost::thread* send_thr = 
			new boost::thread(&Sender::run, sender);
		senders.push_back(sender);
		send_threads.push_back(send_thr);
	}
	Receiver recvr(&io);
	boost::thread* recv_thr = new boost::thread(&Receiver::run, &recvr);

	/* distribute requests among sender threads */
	distribute_requests();

	/* 
	 * signal send termination and join all senders before 
	 * bringing down the receiver. The senders will join
	 * once all accepted requests have been successfully sent
	 */
	for(i = 0; i < num_senders; i++){
		senders[i]->set_done();
		send_threads[i]->join();
	}
	cout << "DEBUG: " << "All senders done. Waiting " 
		<< ACK_RECV_DRAIN_TIME_SECS << " secs to drain ACKs..."<< endl;
	/* sleep some and signal the receiver to wrap up */	
	boost::asio::deadline_timer t(io, 
		boost::posix_time::seconds(ACK_RECV_DRAIN_TIME_SECS));
	t.wait();

	cout << "DEBUG: " << "Interrupting receiver..." << endl;
	recvr.set_done();
	recvr.shutdown();
	recv_thr->join();
	cout << "DEBUG: " << "Receiver exited" << endl;
}
