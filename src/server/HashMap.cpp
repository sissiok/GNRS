#include <iostream>
#include <string.h>
#include <exception>
using namespace std;

#include "HashMap.h"
#include "../common/common.h"


HashMap::HashMap()
{
}

/*
* put : To put a <<key: value>> combination onto HashMap
*/

void HashMap::put(string guid, vector<string*>* locator, vector<unsigned int*>* expires, vector<unsigned short*>* weight)
{
	value v;
	v.locator=locator;
	v.expire=expires;
	v.weight=weight;
	T[guid]=v;   //TODO:this will cause memory leak if guid entry already exists
}


void HashMap::erase(string guid)
{
	T.erase(guid);

}


value HashMap::get_value(string guid)
{
	value v;
	GUID_LOCATOR_MAP::iterator it=T.find(guid);
	//if guid present : fetch the identities
	if(it!=T.end()){
	    v=it->second;
	    return v;
	}

	else{
		//No such guid present in HashMap
		if(DEBUG>=1) printf("No Key Found\n");
		throw exception();
	}
}


/*
* get: To get the locator values associated with particular key
*/

vector<string*>* HashMap::get_locator(string guid)
{
	value v;
	GUID_LOCATOR_MAP::iterator it=T.find(guid);
		//if guid present : fetch the identities
		if(it!=T.end()){
		v=it->second;
		return v.locator;
		}

		else{
			//No such guid present in HashMap
			if(DEBUG>=1) cout<<"No Key Found"<<endl;
			throw exception();
		}
	
}


/*
* get: To get the TTL values associated with particular key
*/

vector<unsigned int*>* HashMap::get_ttl(string guid)
{
	value v;
	GUID_LOCATOR_MAP::iterator it=T.find(guid);
	//if guid present : fetch the identities
	if(it!=T.end()){
	v=it->second;
	return v.expire;
	}

	else{
	//No such guid present in HashMap
	if(DEBUG>=1) cout<<"No Key Found"<<endl;
	throw exception();
	}
}



/*
* get: To get the TTL values associated with particular key
*/

vector<unsigned short*>* HashMap::get_weight(string guid)
{
	value v;
	GUID_LOCATOR_MAP::iterator it=T.find(guid);
	//if guid present : fetch the identities
	if(it!=T.end()){
	v=it->second;
	return v.weight;
	}

	else{
	//No such guid present in HashMap
	if(DEBUG>=1) cout<<"No Key Found"<<endl;
	throw exception();
	}
}





/*
* print() : Print the HashMap Key:: Value pairs
*/

void HashMap::print(){
cout << "came to print" << endl;
for ( it=T.begin() ; it != T.end(); it++ ){
	string key=(*it).first;
    cout << "GUID :"<<key<< endl;
     value temp=T.find(key)->second;
	cout << "VALUES ASSOCIATED WITh GUID "<<endl;
	for(int k=0;k < (int)temp.locator->size();k++){
      		cout << (*temp.locator)[k] <<endl;
	}

}
}

/*
* Performance Analysis :
* get() : Linear Time operation O(1) 
* put() : Linear Time operation O(1) 
*/


// Test the code written : 



 


