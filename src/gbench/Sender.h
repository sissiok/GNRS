#include <queue>
#include <boost/asio.hpp>

#ifndef REQUEST_H
#define REQUEST_H
#include "Request.h"
#endif

#define SEND_QUEUE_SIZE 50 //max requests waiting to be sent
#define Q_EMPTY_WAIT_TIME_SECS 1

using boost::asio::ip::udp;

/**
 * Sender constructs and sends request packets to service
 * based on set trace file. It also adds the sent request info
 * into a tracking data structure to account responses received 
 * from the service.
 */

class Sender{

private:
	int my_id;
	bool done;
	queue<Request*> in_q;
	boost::asio::io_service* io_service;
	udp::socket *udp_sock;
	void udp_send(const char* payload, size_t len);

public:
	Sender(int id, boost::asio::io_service* io):
					my_id(id), done(false),io_service(io){
		udp_sock = new 
			udp::socket(*io_service, udp::endpoint(udp::v4(), 0));
	}

	/* return 0 on success, -1 on failure or queue full */
	int add_req(Request* req);
	void run();
	void set_done(){done = true;}
};
