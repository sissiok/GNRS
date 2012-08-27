#include <iostream>
#include <string.h>
#include <exception>
using namespace std;

#include "HashMap.h"
#include "../common/common.h"


HashMap::HashMap()
{
/*	env=new DbEnv(DB_CXX_NO_EXCEPTIONS);
	
	int ret=env->open(NULL,DB_CREATE | DB_INIT_MPOOL | DB_THREAD | DB_PRIVATE, 0);
	if (ret != 0) {
    		cout<<"db environment open fails!"<<endl;
		exit(0);
	} 

	pdb=new Db(env, DB_CXX_NO_EXCEPTIONS);
	//pdb->open(NULL,NULL,NULL,DB_BTREE,DB_CREATE | DB_THREAD, 0);
	pdb->open(NULL,NULL,NULL,DB_HASH,DB_CREATE | DB_THREAD, 0);
	
	T=new GUID_LOCATOR_MAP(pdb,env); 
*/
}

/*
* put : To put a <<key: value>> combination onto HashMap
*/

void HashMap::put(string guid, vector<string*>* locator, vector<unsigned int*>* expires, vector<unsigned short*>* weight)
{
	value v;
	/* if key already present 
	if(T.count(guid)>0){
	v=T.find(guid)->second;

	//append to existing vector of identities 
	v.id.push_back(id);
	T[guid]=v;
	}

	else{ 
	// new vector of identities
	v.id.push_back(id);
	T[guid] =v;
	}*/
	v.locator=locator;
	v.expire=expires;
	v.weight=weight;
	//(*T)[guid] =v;
	//T->insert(make_pair(guid,v));
	//T.insert(make_pair(guid,v));
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
	/*if(T.size(false)==0)
	{
		//error if HashMap empty 
		cout<<"HashMap empty"<<endl;
		throw exception();
	}

	else  */
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
	/*if(T.size(false)==0){
	//error if HashMap empty 
	cout<<"HashMap empty"<<endl;
	throw exception();
	}   

	else   */
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
	/*if(T.size(false)==0){
	//error if HashMap empty 
	cout<<"HashMap empty"<<endl;
	throw exception();
	}

	else   */
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



 


