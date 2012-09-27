#ifndef COMMON_H
#define COMMON_H

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <vector>
#include <map>
#include <queue>
#include <string>
#include <cstring>
#include <sstream>
#include <math.h>
#include <fstream>
#include <stdint.h>

using namespace std; 

#ifndef MAX_EDGE_WEIGHT
#define MAX_EDGE_WEIGHT 4294967295U 
#endif 
#ifndef K_NUM
#define K_NUM 0	//Number of replicated (number of hashing function)
#endif 
#ifndef MAX_TRY_NUM
#define MAX_TRY_NUM 10   //maximum number of rehashing 
#endif
#ifndef DEBUG
#define DEBUG 0 
#endif
#ifndef SAMPLING
#define SAMPLING 1
#endif

//#ifndef ROUTING_TABLE 
//#define ROUTING_FROM_FILE 0 
//#endif
#ifndef PRINT_OUTPUT 
#define PRINT_OUTPUT 0
#endif

using namespace std;



//typedef unsigned __int8 u8b;
typedef unsigned char u8b;
typedef unsigned short u16b;  //0 to 65,535
typedef unsigned int u32b; //0 to 4,294,967,295
typedef unsigned long u64b; //0 to 18,446,744,073,709,551,615

typedef u32b asIdx;   //AS index
typedef u32b asNum;  //AS number



//Input file prefix 
typedef struct prefix_entry_t{
	u32b prefix;    
	u8b  mask_bit;
	asNum orgin_AS; 
} Prefix_entry; 

//CIDR prefix 
typedef struct cidr_t{
	u32b prefix;    
	u8b  mask_bit;
} Cidr; 

//Location Struct
typedef struct location_t {
	double longitude;
	double latitude; 
} Location_t;    //AS's speaker location


//Entry for lookup table Struct
typedef struct guid_entry_t{
	u64b updated_time; 
	u64b GUID; 
	//u32b sourceIP; 
} Gentry_t; // <GUID, timestamp>

//prefixt with list of GUID
typedef struct as_prefix_entry_t{
	Cidr cdir_prefix; 
	vector<Gentry_t> GUID_list;  
} AS_Prefix_entry_t; 



class Common
{
public:
	static uint32_t port_str2num(char *listen_port);
	//------------------------------Prefix to int conveter ------------------------
	static u32b stringPrefix2Int(string inPrefix);
	//------------------------------stringToIntArray Converter --------------------
	static int stringToIntArr(const string& s, char delim, vector<int>& storeResult);
	//------------------------------String to array of string ---------------------
	static int str2StrArr(const string& s, char delim, vector<string>& storeResult);
	//------------------------------Check if an IP is belong to a prefix---------------------
	static bool isPartOfPrefix(Cidr prefix, u32b ip);
	//------------------------------IP to integer converter---------------------
	static u32b ip2num(vector<u8b> _ipAdd);
	//------------------------------integer to IP converter---------------------
	static vector<u8b> num2ip(u32b _ip);
	//------------------------------Binary search - Generic ---------------------
	static int binary_search(vector<Gentry_t>& vec, unsigned start, unsigned end, Gentry_t& key);
	
	static u32b myMASK[32];
	
	//compute distance from an IP to a prefix
	static u32b ipDistanceIPtoPrefix(Prefix_entry prefix, u32b ip);

};

#endif //COMMON_H
