/*
 * symbols-threads.h
 *
 *	Macros for cross-platforms thread-related operations.
 */

#ifndef VRTHREADS_H
#define VRTHREADS_H

///////////////////////////////////////////////////////////////////////////////
// THREADS STUFF
///////////////////////////////////////////////////////////////////////////////

// THREAD IDs:
//	0 -- main thread
//	1 -- sendFrame
//	2 -- balanceLoad
//	3 -- readFromBacks
//	4 -- drawFrame
//	5 -- Pulse
//	6 -- PulseConstant
//	20+ -- readFrame

#define VRTHREAD_HIGHEST	0
#define VRTHREAD_HIGH		1
#define VRTHREAD_MEDIUM 	2
#define VRTHREAD_LOW		3
#define VRTHREAD_LOWEST		4

///////////////////////////////////////////////////////////////////////////////

#ifndef WIN32

///////////////////////////////////////////////////////////////////////////////

#if	MULTI_THREADED

#include <pthread.h>
#include <semaphore.h>
#include <sched.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#ifdef LINUX

#include <gtk/gtk.h>
#include "/usr/include/gdk/gdkx.h"
#include <gtkgl/gtkglarea.h>

#endif

typedef	pthread_t VRTHREAD;

//-----------------------------------------------------------------------------
// thread local storage
//-----------------------------------------------------------------------------

#define DECLARE_THREADKEYSTORAGE  					\
    pthread_key_t THREADIDKEY =	2;

EXTERN pthread_key_t	THREADIDKEY;

#define CREATE_THREADKEYS { 						\
    pthread_key_create(&THREADIDKEY, NULL);				\
}

#define GET_KEY(key) 		(*(int*)(pthread_getspecific(key)))
#define GET_KEYPTR(key) 	((int*)(pthread_getspecific(key)))
#define SET_KEY(key,val)	{ *GET_KEYPTR(key) = val; }

#define GET_THREADIDKEY		GET_KEY(THREADIDKEY)
#define GET_THREADSYSID 	pthread_self()

#define REGISTER_THREAD(id) {						\
    int *tmp = new int(id); 						\
    pthread_setspecific(THREADIDKEY, tmp);				\
}

#define REGISTER_OGLCONTEXT(id)

//-----------------------------------------------------------------------------
// thread creation and scheduling
//-----------------------------------------------------------------------------

#define VRTHREAD_ATTR_T	pthread_attr_t

#ifdef	_DEBUG
#define SET_SCOPE_SYSTEM(attrPtr)
#else	// _DEBUG
#define	SET_SCOPE_SYSTEM(attrPtr) {					\
    int _kr = pthread_attr_setscope(attrPtr, PTHREAD_SCOPE_SYSTEM);	\
    if (_kr != 0)							\
        cerr << "** WARNING **: Failed to set scheduling scope to system\n"; \
}
#endif	// _DEBUG
    
#define VRTHREAD_INIT_ATTR(attrPtr) {					\
    pthread_attr_init(attrPtr);						\
    SET_SCOPE_SYSTEM(attrPtr);						\
}

#define VRTHREAD_CREATE(threadPtr, attrPtr, procName,dataPtr) {		\
    int stat = pthread_create (threadPtr, attrPtr, procName, dataPtr); 	\
    if (stat != 0) {							\
	cerr << "pthread_create() failed!";				\
	abort();							\
    }									\
}

#define VRTHREAD_JOIN(tid) pthread_join((tid), NULL);


// Define to run at higher priority than system daemons on IRIX
// #define USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS

#ifdef	USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS
static const int basePrio = 111;
#else	// USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS
static const int basePrio = 2;
#endif	// USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS

static const int prio0 = basePrio;
static const int prio1 = basePrio + 1;
static const int prio2 = basePrio + 2;
static const int prio3 = basePrio + 3;
static const int prio4 = basePrio + 4;
static const int prio5 = basePrio + 5;
static const int prio6 = basePrio + 6;
static const int prio7 = basePrio + 7;
static const int prio8 = basePrio + 8;
static const int prio9 = basePrio + 9;

