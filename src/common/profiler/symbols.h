/*
 * symbols.h:
 *
 *	Useful defines, typdefs, etc. for different platforms.
 *	Currently includes WIN32, SGI (IRIX), and LINUX.
 */

#ifndef	_SYMBOLS_SYMBOLS_H
#define	_SYMBOLS_SYMBOLS_H

////////////////////////////////////////////////////////
// System wide defines
////////////////////////////////////////////////////////


#define EXTERN		extern

#ifndef	NULL
#define NULL		0
#endif	// NULL

typedef unsigned char u_char;


////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

#ifdef WIN32

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

// Don't warn about double to float conversion
#pragma warning( disable : 4305 4244 )

typedef __int64	VRLONGLONG;
typedef int	pid_t;

#define STRDUP	_strdup

#define DCACHE 0
#define CACHEFLUSH(buf, size, which)

#define	CREATEWINDOWSTHREADTARGET(cls, x, name) 	\
    UINT x(LPVOID arg) {		    		\
        REGISTER_OGLCONTEXT(NULL);	    		\
        DECLARE_TIMING_THREAD(name);			\
	return (UINT)((cls*)arg)->x();			\
    }

#define STATSTRUCT  struct _stat
#define	STAT	    _stat

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

#else
#ifdef SGI

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

typedef long long int 	VRLONGLONG;

#define UINT	unsigned int

#ifndef _BOOL
// bool not predefined
typedef unsigned char bool;
static const bool false = 0;
static const bool true = 1;
#endif // _BOOL

#define DCACHE 0
#define CACHEFLUSH(buf, size, which) {				\
    int stat = cacheflush (buf, size, DCACHE); 			\
    CHECK((stat == 0), "cacheflush () failed: " << errno);	\
}

#define CREATEWINDOWSTHREADTARGET(cls, x, name)	\
    void *x(void *arg) {			\
	DECLARE_TIMING_THREAD(name);		\
	return ((cls*)arg)->x();		\
    }

#define STRDUP		strdup
#define STATSTRUCT	struct stat
#define STAT		stat

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

#else
#ifdef	LINUX

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

#include <string.h>

typedef long long int 	VRLONGLONG;

#define UINT	unsigned int

#define false 0
#define true 1

#define DCACHE 0
#define CACHEFLUSH(buf, size, which)

#define CREATEWINDOWSTHREADTARGET(cls, x, name)	\
    void *x(void *arg) {			\
	DECLARE_TIMING_THREAD(name);		\
	return ((cls*)arg)->x();		\
    }

#define STRDUP		strdup
#define STATSTRUCT	struct stat
#define STAT		stat

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

#endif	// LINUX

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

#endif	// SGI

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

#endif	// WIN32

#endif  // _SYMBOLS_SYMBOLS_H

