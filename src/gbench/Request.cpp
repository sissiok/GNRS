#include <iostream>
#include <string.h>
#include <cstring>
#include <boost/regex.hpp>
#include <boost/asio.hpp>

#ifndef MESSAGES_H
#define MESSAGES_H
#include "../common/Messages.h"
#endif

#ifndef REQUEST_H
#define REQUEST_H
#include "Request.h"
#endif

using namespace std;

/**
 * Parses type of request and retrieves request instance by
 * invoking parse method of corresponding request class
 */
Request* Request::parse(string s){

	/* looking for 'timestamp <request-type> <parameters>' */
	boost::regex exp(
		"^\\w*[0-9]+\\t(INSERT|I|UPDATE|U|LOOKUP|L|QUERY|Q)\\t.*$");
	boost::cmatch what;
	//cout << "DEBUG: " << "Parsing line: " << s << endl;
	if(boost::regex_match(s.c_str(), what, exp)){
		string op(what[1].first, what[1].length());
		//cout << "DEBUG: " << "Parse op: '" << op << "'" << endl;
		if(!op.compare("INSERT") || !op.compare("I")){
			return Insert_Request::parse(s);
		}else if(!op.compare("UPDATE") || !op.compare("U")){
			return Update_Request::parse(s);
		}else if(!op.compare("LOOKUP") || !op.compare("L") 
				|| !op.compare("QUERY") || !op.compare("Q")){
			return Lookup_Request::parse(s);
		}else{
			//shouldn't come here, dummy throw
			throw exception();
		}
	}else{
		//unknown request type
		throw exception();
	}
}

Insert_Request* Insert_Request::parse(string s){
	/* looking for:
	 * <ts> INSERT <sGUID> <sNA-list>
	 */
	boost::regex exp(
		"^\\h*([0-9]+)\\t(INSERT|I)\\t([0-9a-fA-F]+)\\t([^\t^#^\r]+).*$");
	boost::cmatch what;
	unsigned long ts;
	if(boost::regex_match(s.c_str(), what, exp)){
		string ts_str(what[1].first, what[1].length());
		ts = atol(ts_str.c_str());
		Insert_Request* req = new Insert_Request(ts);		
		//set guid
		if(what[3].length() > SIZE_OF_GUID){
			throw exception();
		}
		memcpy(req->src_guid, what[3].first, what[3].length());
		req->src_guid[what[3].length()] = '\0';
		//set net addr
		if(what[4].length() > SIZE_OF_NET_ADDR){
			throw exception();
		}
		memcpy(req->src_net_addr, what[4].first, what[4].length());
		req->src_net_addr[what[4].length()] = '\0';
		return req;
	}else{
		//parse error
		throw exception();
	}
}

Update_Request* Update_Request::parse(string s){
	/* looking for:
	 * <ts> UPDATE <sGUID> <sNA-list>
	 */
	boost::regex exp(
		"^\\h*([0-9]+)\\t(UPDATE|U)\\t([0-9a-fA-F]+)\\t([^\t^#^\r]+).*$");
	boost::cmatch what;
	unsigned long ts;
	if(boost::regex_match(s.c_str(), what, exp)){
		string ts_str(what[1].first, what[1].length());
		ts = atol(ts_str.c_str());
		Update_Request* req = new Update_Request(ts);
		//set guid
		if(what[3].length() > SIZE_OF_GUID){
			throw exception();
		}
		memcpy(req->src_guid, what[3].first, what[3].length());
		req->src_guid[what[3].length()] = '\0';
		//set net addr
		if(what[4].length() > SIZE_OF_NET_ADDR){
			throw exception();
		}
		memcpy(req->src_net_addr, what[4].first, what[4].length());
		req->src_net_addr[what[4].length()] = '\0';
		return req;
	}else{
		//parse error
		throw exception();
	}
}

Lookup_Request* Lookup_Request::parse(string s){
	/* looking for:
	 * <ts> LOOKUP <sGUID> <sNA-list> <dGUID> <dNA-list>
	 */
	boost::regex exp("^\\h*([0-9]+)\\t(LOOKUP|L|QUERY|Q)\\t([0-9a-fA-F]+)\\t([^\t^#]*)\\t([0-9a-fA-F]+)\\t([^\t^#^\r]*).*$");
	boost::cmatch what;
	unsigned long ts;
	if(boost::regex_match(s.c_str(), what, exp)){
		string ts_str(what[1].first, what[1].length());
		ts = atol(ts_str.c_str());
		Lookup_Request* req = new Lookup_Request(ts);
		//set src guid
		if(what[3].length() > SIZE_OF_GUID){
			throw exception();
		}
		memcpy(req->src_guid, what[3].first, what[3].length());
		req->src_guid[what[3].length()] = '\0';
		//set net addr
		if(what[4].length() > SIZE_OF_NET_ADDR){
			throw exception();
		}
		memcpy(req->src_net_addr, what[4].first, what[4].length());
		req->src_net_addr[what[4].length()] = '\0';
		//set dst guid
		if(what[5].length() > SIZE_OF_GUID){
			throw exception();
		}
		memcpy(req->dst_guid, what[5].first, what[5].length());
		req->dst_guid[what[5].length()] = '\0';
		//set net addr
		if(what[6].length() > SIZE_OF_NET_ADDR){
			throw exception();
		}
		memcpy(req->dst_net_addr, what[6].first, what[6].length());
		req->dst_net_addr[what[6].length()] = '\0';
		return req;
	}else{
		//parse error
		throw exception();
	}
}

const char* Insert_Request::get_payload_buffer(size_t& len){
	
	if(!valid_payload){
		payload = new char[sizeof(insert_message_t)];
		/* populate the payload */
		insert_message_t* i_msg = 
			(insert_message_t*)payload;
		i_msg->c_hdr.type = INSERT;
		i_msg->c_hdr.req_id = id;
		strcpy(i_msg->guid, src_guid);
		strcpy(i_msg->net_addr, src_net_addr);

		valid_payload = true;
	}
	len = sizeof(insert_message_t);
	return (const char*)payload;
}

const char* Update_Request::get_payload_buffer(size_t& len){
	
	if(!valid_payload){
		payload = new char[sizeof(update_message_t)];
		/* populate the payload */
		update_message_t* u_msg = 
			(update_message_t*)payload;
		u_msg->c_hdr.type = UPDATE;
		u_msg->c_hdr.req_id = id;
		strcpy(u_msg->guid, src_guid);
		strcpy(u_msg->net_addr, src_net_addr);

		valid_payload = true;
	}
	len = sizeof(update_message_t);
	return (const char*)payload;
}

const char* Lookup_Request::get_payload_buffer(size_t& len){
	
	if(!valid_payload){
		payload = new char[sizeof(lookup_message_t)];
		/* populate the payload */
		lookup_message_t* l_msg = 
			(lookup_message_t*)payload;
		l_msg->c_hdr.type = LOOKUP;
		l_msg->c_hdr.req_id = id;
		/* looking up the destination GUID */
		strcpy(l_msg->guid, dst_guid);

		valid_payload = true;
	}
	len = sizeof(lookup_message_t);
	return (const char*)payload;
}