#define VRTHREAD_SET_THREAD_PRIO(TID, PRILEVEL) {			\
    struct sched_param schedp;						\
    int policy;								\
    pthread_getschedparam (TID, &policy, &schedp);			\
    if (policy != SCHED_RR)						\
        cerr << "** WARNING **: Thread scheduling policy is not RR!\n";	\
    assert(((PRILEVEL >= prio0) && (PRILEVEL <= prio9)),		\
	   "Illegal priority level " << PRILEVEL);			\
    schedp.sched_priority = (PRILEVEL);					\
    int stat = pthread_setschedparam (TID, policy, &schedp);		\
    if (stat != 0) {	                                                \
       	cerr << "pthread_setschedparam() failed";			\
	abort();							\
    }									\
}

#define VRTHREAD_SETPRIOITY(PRILEVEL) {				\
    VRTHREAD myTid = pthread_self ();					\
    VRTHREAD_SET_THREAD_PRIO(myTid, PRILEVEL);				\
}
#ifdef SGI
#define YIELD_PROC	sginap(0)
#endif //SGI
#ifdef LINUX
#define YIELD_PROC	sleep(0)
#endif //LINUX

// SLEEP(x) sleeps for x ms.
// sginap(x) sleeps for x ticks.  Measured each tick to be 10ms on the O2.
#ifdef SGI
#define SLEEP(x)	{int more = (x)/10; while (more=sginap(more));}
#endif //SGI
#ifdef LINUX
#define SLEEP(x)	{int more = (x)/1000; sleep(more);}
#endif //LINUX

//-----------------------------------------------------------------------------
// thread synchronization
//-----------------------------------------------------------------------------

class criticalSection;

typedef pthread_mutex_t PTHREAD_MUTEX;
#define PTHREAD_MUTEX_INIT(mutexPtr)	pthread_mutex_init(mutexPtr, NULL)
#define PTHREAD_MUTEX_LOCK(mutexPtr)	pthread_mutex_lock(mutexPtr)
#define PTHREAD_MUTEX_UNLOCK(mutexPtr)	pthread_mutex_unlock(mutexPtr)

typedef criticalSection VRTHREAD_MUTEX;
#define VRTHREAD_MUTEX_INIT(mutexPtr, name)	(mutexPtr)->setName(name)
#define VRTHREAD_MUTEX_LOCK(mutexPtr)		(mutexPtr)->lock()
#define VRTHREAD_MUTEX_UNLOCK(mutexPtr)		(mutexPtr)->unlock()

typedef sem_t VRTHREAD_SEMAPHORE;
#define VRTHREAD_SEMAPHORE_INIT(semPtr)		sem_init(semPtr,0,0)
#define VRTHREAD_SEMAPHORE_WAIT(semPtr) {				\
    int __err_;								\
    do {								\
        __err_ = sem_wait((semPtr));					\
    } while ((__err_ == -1) && (errno == EINTR));			\
    if (__err_ == -1) {							\
	cerr << "sem_wait(" << semPtr << ") failed!\n";			\
	abort();							\
    }									\
}
#define VRTHREAD_SEMAPHORE_POST(semPtr) {				\
    int __err_ = sem_post(semPtr);					\
    if (__err_ == -1) {							\
	cerr << "sem_post(" << semPtr << ") failed!\n";			\
	abort();							\
    }									\
}

typedef pthread_cond_t VRTHREAD_CONDITION;
#define VRTHREAD_CONDITION_INIT(condPtr)		pthread_cond_init(condPtr,NULL)
#define VRTHREAD_CONDITION_WAIT(condPtr,lockPtr)	(lockPtr)->conditionWait(condPtr)
#define VRTHREAD_CONDITION_SIGNAL(condPtr)		pthread_cond_signal(condPtr)

#include "../Generic_Source/criticalSection.h"

//-----------------------------------------------------------------------------
// winLock: protects access to X server because Xlib is not thread-safe
//-----------------------------------------------------------------------------

EXTERN VRTHREAD_MUTEX winLock;

#ifdef LINUX 
#define	INIT_WINLOCK             
#define GRAB_WINLOCK            gdk_threads_enter();
#define RELEASE_WINLOCK         gdk_threads_leave();
#else
#define	INIT_WINLOCK 		VRTHREAD_MUTEX_INIT(&winLock, "winLock")
#define GRAB_WINLOCK 		VRTHREAD_MUTEX_LOCK(&winLock)
#define RELEASE_WINLOCK		VRTHREAD_MUTEX_UNLOCK(&winLock)
#endif //!LINUX

#else	// !MULTI_THREADED

typedef	void* VRTHREAD;

