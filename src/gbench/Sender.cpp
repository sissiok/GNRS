#include <iostream>

#include <boost/asio.hpp>


#include "../common/gnrsconfig.h"


#ifndef SENDER_H
#define SENDER_H
#include "Sender.h"
#endif

using namespace std;
using boost::asio::ip::udp;

/* return 0 on success, -1 on failure or queue full */
int Sender::add_req(Request* req){
	if(in_q.size() > SEND_QUEUE_SIZE)return -1;
	in_q.push(req);
	cout << "DEBUG:" << " Added req# " 
		<< req->get_id() << " to sender q" << endl;
	return 0;
}

void Sender::run(){

	cout << "INFO: " << "[Sender " << my_id << " started]" << endl;

	while(!(done && in_q.empty())){
		/* read request from queue if available, packetize and
		 * send to designated gnrs server
		 */
		if(!in_q.empty()){
			Request* req = in_q.front();
			in_q.pop();
			cout << "DEBUG: " << "S" << my_id 
				<< ": Sending req# " << req->get_id() << endl;
			size_t len;
			const char* buf = req->get_payload_buffer(len);
			udp_send(buf, len);
		}else{
			/* q-empty - sleep some */	
			boost::asio::deadline_timer t(*io_service, 
				boost::posix_time::seconds(
						Q_EMPTY_WAIT_TIME_SECS));
			t.wait();
		}

	}
}

void Sender::udp_send(const char* payload, size_t len){

	udp::resolver resolver(*io_service);
	char port_str[10];

	sprintf(port_str, "%d", GNRSConfig::daemon_listen_port);
	//udp::resolver::query query(udp::v4(), "localhost", port_str);
	udp::resolver::query query(udp::v4(), GNRSConfig::server_addr, port_str);
	udp::resolver::iterator iterator = resolver.resolve(query);

	udp_sock->send_to(boost::asio::buffer(payload, len), *iterator);
}
