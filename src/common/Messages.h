#ifndef MESSAGES_H
#define MESSAGES_H
#include <stdint.h>
#include <string.h>

#define SIZE_OF_GUID 20
#define SIZE_OF_NET_ADDR 30
#define MAX_RESPONSE_LENGTH 1024
#define INSERT_MAX_NA 4
#define LOOKUP_MAX_NA 10



struct NA{
		char net_addr[SIZE_OF_NET_ADDR];
		uint32_t ttlMsec; // Expiration TTL in milliseconds
		uint16_t weight;
};

void* nacpy(NA& d_NA, NA& s_NA);
void* hton_nacpy(NA& d_NA, NA& s_NA);
void* ntoh_nacpy(NA& d_NA, NA& s_NA);

/**
 * Holds definitions of messages involved in the GNRS protocol
 */

	enum message_type_t {
		INSERT, 
		LOOKUP, 
		INSERT_ACK,
		LOOKUP_RESP,
	};

/*****
//this is the old message type
	enum message_type_t {
		INSERT, 
		UPDATE, 
		LOOKUP, 
		INSERT_ACK, 
		UPDATE_ACK, 
		LOOKUP_RESP,
	};
*/

	enum response_code_t {
		SUCCESS,
		ERROR,
	};

	
	/* common fields for each message type */
	struct common_header{
		uint32_t req_id; 	//unique request id
		uint8_t type; 	//message type
		char sender_addr[SIZE_OF_NET_ADDR];
		uint32_t sender_listen_port;
	};
	typedef struct common_header common_header_t;

	struct insert_message{
		common_header_t c_hdr;		//common header
		char guid[SIZE_OF_GUID]; 	//GUID of network entity 
		uint8_t dest_flag;  //dest_flag=1: the destination for the GUID entry is computed out; 0: the destination address hasn't been computed
		uint16_t na_num;  //the number of NA in the message
		NA NAs[];
	};
	typedef struct insert_message insert_message_t;


/*******************************************************
//update function is realized by insert.

	struct update_message{
		common_header_t c_hdr;
		char guid[SIZE_OF_GUID];
		NA NAs[INSERT_MAX_NA];
	};
	typedef struct update_message update_message_t;
********************************************************/

	struct lookup_message{
		common_header_t c_hdr;
		char guid[SIZE_OF_GUID]; 	//GUID of target being looked up
		uint8_t dest_flag;  //dest_flag=1: the destination for the GUID entry is computed out; 0: the destination address hasn't been computed
	};
	typedef struct lookup_message lookup_message_t;


	struct ack_message{
		common_header_t c_hdr;
		uint8_t resp_code;	//response code 
	};
	
	typedef struct ack_message insert_ack_message_t;
	//typedef struct ack_message update_ack_message_t;

	struct lookup_response_message{
		common_header_t c_hdr;
		uint8_t resp_code; 	//response code 
		uint16_t na_num;     //the number of NA in the message
		NA NAs[];
	};
	typedef struct lookup_response_message lookup_response_message_t;

#endif //GNRSMESSAGE_H
