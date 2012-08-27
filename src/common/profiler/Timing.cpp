/* 
 * Timing.cpp
 */

#include "symbols.h"
#include "symbols-threads.h"

#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>

#ifdef	WIN32
#include <afxwin.h>
#include <time.h>
#endif	// WIN32

#ifdef	SGI
#include <sys/time.h>
#include <stddef.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/syssgi.h>
#include <errno.h>
#include <unistd.h>
#endif	// SGI

//#include "cerrout.h"
#include "hwc.h"
#include "Timing.h"

       #include <sys/time.h>
       #include <sys/resource.h>
       #include <unistd.h>

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

//  The one and only TimingDB object.  The constructor finds
//  a suitable output file name and puts in the date.
//  The destructor dumps the accumulated timing info.


TimingDB    MrTimer;	// the one and only
#ifdef LINUX
int	   TimingDB::TimingType::m_indentationLevel;
#endif //LINUX
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

// TimingType

TimingDB::TimingType::TimingType(char* name, TimingType* dad)
{
    m_parent = dad;
    m_sibling = NULL;
    m_child = NULL;
    m_closed = false;

    int len = strlen(name);
    m_label = new char[len+1];
    strcpy (m_label, name);
    m_count = 0;
    m_total = 0.0;
    m_min = 99999999;
    m_max = 0;

    m_indentationLevel = 0;
}


TimingDB::TimingType::~TimingType()
{
    delete [] m_label;
    if (m_child) delete m_child;
    if (m_sibling) delete m_sibling;
}


void
TimingDB::TimingType::PrintTiming(ofstream& outputFile, double parentTime, int parentCount)
{
    double average;
    int index;

    for (index = 0; index < TimingType::m_indentationLevel; index++) {
	outputFile << "    ";
    }
    // just flag a peculiarity in the output file    
    if (!m_closed) outputFile << "*";
    if(strlen(m_label) <= 7 )
      outputFile << m_label << "\t\t\t" << " ";
    else if (strlen(m_label) <= 15 )
      outputFile << m_label << "\t\t" << " ";
    else if (strlen(m_label) < 24 )
      outputFile << m_label << "\t" << " ";
    else
      outputFile << m_label << " ";

    if (m_count == 0) {
	outputFile << "No samples taken\n";
	average = 0.0;
    } else {
	average = m_total / m_count;
	if ((m_firstSample > 1.2 * average) && (m_count > 1)) {
	    // first sample was anomalous
	    average = (m_total - m_firstSample) / (m_count - 1);
	}
	outputFile << average << " usecs\t";
	if (parentTime > 0.0) {
	    double countRatio = (parentCount > 0) ? (double)m_count / (double)parentCount : 1.0;
	    int percent = (int)((average * countRatio) / parentTime * 100);
	    outputFile << percent << "%\t";
	} else {
	    outputFile << "    ";
	}
	outputFile << m_min << " to " << m_max << "\t("<< m_count << ")\n";
    }

    if (m_child) {
      TimingType::m_indentationLevel++;
      m_child->PrintTiming(outputFile, average, m_count);
      TimingType::m_indentationLevel--;
    }
    if (m_sibling) {
      m_sibling->PrintTiming(outputFile, parentTime, parentCount);
    }
}


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

// TimingDB

TimingDB::TimingDB()
{
    int		index;
    time_t	timer;
    struct tm*	ourTime;
    STATSTRUCT	buf;
    char	filename[256];

    for (index = 0; index < MAXTHREADS; index++) {
	m_thread[index] = NULL;
	// well, we're hoping no thread has a system ID of null...
    }

    m_head = NULL;
    m_silent = false;
    VRTHREAD_MUTEX_INIT(&m_timingLock, "timingLock");
    HWC::Init();

#ifdef	TIME_OPERATIONS
    // Create the log file
    for (index=0; ; index++ ) {
	sprintf(filename, "/var/log/timings-%d.log", index);
	if (STAT(filename, &buf) == -1) break;
	// we've found first unused name
    }
    outputFile.open(filename);

    // Log current time
    time(&timer);
    ourTime = localtime(&timer);
    outputFile << "\n" << asctime(ourTime) << "\n";
    
    // Log execution platform
#ifdef	WIN32
#ifdef	WINDOWS_NATIVE
    outputFile << "Windows native execution\n";
#else	// !WINDOWS_NATIVE
    outputFile << "Voodoo II execution\n";
#endif	// WINDOWS_NATIVE
#endif	// WIN32

#ifdef	SGI
    outputFile << "SGI execution\n";
#endif	// SGI

#ifdef	LINUX
    outputFile << "Linux execution\n";
#endif	// LINUX
    
#endif	// TIME_OPERATIONS
}


TimingDB::~TimingDB()
{
#ifdef	TIME_OPERATIONS
    // print the results
    TimingType*	threadHead = m_head;

    // close all thread timing heads
    m_silent = true;
    while (threadHead) {
	RegisterTiming(threadHead->m_label);
	threadHead = threadHead->m_sibling;
    }

    if (m_head) m_head->PrintTiming(outputFile, 0.0, 0);
    outputFile.close();
#endif	// TIME_OPERATIONS 

    if (m_head) delete m_head;

    struct rusage resource_usage;
    getrusage(RUSAGE_SELF, &resource_usage);

    printf("\nswap#: %ld", resource_usage.ru_nswap);
}


