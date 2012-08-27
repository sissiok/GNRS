/*
 * hwcounter.h
 *
 *	GET_NOW is implemented as a macro instead of an inline funtion
 *	because it allows for the most flexibility in handling things
 *	like volatile timestamps and still be as efficient as possible.
 */

#ifndef	_HWCOUNTER_H_
#define _HWCOUNTER_H_

#ifdef	WIN32
#include <stdafx.h>
#endif	// WIN32

#ifdef	SGI
#include <stdio.h>
typedef unsigned int	VRTIMER_TYPE;
#endif	 // SGI

#ifdef	LINUX
#include <stdio.h>  
#include <string.h>      
#include <time.h>

typedef unsigned long long	VRTIMER_TYPE;
#endif	// LINUX


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

// Some usefule macros

#define COUNTER_DIFF(start, end) 	HWC::Diff(start, end)

#define SECS2TICKS(val)		HWC::Usecs2Ticks(val * 1000000)
#define MSECS2TICKS(val)	HWC::Usecs2Ticks(val * 1000)
#define USECS2TICKS(val)	HWC::Usecs2Ticks(val)

#define TICKS2SECS(val)		(HWC::Ticks2Usecs(val) / 1000000.0)
#define TICKS2MSECS(val)	(HWC::Ticks2Usecs(val) / 1000.0)
#define TICKS2USECS(val)	(HWC::Ticks2Usecs(val))

#define SEC2MSEC(val) ((val) * 1000)
#define SEC2USEC(val) ((val) * 1000000)
#define MSEC2SEC(val) ((double)(val) / 1000.0)
#define USEC2SEC(val) ((double)(val) / 1000000.0)
#define MSEC2USEC(val) ((val) * 1000)
#define USEC2MSEC(val) ((double)(val) / 1000.0)

#ifdef LINUX
#define NSECS2TICKS(val)	HWC::Nsecs2Ticks(val)
#define TICKS2NSECS(val)	(HWC::Ticks2Nsecs(val))
#endif

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

class HWC
{
public:

    // Initialize hardware counter
    static void Init();

    // The following functions are all inline - see implementations below

    // Compute end - start accounting for possible wrapping of counter
    static VRTIMER_TYPE Diff(VRTIMER_TYPE start, VRTIMER_TYPE end);

    // Convert from ticks to usec and vice versa
    static double Ticks2Usecs(VRTIMER_TYPE numTicks);
    static double Usecs2Ticks(double numUsecs);

#ifdef	LINUX
    // Convert from ticks to nsec and vice versa
    static double Ticks2Nsecs(VRTIMER_TYPE numTicks);
    static double Nsecs2Ticks(double numNsecs);
#endif	// LINUX

private:

#ifdef	WIN32
    static VRTIMER_TYPE		m_hwcFrequency;
#endif	// WIN32

#ifdef	SGI
    // These values are for the O2.  Most likely not right for other models.
    static const MAX_COUNTER_VALUE = 4294967295;
    static const RES_UNITS_PER_USEC = 1000000;	// pico seconds

    static volatile iotimer_t*	m_hwcNow;
    static float		m_usecsPerTick;
#endif	// WIN32

#ifdef	LINUX
    static float		m_usecsPerTick;
    static float		m_nsecsPerTick;
#endif	// LINUX
};


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

#ifdef	WIN32

#define	GET_NOW(timestamp) QueryPerformanceCounter(&(timestamp))

inline VRTIMER_TYPE HWC::Diff(VRTIMER_TYPE start, VRTIMER_TYPE end) {
    return end.QuadPart - start.QuadPart;
}

inline double HWC::Ticks2Usecs(VRTIMER_TYPE numTicks) {
    return (double)numTicks / (double)HWC::m_hwcFrequence.QuadPart * 1000000.0;
}

inline double HWC::Usecs2Ticks(double numUsecs) {
    return (double)numUsecs * (double)HWC::m_hwcFrequency.QuadPart / 1000000.0;
}

#endif	// WIN32

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

#ifdef	SGI

#define GET_NOW(timestamp) timestamp = *HWC::m_hwcNow

inline VRTIMER_TYPE HWC::Diff(VRTIMER_TYPE start, VRTIMER_TYPE end) {
    return (end > start) ? end - start
	                 : MAX_COUNTER_VALUE - start + end;
}

inline double HWC::Ticks2Usecs(VRTIMER_TYPE numTicks) {
    return (double)numTicks * m_usecsPerTick;
}

inline double HWC::Usecs2Ticks(double numUsecs) {
    return (double)numUsecs / m_usecsPerTick;
}

#endif	// SGI

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

#ifdef	LINUX

//#define GET_NOW(timestamp) \
   asm volatile (".byte 0x0f, 0x31" : "=A" (timestamp))
#define GET_NOW(timestamp) \
	struct timespec t; \
	clock_gettime(CLOCK_REALTIME, &t);\
	timestamp = NSECS2TICKS(t.tv_sec*1000000000 + t.tv_nsec)
	//printf("DEBUG: GET_NOW: %llu\n", timestamp)


#define GET_NOW_clock_gettime(timestamp,ref) \
	struct timespec t; \
	clock_gettime(CLOCK_REALTIME, &t);\
	timestamp = NSECS2TICKS((unsigned long long)(t.tv_sec-ref.tv_sec)*1000000000 + t.tv_nsec-ref.tv_nsec)

inline VRTIMER_TYPE HWC::Diff(VRTIMER_TYPE start, VRTIMER_TYPE end) {
    if (end <= start) {
      fprintf(stderr, "HWC::Diff: WARNING: Counter wrapped, returning 0\n");
      return 0;
    } else {
      return end - start;
    }
}

inline double HWC::Ticks2Usecs(VRTIMER_TYPE numTicks) {
    return (double)numTicks * m_usecsPerTick;
}

inline double HWC::Usecs2Ticks(double numUsecs) {
    return (double)numUsecs / m_usecsPerTick;
}

inline double HWC::Ticks2Nsecs(VRTIMER_TYPE numTicks) {
    return (double)numTicks * m_nsecsPerTick;
}

inline double HWC::Nsecs2Ticks(double numNsecs) {
    return (double)numNsecs / m_nsecsPerTick;
}
#endif	// LINUX

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

#endif	// _HWCOUNTER_H_
