#ifndef HASHMAP_H
#define HASHMAP_H

#define DB_STL

#include <string.h>
#include <vector>

/*
* This is internally used by HashMap class to initialize Map 
*/
struct value{
vector<string*>* locator;
vector<unsigned int*>* expire;
vector<unsigned short*>* weight;
};

#ifdef DB_STL
#include "dbstl_map.h"
using namespace dbstl;
typedef dbstl::db_map<string,value> GUID_LOCATOR_MAP;
#else
#include <map>
typedef map<string,value> GUID_LOCATOR_MAP;
#endif
/*
*   Class HashMap : Wrapper for actual implementation of HashMap
*   Supports get; put; remove operations of HashMap
*   Data Structure - HashMap with
*   key : string <<GUID>>
*   value : vector<string> << Multiple network identities>> 
*   Usage : Each BGP has one such HashMap 
*/


class HashMap{
//DbEnv* env;
//Db* pdb;
GUID_LOCATOR_MAP T;
GUID_LOCATOR_MAP::iterator it;

public:
  HashMap();
 void put(string , vector<string*>*, vector<unsigned int*>*,vector<unsigned short*>*);
 void erase(string);
 value get_value(string);
 vector<string*>* get_locator(string);
 vector<unsigned int*>* get_ttl(string);
  vector<unsigned short*>* get_weight(string);
 void print();
};

#endif
