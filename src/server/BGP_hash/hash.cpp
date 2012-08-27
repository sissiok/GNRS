#include "hash.h"
 

/*Hashing Function 
  Input: GUID and index of the hash function tobe used
*/
u32b Hash::hashG2IP(u64b GUID, u8b hashIndex){
	CSHA1 sha1;	
	if (sha1.curr_GUID == GUID)  //precomputed hash
		return sha1.hash_result[hashIndex]; 
	//convert GUID to binary bit string
	char bitstringGUID[64]; 
	u64b tmp = GUID; 
	for (int i=63;i>=0;i--){
		bitstringGUID[i] = tmp & 1;
		tmp = tmp >> 1; 
	}
	sha1.reInit(GUID); 
	sha1.Update(reinterpret_cast<unsigned char*>(bitstringGUID), 64 * sizeof(char));
	sha1.Final(); //Finish updating string tobe hashed
	sha1.Havest(); //put the result into hash_result vector
	return sha1.hash_result[hashIndex];
}

u32b Hash::hashIP2IP(u32b IP){
	CSHA1 sha1; 
	//convert GUID to binary bit string
	char bitstringIP[64];
	u64b tmp = (u64b)IP; 
	for (int i=63;i>=0;i--){
		bitstringIP[i] = tmp & 1;
		tmp = tmp >> 1; 
	}
	sha1.reInit((u64b)IP); 
	sha1.Update(reinterpret_cast<unsigned char*>(bitstringIP), 64 * sizeof(char));
	sha1.Final(); //Finish updating string tobe hashed
	sha1.Havest(); //put the result into hash_result vector
	return sha1.hash_result[0]; 
}

//int main(){
//	cout << Hash::hashG2IP(12,0) <<endl;
//	cout << Hash::hashG2IP(12,1) <<endl;
//}