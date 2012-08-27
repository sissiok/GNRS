//content message format is define here

#ifndef CONTENT_MESSAGE_H
#define CONTENT_MESSAGE_H

#define DEFAULT_PLD_SIZE 1000

struct init_request {
	uint32_t sender_receiving_port;
	char sender_addr[SIZE_OF_NET_ADDR];
	char guid[SIZE_OF_GUID];
};

struct content_pkt {
	uint8_t file_ending_flag;
	uint32_t pld_size;
	char pld[];
};


#endif