#define DECLARE_THREADKEYSTORAGE
EXTERN void* THREADIDKEY;

#define CREATE_THREADKEYS

#define GET_KEY(key)		0
#define GET_KEYPTR(key)		NULL
#define SET_KEY(key,val)

#define GET_THREADIDKEY		0
#define GET_THREADSYSID		0

#define REGISTER_THREAD(id)
#define REGISTER_OGLCONTEXT(id)

//-----------------------------------------------------------------------------
// thread creation and scheduling
//-----------------------------------------------------------------------------

#define VRTHREAD_ATTR_T void*

#define SET_SCOPE_SYSTEM(attrPtr)
#define VRTHREAD_INIT_ATTR(attrPtr)
#define VRTHREAD_CREATE(threadPtr, attrPtr, procName,dataPtr)
#define VRTHREAD_JOIN(tid)


// Define to run at higher priority than system daemons on IRIX
// #define USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS

#ifdef	USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS
static const int basePrio = 111;
#else	// USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS
static const int basePrio = 2;
#endif	// USE_HIGHER_PRIO_THAN_SYSTEM_DAEMONS

static const int prio0 = basePrio;
static const int prio1 = basePrio + 1;
static const int prio2 = basePrio + 2;
static const int prio3 = basePrio + 3;
static const int prio4 = basePrio + 4;
static const int prio5 = basePrio + 5;
static const int prio6 = basePrio + 6;
static const int prio7 = basePrio + 7;
static const int prio8 = basePrio + 8;
static const int prio9 = basePrio + 9;

#define VRTHREAD_SETPRIORITY(PRILEVEL)
#define YIELD_PROC

#define SLEEP(x)

//-----------------------------------------------------------------------------
// thread synchronization
//-----------------------------------------------------------------------------

class criticalSection;

typedef void* PTHREAD_MUTEX;

typedef void* VRTHREAD_MUTEX;
#define VRTHREAD_MUTEX_INIT(mutexPtr, name)
#define VRTHREAD_MUTEX_LOCK(mutexPtr)
#define VRTHREAD_MUTEX_UNLOCK(mutexPtr)

typedef void* VRTHREAD_SEMAPHORE;
#define VRTHREAD_SEMAPHORE_INIT(semPtr)
#define VRTHREAD_SEMAPHORE_WAIT(semPtr)
#define VRTHREAD_SEMAPHORE_POST(semPtr)

typedef void* VRTHREAD_CONDITION;
#define VRTHREAD_CONDITION_INIT(condPtr)
#define VRTHREAD_CONDITION_WAIT(condPtr,lockPtr)
#define VRTHREAD_CONDITION_SIGNAL(condPtr)
		
//-----------------------------------------------------------------------------
// winLock: protects access to X server because Xlib is not thread-safe
//-----------------------------------------------------------------------------

EXTERN VRTHREAD_MUTEX winLock;
#define INIT_WINLOCK
#define GRAB_WINLOCK
#define RELEASE_WINLOCK

#endif	// MULTI_THREADED

///////////////////////////////////////////////////////////////////////////////

#else	// !WIN32

///////////////////////////////////////////////////////////////////////////////

//#include "../../pcDraw/stdafx.h"
#include <afxwin.h>

typedef	CWinThread*	VRTHREAD;

//-----------------------------------------------------------------------------
// thread local storage
//-----------------------------------------------------------------------------

#define DECLARE_THREADKEYSTORAGE	DWORD WINLOCKKEY, THREADIDKEY, OGLCONTEXTKEY;

EXTERN DWORD WINLOCKKEY, THREADIDKEY, OGLCONTEXTKEY;

#define CREATE_THREADKEYS {WINLOCKKEY = TlsAlloc();  THREADIDKEY=TlsAlloc(); OGLCONTEXTKEY=TlsAlloc();}

#define REGISTER_THREAD(id) TlsSetValue(THREADIDKEY,(LPVOID)id)

#define GET_THREADIDKEY	((int)TlsGetValue(THREADIDKEY))
#define GET_THREADSYSID AfxGetThread()

#define REGISTER_OGLCONTEXT(id) TlsSetValue(OGLCONTEXTKEY,(LPVOID)id)
#define GET_OGLCONTEXTKEY	((GLContext*)TlsGetValue(OGLCONTEXTKEY))

