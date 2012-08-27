#ifndef TIMEMEASURE_H
#define TIMEMEASURE_H

#include <stdlib.h>
#include <sys/time.h>
#include <time.h>

long long timeval_diff(struct timeval *difference,struct timeval *end_time,struct timeval *start_time);
unsigned long long timeval_diff(timespec start, timespec end);


#endif