void
TimingDB::AddLine(const char* newLine)
{
    outputFile << newLine;
}


void
TimingDB::DeclareTimingThread(char* name)
{
  //  VRTHREAD	thisThread = GET_THREADSYSID;
  VRTHREAD	thisThread  = (void*)pthread_self();

    int		index, cnt;

    // hash thread id
    index = (((long)thisThread)>>1) & THREADIDMASK;

    VRTHREAD_MUTEX_LOCK(&m_timingLock);
    for (cnt = 0; cnt < MAXTHREADS; cnt++) {
      //if (m_thread[index] == 0) {
      	if (m_thread[index] == (VRTHREAD)(NULL)) {
	    m_thread[index] = thisThread;
	    if (m_head) {
		TimingType* newTiming;
		newTiming = new TimingType(name, NULL);
		newTiming->m_sibling = m_head;
		m_head = newTiming;
	    } else {
		m_head = new TimingType(name, NULL);
	    }
	    m_currentTiming[index] = m_head;
	    	struct timespec t_;            //create reference value
	    clock_gettime(CLOCK_REALTIME, &reference[index]);
	    GET_NOW_clock_gettime(m_currentTiming[index]->m_start, reference[index]);
	    //GET_NOW(m_currentTiming[index]->m_start);
	    //printf("declare: %llu, ref: %llu, index: %d\n", m_currentTiming[index]->m_start, (unsigned long long)reference[index].tv_sec*1000000000 + reference[index].tv_nsec, index);
	    break;
	}
	index = (index + 1) & THREADIDMASK;
    }
    VRTHREAD_MUTEX_UNLOCK(&m_timingLock);
    if (cnt > MAXTHREADS) {
	// have to exit to avoid infinite recursion in FindMe() in
	// the error condition
	exit(-1);
    }
}


int
TimingDB::FindMe()
{
  VRTHREAD	thisThread  = (void*)pthread_self();

      //    VRTHREAD	thisThread = GET_THREADSYSID;
    int		index;

    // hash thread id
    index = (((long)thisThread)>>1) & THREADIDMASK;

    for (int cnt = 0; cnt < MAXTHREADS; cnt++) {
	if (m_thread[index] == thisThread) {
	  return index;
	}
	index = (index + 1) & THREADIDMASK;
    }
    const std::string thr_name = "Tomb of the Unknown Thread";
    DECLARE_TIMING_THREAD((char*)thr_name.c_str());
    return FindMe();
}


void
TimingDB::StartTiming(char* label)
{
    int	ego;
    ego = FindMe();

    if (!m_currentTiming[ego]->m_child) {
    	m_currentTiming[ego]->m_child =
	    new TimingType(label, m_currentTiming[ego]);
	m_currentTiming[ego] = m_currentTiming[ego]->m_child;
    }
    else {
    	m_currentTiming[ego] = m_currentTiming[ego]->m_child;
	while (strcmp(label, m_currentTiming[ego]->m_label)){
	    if (!m_currentTiming[ego]->m_sibling) {
		m_currentTiming[ego]->m_sibling =
		    new TimingType(label, m_currentTiming[ego]->m_parent);
		m_currentTiming[ego] = m_currentTiming[ego]->m_sibling;
		break;
	    }
	    m_currentTiming[ego] = m_currentTiming[ego]->m_sibling;
	}
    }

    //GET_NOW(m_currentTiming[ego]->m_start);
    GET_NOW_clock_gettime(m_currentTiming[ego]->m_start, reference[ego]);
	//printf("start: %llu, ref: %llu, index: %d\n ", m_currentTiming[ego]->m_start, (unsigned long long)reference[ego].tv_sec*1000000000 + reference[ego].tv_nsec, ego);
}


double
TimingDB::RegisterTiming(char* label)
{
    int ego = FindMe();

    if (m_currentTiming[ego] == NULL ||
	strcmp(m_currentTiming[ego]->m_label, label)) {	return(-1);
    } else {
	//double sample = USEC_SINCE(m_currentTiming[ego]->m_start);
	double sample = USEC_SINCE_CGT(m_currentTiming[ego]->m_start,reference[ego]);
	m_currentTiming[ego]->m_total += sample;
	if (m_currentTiming[ego]->m_count++ == 0) {
	    m_currentTiming[ego]->m_firstSample = sample;
	}
	if (sample > m_currentTiming[ego]->m_max) {
	    m_currentTiming[ego]->m_max = sample;
	}
	if (sample < m_currentTiming[ego]->m_min) {
	    m_currentTiming[ego]->m_min = sample;
	}

	m_currentTiming[ego]->m_closed = true;
	m_currentTiming[ego]= m_currentTiming[ego]->m_parent;
	return(sample);
    }
}








