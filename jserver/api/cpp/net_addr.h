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
#ifndef NET_ADDR_H
#define NET_ADDR_H

#include <sstream>
#include <iostream>
#include <string>

#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <stdio.h>

#include "common.h"

using namespace std;

//address types
#define NET_ADDR_TYPE_IPV4_PORT 0
#define NET_ADDR_TYPE_GUID 1
#define NET_ADDR_TYPE_NA 2

#define NET_ADDR_LEN_IPV4_PORT 6
#define NET_ADDR_LEN_GUID 20
#define NET_ADDR_LEN_NA 4

#define PORT_OFFSET_IPV4_PORT 4

#define MAX_NET_ADDR_ENCODED_LEN 20

class NetAddr {

    public:

        uint16_t type;

        /* encoded length */
        uint16_t len;
        /* 
         * encoding here is determined by address type 
         * refer to documentation for considered address
         * types and suggested encodings
         */
        string value;

        unsigned char bytes[MAX_NET_ADDR_ENCODED_LEN];

        /* absolute time in milliseconds since 1970 epoch after which
         * the address, when associated with a GUID, should be 
         * considered invalid. Ignored if 0 - no defined expiry
         */
        uint32_t expiry;

        /* relative time in milliseconds following a caching operation after
         * which the address, when associated with a GUID,  should 
         * be considered invalid. Ignored if 0 - no defined ttl
         */
        uint32_t ttl;


        //these should go into derived class
        string hostname_or_ip;
        int port;

        NetAddr(uint16_t addr_type, string addr_value, 
                uint32_t expiry_ms = 0, uint32_t ttl_ms = 0): 
                type(addr_type), value(addr_value), 
                expiry(expiry_ms), ttl(ttl_ms) {
                
            switch(type) {
                case NET_ADDR_TYPE_IPV4_PORT: len = NET_ADDR_LEN_IPV4_PORT; 
                                        parse_ip_and_port();
                                        break;
                case NET_ADDR_TYPE_GUID: len = NET_ADDR_LEN_GUID; 
                                        //TODO: parse/encode GUID
                                        break;
                case NET_ADDR_TYPE_NA: len = NET_ADDR_LEN_NA; 
                                        //TODO: parse/encode NA
                                        break;
            }
        }
     
        /* constructor for binary encoded char array for address value */
        NetAddr(uint16_t addr_type, unsigned char* buf, int len): 
                                        type(addr_type), len(len) { 
            switch(type) {
                case NET_ADDR_TYPE_IPV4_PORT: 
                    if(len != NET_ADDR_LEN_IPV4_PORT){
                        cerr << "ERROR: NetAddr: length mismatch for type: " 
                            << addr_type << "; expect " << NET_ADDR_LEN_IPV4_PORT 
                            << "got " << len << endl;	
                        //TODO throw exception
                        return;
                    }
                    break;
            
                case NET_ADDR_TYPE_GUID: 
                    //TODO: 
                    break;
                case NET_ADDR_TYPE_NA: 
                    //TODO: 
                    break;
            }
            memcpy(bytes, buf, len);
            //TODO parse IP address
            char tmp[32];
            sprintf(tmp, "%u.%u.%u.%u", *(uint8_t*)&bytes[0], 
                                        *(uint8_t*)&bytes[1], 
                                        *(uint8_t*)&bytes[2], 
                                        *(uint8_t*)&bytes[3]);
            hostname_or_ip.assign(tmp); 
            port = htons(*((uint16_t*)&bytes[PORT_OFFSET_IPV4_PORT]));
            sprintf(tmp, "%s:%u", hostname_or_ip.c_str(), port); 
            value.assign(tmp);
        }

        //IPV4/TCP/UDP specfic functions
        string get_hostname_or_ip() {
            return hostname_or_ip;
        }

        int get_port() {
            return port;
        }

        addr_tlv_t get_tlv() {
            addr_tlv_t t;
            t.type = type;
            t.len = len;
            t.value = bytes;

            return t;
        }

    private:
        void parse_ip_and_port(){
            //format: (hostname_or_IP:port)
            const char* s = value.c_str(); 
            const char* index = strchr(s, ':');
            hostname_or_ip = value.substr(0, index - s);
            struct hostent *he;
            if (!(he = gethostbyname(hostname_or_ip.c_str()))) {
                //TODO throw an exception
                cerr << "FATAL: gethostbyname failed for host: " 
                    << hostname_or_ip
                    << endl;
                exit(EXIT_FAILURE);
            }

            memcpy(bytes, he->h_addr_list[0], he->h_length);
            //port
            istringstream strm(value.substr(index - s + 1, value.length()));
            strm >> port;
	        *(uint16_t*)&bytes[he->h_length] = ntohs(port);
        }
};

#endif //NET_ADDR_H
