#ifndef HASH128_H
#define HASH128_H

#include "MD5.h"
#include <stdint.h>
#include <string.h>
#include "../common/common.h"

using namespace std;

extern vector<string> server_list;

class Hash128
{
	
       public: 
		string HashG2Server(char *GUID, uint8_t hashIndex);   //return with an server address
		uint32_t HashG2IP(char *GUID, uint8_t hashIndex);   //return with an IP address
		uint32_t HashIP2IP(uint32_t IP, uint8_t hashIndex);
		string MapAS2Server(asNum asNumber);   //mapping between the as# and the server IP
	private: 
		MD5 md5;
		int globalHash(char guid[],int count);
		uint32_t globalHash(char guid[]);
};


#endif
