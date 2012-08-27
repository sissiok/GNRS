/*
	Getting the list of prefix entries and store it on entryList
*/ 
#include "announced_prefix_table.h"

//Pleasing the compiler 
PrefixTable::PrefixTable(){

}
void PrefixTable::init(const char *prefixInputFile, u64b _time){
	setPrefixFromFile(prefixInputFile);  
	lastUpdate = _time; 
}

/*
Getting the prefix from file
*/
void PrefixTable::setPrefixFromFile(const char *_fileName){
	ifstream _prefixFHdlr(_fileName);
	string tempt_line;
	if (_prefixFHdlr.is_open()){
		while (_prefixFHdlr.good()){
			getline(_prefixFHdlr,tempt_line);
			if (DEBUG >=2) cout <<tempt_line <<endl; 
			//parse the line here
			if (tempt_line.size() > 10)
			entryList.push_back(prefixEntryParser(tempt_line.c_str())); 
		}
	}
	else{
		cerr << "Can't open PREFIX File !!! Returning..." <<endl;
		return; 
	}
} //end setPrefixFromFile

/*
Parsing lin
*/
Prefix_entry PrefixTable::prefixEntryParser(const char *_prefixEntry){
	Prefix_entry _result;					//returning this 
	string _pref, _sASnum,_sTmp, _ip, _mask; //store prefix with mask
	asNum _asNumber;						 //store AS number 

	vector<u8b> _vBytes; 
	vector<string> _vStrTemp; 
	Common::str2StrArr(_prefixEntry,' ', _vStrTemp); 
	_pref = _vStrTemp[0]; 
	_asNumber = atoi(_vStrTemp[1].c_str()); 
	Common::str2StrArr(_pref,'/', _vStrTemp); 
	_ip = _vStrTemp[0]; 
	_mask=_vStrTemp[1]; 
	Common::str2StrArr(_pref,'.', _vStrTemp); 	
	if (_vStrTemp.size() == 4) {
		for (int i=1;i<=4;i++){
			_vBytes.push_back(atoi(_vStrTemp[i-1].c_str())); 
		}
	}
	else{
		cerr << "Error on PARSING prefix entry, less than 4 bytes was found, size:  " << _vBytes.size() << endl;
	}
	if (DEBUG >=2) {
		cout << "Prefix (string): " <<_pref << endl;
		cout << " AS Number in Int: " <<_asNumber <<endl;
		cout << " Mask (string) " <<_mask <<endl;
	}
	_result.orgin_AS = _asNumber; 
	_result.prefix = Common::ip2num(_vBytes); 
	_result.mask_bit =(u8b)atoi(_mask.c_str());

	if (DEBUG >=2) {
		cout <<"***********************************" <<endl;
		cout << "Prefix : " <<_result.prefix << endl;
		cout << " BitMask: " <<_result.mask_bit <<endl;
		cout << " AS num: " << _result.orgin_AS <<endl; 
	}
	
	return _result; 
}

//int main(){
//	const char* fname="prefix.data";
//	PrefixTable *mypref = new PrefixTable(fname, 2000);	
//	
//	return 0; 
//}
