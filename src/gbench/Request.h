#include <iostream>
#include <string.h>
#include <cstring>
#include <boost/regex.hpp>
#include <boost/asio.hpp>

#ifndef MESSAGES_H
#define MESSAGES_H
#include "../common/Messages.h"
#endif

using namespace std;

/**
 * Abstract base Request class to contain common fields and methods 
 */
class Request{

public:
	enum request_t {INSERT, UPDATE, LOOKUP};
		
	static Request* parse(string s);
	request_t get_type(){return type;}
	unsigned long get_timestamp(){return timestamp;}
	bool is_acked(){return acked;}
	void set_id(unsigned long i){id = i;}
	unsigned long get_id(){return id;}
	virtual const char* get_payload_buffer(size_t& len) = 0;
	virtual const char* to_string() = 0;

protected:
	unsigned long timestamp;
	bool acked;
	request_t type;
	unsigned long id;
	bool valid_payload;
	char* payload; //valid if allocated and initialized
	string to_str;
	void init(unsigned long ts, request_t t){
		timestamp = ts;
		type = t;
		acked = false;
		valid_payload = false;
	}
};

class Insert_Request: public Request{

private:
	char src_guid[SIZE_OF_GUID];
	char src_net_addr[SIZE_OF_NET_ADDR];

public:
	Insert_Request(unsigned long ts){init(ts, INSERT);}
	const char* get_payload_buffer(size_t& len);
	static Insert_Request* parse(string s);
	const char* to_string(){
		char tmp[SIZE_OF_GUID + SIZE_OF_NET_ADDR + 200];
		sprintf(tmp, 
		"INSERT ts: %lu id: %lu GUID: %s NET_ADDR: %s acked: %s",
		timestamp, id, src_guid, src_net_addr, ((acked)?"yes":"no")); 
		to_str.assign(tmp);
		return to_str.c_str();
	}
};

class Update_Request: public Request{

private:
	char src_guid[SIZE_OF_GUID];
	char src_net_addr[SIZE_OF_NET_ADDR];

public:
	Update_Request(unsigned long ts){init(ts, UPDATE);}
	const char* get_payload_buffer(size_t& len);
	static Update_Request* parse(string s);
	const char* to_string(){
		char tmp[SIZE_OF_GUID + SIZE_OF_NET_ADDR + 200];
		sprintf(tmp, 
		"INSERT ts: %lu id: %lu GUID: %s NET_ADDR: %s acked: %s",
		timestamp, id, src_guid, src_net_addr, ((acked)?"yes":"no"));
		to_str.assign(tmp);
		return to_str.c_str();
	}
};

class Lookup_Request: public Request{

private:
	char src_guid[SIZE_OF_GUID];
	char src_net_addr[SIZE_OF_NET_ADDR];
	char dst_guid[SIZE_OF_GUID];
	char dst_net_addr[SIZE_OF_NET_ADDR];

public:
	Lookup_Request(unsigned long ts){init(ts, LOOKUP);}
	const char* get_payload_buffer(size_t& len);
	static Lookup_Request* parse(string s);
	const char* to_string(){
		char tmp[(SIZE_OF_GUID + SIZE_OF_NET_ADDR)*2 + 200];
		sprintf(tmp, 
		"LOOKUP ts: %lu id: %lu sGUID: %s sNET_ADDR: %s dGUID: %s dNET_ADDR: %s acked: %s", timestamp, id, src_guid, src_net_addr, dst_guid, 
		dst_net_addr, ((acked)?"yes":"no"));
		to_str.assign(tmp);
		return to_str.c_str();
	}
};
