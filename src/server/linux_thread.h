#include <pthread.h>

class PosixSpecific {
public:
	/************ MUTEX RELATED ****************/
	typedef pthread_mutex_t MutexT;
	
	static void CreateMutex(MutexT* pMutex);
	static void LockMutex(MutexT& mutex);
	static void UnlockMutex(MutexT& mutex);
	static void DestroyMutex(MutexT& mutex);
	
	/************* THREAD RELATED ****************/
	typedef void* (*ThreadFunctionT)(void*);
	typedef pthread_t ThreadIdentifierT;

	static void BeginThread( ThreadFunctionT pFunction, void* arg, ThreadIdentifierT* pThreadId );

	/************* SEMAPHORE / CONDITION RELATED ****************/
	typedef struct PosixSpecificCondition {
		pthread_mutex_t mutex;
		pthread_cond_t condition;
		bool condition_set;
	} ConditionT;

	static void CreateCondition(ConditionT* pCondition);
	static void WaitForCondition(ConditionT& condition);
	static void SignalCondition(ConditionT& condition);
	static void DestroyCondition(ConditionT& condition);
};

typedef PosixSpecific OSSpecific;
