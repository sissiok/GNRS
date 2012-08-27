/**
 *name assignment service
 *hardcoded as a directory first, need to make it as a network service later
*/

#ifndef NAS_H
#define NAS_H

#include <string>
#include <map>
using namespace std;

typedef map<string,string> name_guid_map;

class NameAssignServ {
public:
  NameAssignServ();
  string get_guid(string); 
  void insert_mapping(string,string);

private:
  name_guid_map N2Gmap;

};

#endif
