#include <queue>
#include <boost/asio.hpp>

#include "../common/gnrsconfig.h"


#ifndef REQUEST_H
#define REQUEST_H
#include "Request.h"
#endif

using boost::asio::ip::udp;

#define MAX_RESPONSE_LENGTH 1024

/**
 * Receiver receives responses from service and completes
 * the accounting for corresponding outstanding request 
 */

class Receiver{

private:
	bool done;
	boost::asio::io_service *io_service;
	udp::socket *udp_sock;
	void handle_recv(udp::endpoint& sender_ep, const char* msg, size_t len);

public:
	Receiver(boost::asio::io_service* io):done(false),io_service(io){
	
		//socket to listen for responses from server
		udp_sock = new udp::socket(*io_service, 
			udp::endpoint(udp::v4(), GNRSConfig::client_listen_port));
	}
	void run();
	void set_done(){done = true;}
	
	/**
	 * 
	 *
	 */
	void shutdown(){
		cout << "DEBUG: " << "Receiver: shutdown" << endl;
		try{
			udp_sock->shutdown(
				boost::asio::ip::udp::socket::shutdown_receive);
			udp_sock->close();
		}catch(exception &e){/*ignore*/}}

};
