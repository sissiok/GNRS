/* 
 * hwcounter.cpp
 */

#include "hwc.h"


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

#ifdef WIN32

#include "cerrout.h"

VRTIMER_TYPE HWC::m_hwcFrequency;

void HWC::Init()
{
    // use QueryPerformanceCounter() and QueryPerformanceFrequency()
    QueryPerformanceFrequency(&m_hwcFrequency);
    if (hwcFrequency.QuadPart == 0) {
	CERRBOX ("HWC::Init: Unable to take timings.  Hardware does not have a high performance counter.\n");
    }
}

#endif //WIN32

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

#ifdef SGI

#include <sys/types.h>
#include <time.h>

volatile iotimer_t*	HWC::m_hwcNow;
float			HWC::m_usecsPerTick;

void HWC::Init()
{
    volatile VRTIMER_TYPE *iotimer_addr;
    __psunsigned_t phys_addr, raddr;
    int fd, poffmask;
    unsigned int resolution, size;

    // Get physical address and resolution of counter
    phys_addr = syssgi (SGI_QUERY_CYCLECNTR, &resolution);
    if (phys_addr == -1) {
        fprintf (stderr,
		 "HWC::Init: This machine does not have a hardware counter\n");
        m_hwcNow = (iotimer_t *) -1;
	m_usecsPerTick = 1;
	return;
    }

    // Get size of counter
    size = syssgi (SGI_CYCLECNTR_SIZE);

    // Map counter
    poffmask = getpagesize () - 1;
    raddr = phys_addr & ~poffmask;
    fd = open ("/dev/mmem", O_RDONLY);
    iotimer_addr = (volatile iotimer_t *)mmap(0, poffmask, PROT_READ,
                                              MAP_PRIVATE, fd, (off_t)raddr);
    m_hwcNow = (iotimer_t *)((__psunsigned_t)iotimer_addr +
			     (phys_addr & poffmask));

    // Compute usecs per tick
    m_usecsPerTick = resolution / RES_UNITS_PER_USEC;
}

#endif // SGI

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

#ifdef LINUX

float	HWC::m_usecsPerTick;
float	HWC::m_nsecsPerTick;

void HWC::Init()
{

    FILE *file;
    char s1[100], s2[100], s3[100];
    float mhz;
    int numTokens;

    file = fopen("/proc/cpuinfo", "r");
    if (file == NULL) {
	fprintf(stderr, "Could not open /proc/cpuinfo\n");
	m_usecsPerTick = 1;
	return;
    }

    while (1) {
	numTokens = fscanf(file, "%s: \n", s1);
	if (numTokens == EOF) {
	    fprintf(stderr, "Could not find MHz\n");
	    m_usecsPerTick = 1;
	    return;
	}
	if ((numTokens == 1)&&(strcmp(s1, "MHz") == 0)){
	  fscanf(file, "%s: \n", s1);
	  fscanf(file, "%f\n", &mhz);
	//printf("mhz: %f\n", mhz); 
	  m_usecsPerTick = 1.0 / mhz;	 
	  m_nsecsPerTick = 1000.0 / mhz;	 
	  break;
	}
    }
}

#endif // LINUX
