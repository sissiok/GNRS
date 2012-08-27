#include "NAS.h"

NameAssignServ::NameAssignServ() {
	N2Gmap["smiling face"]="xy12wfx345fda4289fd";   //hardcoded here
	N2Gmap["smiling_face"]="xy12wfx345fda4289fd";   //hardcoded here
}


string NameAssignServ::get_guid(string name)  {
	return(N2Gmap[name]);
}

void NameAssignServ::insert_mapping(string name, string guid)  {
	N2Gmap[name]=guid;	
}
