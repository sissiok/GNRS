#ifndef RECEIVER_H
#define RECEIVER_H

#include <iostream>
#include <pthread.h>
#include "statistics.h"
#include "../common/common.h"
#include "../common/Messages.h"
#include "../common/incomingconnection.h"
#include "../common/gnrsconfig.h"

using namespace std;

struct Client_Condition {
        pthread_mutex_t mutex;
        pthread_cond_t condition;
        bool condition_set;
};

void* waitForAckReceiver(void*);

#endif
