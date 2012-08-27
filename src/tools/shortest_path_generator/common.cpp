#include "common.h"
using namespace std;
 
/*
Utility function 
Convert a string of integers to an array 
Return 0 if at least 1 element is found, and 1 if error 
*/


u32b Common::myMASK[] = {2147483648U,3221225472U,3758096384U,4026531840U,
4160749568U,4227858432U,4261412864U,4278190080U,
4286578688U,4290772992U,4292870144U,4293918720U,
4294443008U,4294705152U,4294836224U,4294901760U,
4294934528U,4294950912U,4294959104U,4294963200U,
4294965248U,4294966272U,4294966784U,4294967040U,
4294967168U,4294967232U,4294967264U,4294967280U,
4294967288U,4294967292U,4294967294U,4294967295U}; 

int Common::stringToIntArr(const string& s, char delim, vector<int>& storeResult){
	stringstream ss(s); //convert to stream
	string item; 

	while(getline(ss,item,delim)){ //extract from stream until it's empty
			int value = atoi(item.c_str()); //string to integer
			u32b itemValue = (u32b)value; 
			storeResult.push_back(itemValue);
	}
	if (storeResult.size() == 0 )
	{
		if (DEBUG == 1)
			cerr <<"Error on reading topology message" <<endl;
		return 1; 
	}
	return 0; 
}

/*
Utility function convert a string to array of string 
Return 0 if at least 1 element is found, and 1 if error 
*/
int Common::str2StrArr(const string& s, char delim, vector<string>& storeResult){
	storeResult.clear(); 
	stringstream _ss(s);
	string _element; 
	while(getline(_ss,_element,delim)){
			storeResult.push_back(_element);
	}	
	if (storeResult.size() == 0 )
	{
		if (DEBUG == 1)
			cerr <<"Common::str2StrArr: Error on converted string - Size =0 " <<endl;
		return 1; 
	}
	return 0; 

}

/*
	Check if an IP is belong to a prefix 
*/
bool Common::isPartOfPrefix(Cidr prefix, u32b ip){

	u32b leftAfterMasked = prefix.prefix & myMASK[prefix.mask_bit -1];
	u32b rightAfterMasked = ip & myMASK[prefix.mask_bit - 1];
	
	return (leftAfterMasked == rightAfterMasked);
}


/*
	IP to number converter 
	Input: Vector of 4 elements of an IP 
*/
u32b Common::ip2num(vector<u8b> _ipAdd){

	u32b _result=0; 
	vector<u8b>::iterator _vItt; 
	if (_ipAdd.size() !=4){
		cerr << " ip2num:: IP address in wrong format !! - Size: " <<_ipAdd.size() <<endl; 
		return 0; 
	}
	for (int i=0;i<=3;i++){
		_result += (u32b)((_ipAdd.back())* pow(256*1.0,i)); 
		_ipAdd.pop_back(); 
	}

	return _result; 
}

/* Binary search for GUID*/
int Common::binary_search(vector<Gentry_t>& vec, unsigned start, unsigned end, Gentry_t& key){
	//cout << "in Binary Search Start,end : <" <<start << "," << end << "> " <<endl;	
    // Termination condition: start index greater than end index
    if(start > end) return -1;   
    unsigned middle = (start + ((end - start) / 2));
	//cout << "middle: " <<middle<<endl;
	if(vec[middle].GUID == key.GUID)  
		return middle;
    else if ((vec[middle].GUID > key.GUID) ){
		if  (middle >0) return binary_search(vec, start, middle - 1, key);
		else return -1; 
    }
	if (middle <end ) 
		return binary_search(vec, middle + 1, end, key);
	else return -1; 
}

//compute distance from an IP to a prefix
u32b Common::ipDistanceIPtoPrefix(Prefix_entry prefix, u32b ip){
	u32b prefVal = prefix.prefix;
	//get the begining of the ip trunk - erase  the last mask_bit bits
	prefVal  = prefVal >> (32-  prefix.mask_bit) ;
	u32b beginVal = prefVal << (32 - prefix.mask_bit) ; 
	//get ending of the trunk 
	u32b tmp = 1;
	tmp = tmp <<  (32 - prefix.mask_bit);
	u32b endVal = beginVal + tmp -1; 
	//return 
	if (endVal < ip)
	 return (ip-endVal);
	else if (beginVal >ip)
	 return (beginVal - ip);
	else
	 return 0; 
}
