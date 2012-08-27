/*
 * Timing.h
 */

#ifndef	_UTILS_TIMING_H_
#define _UTILS_TIMING_H_

#include "symbols.h"
#include "symbols-threads.h"
#include "criticalSection.h"

#include <iostream>
#include <fstream>
#ifdef	WIN32
    #include <stdafx.h>
    #include <strstrea.h>
#else
    #include <sstream>
#endif	// WIN32

#include "hwc.h"

#ifdef LINUX
#include <time.h>
#endif

using namespace std;

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

// system independent macros first

// Define TIME_OPERATIONS to enable the detailed timings
// #define TIME_OPERATIONS

#ifdef	TIME_OPERATIONS

    #define DECLARE_TIMING_THREAD(x)	MrTimer.DeclareTimingThread(x)
    #define START_TIMING(x)	    	MrTimer.StartTiming(x)
    #define REGISTER_TIMING(x)  	MrTimer.RegisterTiming(x)

    #define TIMINGSOUT(x) {		\
        ostrstream nullStr;		\
        nullStr << x << ends;		\
	char* bp = nullStr.str();	\
	MrTimer.AddLine(bp);		\
    }

#else	// !TIME_OPERATIONS

    #define	DECLARE_TIMING_THREAD(x)
    #define	START_TIMING
    #define	REGISTER_TIMING(x)
    #define	TIMINGSOUT(x)

#endif	// TIME_OPERATIONS

#define SEC_DIFF(start, end) (TICKS2SECS(COUNTER_DIFF(start, end)))
#define MSEC_DIFF(start,end) (TICKS2MSECS(COUNTER_DIFF(start,end)))

#define SEC_SINCE(val) TimingDB::SecondsSince(val)
#define SEC_SINCE_CGT(val,ref) TimingDB::SecondsSince_CGT(val,ref)
#define MSEC_SINCE(val) (SEC_SINCE(val) * 1000.0)
#define USEC_SINCE(val) (SEC_SINCE(val) * 1000000.0)
#define USEC_SINCE_CGT(val,ref) (SEC_SINCE_CGT(val,ref) * 1000000.0)


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

// Timestamp using system time.  Don't know that this is used anymore but
// keep for now.

#ifdef	WIN32

#define	TIMESTAMP   CTime::GetCurrentTime().Format("%H:%M:%S")

#else	// !WIN32 (SGI | LINUX)

#include <time.h>

#define	TIMESTAMP   timestamp()

static inline char* timestamp() {
    time_t t;
    t = time(NULL);
    return asctime(localtime(&t));
}

#endif	// WIN32


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

// The critical class and it's one instantiation first

class TimingDB
{
public:
    
    TimingDB();
    ~TimingDB();

    void    DeclareTimingThread(char* name);
    void    StartTiming(char* label);
    //void    RegisterTiming(char* label);
    double    RegisterTiming(char* label);
    void    AddLine(const char* newLine);

    inline static double SecondsSince(VRTIMER_TYPE val) {
	VRTIMER_TYPE	now;
	GET_NOW(now);
	return SEC_DIFF(val, now);
    };

    inline static double SecondsSince_CGT(VRTIMER_TYPE val, struct timespec ref) {
	VRTIMER_TYPE	now;
	GET_NOW_clock_gettime(now,ref);
	return SEC_DIFF(val, now);
    };

private:

    class TimingType {
    public:
	TimingType(char* name, TimingType* dad);
	~TimingType();

	void PrintTiming(ofstream& outputFile, double parentTime, int parentCount);

	char*		m_label;
	double		m_total;
	double		m_min;
	double		m_max;
	int		m_count;
	VRTIMER_TYPE	m_start;
	double		m_firstSample;

	// true if we've seen the REGISTER_TIMING corresponding to the
	// last START_TIMING for this guy
	bool		m_closed;
	
	// link to timings that this timing encloses
	TimingType*	m_child;

	// link to other timings enclosed by this guy's parent
	TimingType*	m_sibling;

	// link to parent
	TimingType*	m_parent;

	static int		m_indentationLevel;
    };

    // we've got to do these timings on a per-thread basis...
    //enum { MAXTHREADS = 32, THREADIDMASK = 0x0000001f };
    enum { MAXTHREADS = 512, THREADIDMASK = 0x000001ff };

    int	    FindMe();

    ofstream		outputFile;

    VRTHREAD_MUTEX	m_timingLock;
    TimingType*		m_currentTiming[MAXTHREADS];
    TimingType*		m_head;
    VRTHREAD		m_thread[MAXTHREADS];
    struct timespec reference[MAXTHREADS];   //clock time reference for GET_NOW_clock_gettime 

    // turns off complaining about an unexpectd REGISTER_TIMING
    // (for use when printing final results -- see destructor)
    bool		m_silent;

};

extern TimingDB    MrTimer;	// the one and only
//TimingDB    MrTimer;	// the one and only

#endif	// _UTILS_TIMING_H_





