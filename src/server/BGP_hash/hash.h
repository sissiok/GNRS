#ifndef HASH_H
#define HASH_H

#include "common.h"
#include "SHA1.h"

class Hash{
CSHA1 sha1;	
public:
	//------------------------------hashG2IP function---------------------
	static u32b hashG2IP(u64b GUID, u8b hashIndex);
	//------------------------------hashIP to annother IP function---------------------
	static u32b hashIP2IP(u32b IP);
private: 
	//------------------------------u64b to bit string converter---------------------
	char *converter64b2binStr(u64b num);

};

#endif //HASH_H