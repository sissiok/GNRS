/**
 * Copyright (c) 2013, Rutgers, The State University of New Jersey
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization(s) stated above nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#ifndef MESSAGES_H
#define MESSAGES_H

#include <list>
#include "common.h"
#include "guid.h"

#define INSERT_REQUEST (unsigned char)0
#define LOOKUP_REQUEST (unsigned char)1
#define UPDATE_REQUEST (unsigned char)2

#define INSERT_RESPONSE (unsigned char)0x80
#define LOOKUP_RESPONSE (unsigned char)0x81
#define UPDATE_RESPONSE (unsigned char)0x82

#define PROTOCOL_VERSION 0 //for debug

//options
#define OPTION_REQUEST_REDIRECT (uint8_t)0
#define OPTION_NA_EXPIRATION (uint8_t)1
#define OPTION_NA_TTL (uint8_t)2

#define FINAL_OPTION_FLAG (uint8_t)0x80

//response status
#define RESPONSE_INCOMPLETE 0
#define RESPONSE_COMPLETE 1

//response containers for easy unpacking
#define MAX_RESPONSE_OPTS 64
#define MAX_LOOKUP_RESPONSE_ADDRS 64

/**
 * Note: The following data structures are for use on host only.
 * For on the wire transmission, these need to be serialized with correct byte
 * ordering. Refer to the wire protocol documentation for correct field
 * ordering in packet
 * https://bitbucket.org/romoore/gnrs/wiki/Home
 */

typedef struct{
    uint8_t version;
    char type;
    uint16_t len;
    uint32_t id;
    addr_tlv_t src_addr;
    uint16_t data_len;
    void* data; /* request-type specific payload */
    uint16_t num_opts;
    opt_tlv_t* opts;
}req_t;

typedef struct{
    uint8_t guid[GUID_BINARY_SIZE];    /* binary encoded guid */ 
}lookup_t;

typedef struct{
    uint8_t guid[GUID_BINARY_SIZE];     
    uint32_t size;
    addr_tlv_t* addrs; /* points to vector of addresses */ 
}upsert_t;


typedef struct{
    uint32_t size;
    addr_tlv_t addrs[MAX_LOOKUP_RESPONSE_ADDRS]; /* points to vector of addresses */
}lookup_resp_t; 

/*
typedef struct{
}upsert_resp_t;
*/

typedef struct{
    uint8_t status;// RESPONSE_COMPLETE, RESPONSE_INCOMPLETE
    uint8_t version;
    unsigned char type;
    uint16_t len;
    uint32_t req_id;
    addr_tlv_t src_addr;
    uint16_t code; 
    uint16_t data_len;
    lookup_resp_t lkup_data; /* request-type specific payload */
    //upsert_resp_t ups_data; /* request-type specific payload */
    uint16_t num_opts;
    opt_tlv_t opts[MAX_RESPONSE_OPTS];
}resp_t;

#endif //MESSAGES_H
