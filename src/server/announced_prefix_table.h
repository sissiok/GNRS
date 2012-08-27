#ifndef ANNOUNCED_PREFIX_TABLE_H
#define ANNOUNCED_PREFIX_TABLE_H

#include "../common/common.h"

class PrefixTable{

public:
	//Constructor 
	//getting prefixes from file
	PrefixTable(); 
	void init(const char *prefixInputFile, u64b _time);
	
	//**********************Member**********************
	vector<Prefix_entry> entryList; 
	u64b lastUpdate; 
	u32b tabSize; 

	//**********************Operation**********************
	
	//Read prefix input file
	void  setPrefixFromFile(const char *filename);


private:	
	Prefix_entry prefixEntryParser(const char *prefixEntry); 
};

#endif //ANOUNCE_PREFIX_TABLE_H