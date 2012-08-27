#include <vector>

#ifndef SENDER_H
#define SENDER_H
#include "Sender.h"
#endif

#ifndef REQUEST_H
#define REQUEST_H
#include "Request.h"
#endif

/* global structure to hold all requests and bookkeep their status */
vector<Request*> req_tab;

/* list of senders - for distributing requsts to */
vector<Sender*> senders;

#define ACK_RECV_DRAIN_TIME_SECS 10

#define DEFAULT_NUM_SENDERS 1
#define MAX_NUM_SENDERS 8