//-----------------------------------------------------------------------------
// thread creation and scheduling
//-----------------------------------------------------------------------------

#define VRTHREAD_ATTR_T	void
#define VRTHREAD_INIT_ATTR(attrPtr)	/* Nothing */

#define VRTHREAD_CREATE(threadPtr, attrPtr, procName, dataPtr) 			\
  *threadPtr = AfxBeginThread (procName, dataPtr);

#define VRTHREAD_JOIN(tid)

#include <afxmt.h>

#define VRTHREAD_SETPRIORITY(PRILEVEL) {				\
  BOOL stat;								\
  switch (PRILEVEL) {						        \
    case VRTHREAD_HIGHEST:						\
    case VRTHREAD_HIGH:							\
      stat = AfxGetThread()->SetThreadPriority(THREAD_PRIORITY_ABOVE_NORMAL); \
      break;								\
    case VRTHREAD_MEDIUM:						\
      stat = AfxGetThread()->SetThreadPriority(THREAD_PRIORITY_NORMAL);	\
      break;								\
    case VRTHREAD_LOW:							\
    case VRTHREAD_LOWEST:						\
      stat = AfxGetThread()->SetThreadPriority(THREAD_PRIORITY_BELOW_NORMAL); \
      break;								\
   }									\
   CHECK ((stat), "SetThreadPriority() failed");			\
}

#define YIELD_PROC	Sleep(0)
#define	SLEEP(x)	Sleep(x)

//-----------------------------------------------------------------------------
// thread synchronization
//-----------------------------------------------------------------------------

// use CRITICAL_SECTION rather than CCriticalSection for cross-class synch
typedef CRITICAL_SECTION VRTHREAD_MUTEX;
#define VRTHREAD_MUTEX_INIT(mutexPtr, name)	::InitializeCriticalSection(mutexPtr)
#define VRTHREAD_MUTEX_LOCK(mutexPtr)		::EnterCriticalSection(mutexPtr);
#define VRTHREAD_MUTEX_UNLOCK(mutexPtr)		::LeaveCriticalSection(mutexPtr);

typedef HANDLE VRTHREAD_SEMAPHORE;
#define VRTHREAD_SEMAPHORE_INIT(semPtr) 	*semPtr = CreateSemaphore(NULL,0,1000,NULL);
#define VRTHREAD_SEMAPHORE_WAIT(semPtr) 	WaitForSingleObject(*semPtr,INFINITE);
#define VRTHREAD_SEMAPHORE_POST(semPtr)		ReleaseSemaphore(*semPtr,1,NULL);

// XXX There is an obvious race in the implementation of VRTHREAD_CONDITION_WAIT
// XXX here.  However, Windows 95 doesn't have what it takes to support
// XXX condition variables properly.
typedef VRTHREAD_SEMAPHORE VRTHREAD_CONDITION;
#define VRTHREAD_CONDITION_INIT(condPtr) VRTHREAD_SEMAPHORE_INIT(condPtr)
#define VRTHREAD_CONDITION_WAIT(condPtr,lockPtr) {		\
    VRTHREAD_MUTEX_UNLOCK(lockPtr);				\
    VRTHREAD_SEMAPHORE_WAIT(condPtr);				\
    VRTHREAD_MUTEX_LOCK(lockPtr);				\
}
#define VRTHREAD_CONDITION_SIGNAL(condPtr) VRTHREAD_SEMAPHORE_POST(condPtr)

// The following would be a correct implementation of condition variables but
// does not work on Windows 95.
//#define VRTHREAD_CONDITION(cond)		HANDLE cond
//#define VRTHREAD_CONDITION_INIT(condPtr) 	*condPtr = CreateEvent(NULL,0,0,NULL)
//#define VRTHREAD_CONDITION_WAIT(condPtr,lockPtr) 		\
//    { SignalObjectandWait(*lockPtr,*condPtr,INFINITE,0);	\
//      VRTHREAD_MUTEX_LOCK(lockPtr); }
//#define VRTHREAD_CONDITION_SIGNAL(condPtr)	PulseEvent(*condPtr)

//-----------------------------------------------------------------------------
// winLock: WIN32 is thread-safe so does not need a winLock
//-----------------------------------------------------------------------------

#define	INIT_WINLOCK
#define GRAB_WINLOCK
#define RELEASE_WINLOCK

#endif	// not WIN32

///////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////

#endif	// VRTHREADS_H

