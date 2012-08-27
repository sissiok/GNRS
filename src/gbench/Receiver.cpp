#include <iostream>
#include <boost/asio.hpp>

#ifndef MESSAGES_H
#define MESSAGES_H
#include "../common/Messages.h"
#endif

#ifndef RECEIVER_H
#define RECEIVER_H
#include "Receiver.h"
#endif

using namespace std;
using boost::asio::ip::udp;

void Receiver::run(){

	cout << "INFO: " << "[Receiver started]" << endl;

	//UDP receive implementation

	while(!done){
		char msg[MAX_RESPONSE_LENGTH];
		udp::endpoint sender_ep;
		try{
			size_t len = udp_sock->receive_from(
			boost::asio::buffer(msg, MAX_RESPONSE_LENGTH), 
			sender_ep);
			if(len == 0){
				//when sock is cleaned up, also manifests as
				//0 byte recv!!??
				if(!done){
					cout << "WARNING: " 
					<< " Recvd 0 bytes from: " 
					<< sender_ep.address().to_string() 
					<< endl;
				}
			}else if(len < sizeof(common_header_t)){
				/* can't parse this, skip */
				cout << "ERROR: " << "Recvd too short msg (" 
				<< len << " bytes) from " 
				<< sender_ep.address().to_string() << endl;
			}else{
				handle_recv(sender_ep, (const char*)msg, len);
			}
		}catch(exception &e){
			cout << "WARNING: " << " Receiver thread exception: " 
			<< e.what() << endl;
		}
	}
}

void Receiver::handle_recv(udp::endpoint& sender_ep, 
				const char* msg, size_t len){

	common_header_t *hdr = (common_header_t *)msg;
	message_type_t type = (message_type_t)hdr->type;
	cout << "DEBUG: " << "Recvd msg (id: " << hdr->req_id << ", type: " 
	<< type << ", bytes: " << len << ") from " 
	<< sender_ep.address().to_string() << endl;
}
