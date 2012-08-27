/*
 * criticalSection:
 *
 *	Allow a thread to enter the same critical section, via the locking
 *	of a mutex, multiple times.  This emulates WIN32 critical section
 *	semantics for pthreads.
 */

#ifndef	_CRITICAL_SECTION_H
#define _CRITICAL_SECTION_H

#include "symbols.h"
#include "symbols-threads.h"

#include <pthread.h>

#include <stdio.h>
#include <iostream>


class criticalSection
{
public:
    criticalSection() {
	name_ = "";
	threadID_ = (VRTHREAD)-1;
	count_ = 0;
	pthread_mutex_init((pthread_mutex_t*)&lock_, 0);
    }

    inline void setName(const char *name);
    inline void lock();
    inline void unlock();
    inline void conditionWait(VRTHREAD_CONDITION *condPtr);

private:
    const char*		name_;
    VRTHREAD		threadID_;
    int 		count_;
    PTHREAD_MUTEX	lock_;
};


inline void criticalSection::setName(const char *name)
{
    name_ = name;
}


inline void criticalSection::lock()
{
#if	MULTI_THREADED
    pthread_t threadID = pthread_self();
    if (threadID != threadID_) {
	// I'm not holding the lock; gotta wait!
	PTHREAD_MUTEX_LOCK(&lock_);
   
#ifdef	_DEBUG
	// Invariant: 1st time I grab the lock successfully,
	// threadID_ had better be cleared to -1 and count_
	// had better be 0.
	//printf("The ID is %d\n", threadID);
	if ((threadID_ != (pthread_t)-1) || (count_ != 0)) {
	    cerr << __FILE__ << ", " << __LINE__ << ": Funny state: ("
		 << GET_THREADIDKEY << ", " << threadID_ << ", " << count_
		 << " ID = " <<threadID <<"\n";
	}
#endif	// _DEBUG
	
	threadID_ = threadID;		// Yeah, I got it now!
    }
    count_++;
    //   printf("Is LOCKED now... count = %d\n", count_);
#endif	// MULTI_THREADED
}


inline void criticalSection::unlock()
{
#if	MULTI_THREADED
#ifdef	_DEBUG
    // Invariant: threadID_ had better be set to me and count_
    // had better be > 0.
    pthread_t threadID = pthread_self();
    if ((threadID != threadID_) || (count_ <= 0)) {
	cerr << __FILE__ << ", " << __LINE__ << ": Funny state: ("
	     << GET_THREADIDKEY << ", " << threadID << ", " <<
	    threadID_ << ", " << count_ << ")\n";
    }
#endif	// _DEBUG

    count_--;
    //  printf("Going to UNLOCKing now... count = %d\n", count_);    
    if (count_ == 0) {			// Really release the lock
	threadID_ = (pthread_t)-1;
	PTHREAD_MUTEX_UNLOCK(&lock_);

    }
#endif	// MULTI_THREADED
}
 

inline void criticalSection::conditionWait(VRTHREAD_CONDITION *condPtr)
{
#if	MULTI_THREADED
    // Save state because lock_ will be released while waiting for the
    // condition variable
    pthread_t threadID = threadID_;
    int count = count_;

    count_ = 0;
    threadID_ = (pthread_t)-1;
    pthread_cond_wait(condPtr, &lock_);	// This will release lock_

    count_ = count;	// restore state because we have the lock back again.
    threadID_ = threadID;
#endif	// MULTI_THREADED
}

#endif	// _CRITICAL_SECTION_H
