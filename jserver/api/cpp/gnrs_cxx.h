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
#ifndef GNRS_CXX_H
#define GNRS_CXX_H

#include <string>
#include <list>
#include <string.h>

#include "common.h"
#include "guid.h"
#include "net_addr.h"
#include "messages.h"
#include "udpipv4_endpoint.h"


class Gnrs{

    public:

        /**
         * Constructor that sets up service access and local addresses.
         *
         * 
         * Refer to wire protocol documentation to learn about address
         * types supported by the current implementation.
         */
        Gnrs(NetAddr server, NetAddr local): 
            server_addr(server), local_addr(local), request_id(1),
		ue (UdpIpv4Endpoint(server_addr, local_addr)) {}

        /**
         * Retreive address(es) currently mapped to given guid.
         *
         */

        list<NetAddr> lookup(Guid guid);

        /** 
         * Add guid to address(es) mapping. 
         *
         * If a mapping already exists at the time the request is processed 
         * (at the server), then the set of new addresses is added to the
         * mapping - not replaced.
         *
         */
        void add(Guid guid, list<NetAddr>& addrs);

        /**
         * Replace existing mapping (if any) with new set of addresses.
         *
         * The old mappings (set of addresses, and any related options)
         * are deleted from the directory. If no mapping exists at the time
         * request is serviced, then this is equivalent to the add(...)
         * operation.
         *
         */
        void replace(Guid guid, list<NetAddr>& addrs);

    private:
        NetAddr server_addr;
        NetAddr local_addr;
        uint32_t request_id;
        uint16_t req_len_offset;
        UdpIpv4Endpoint ue;

        int _send(unsigned char* buf, int len) {
            //UDP/IP default transport
            return ue._send(buf, len);
        }

        int _recv(unsigned char* buf, int len) {
            return ue._recv(buf, len);
        }

        //TODO: make thread safe
        uint32_t get_request_id() { return request_id++;}

        uint16_t build_request_msg(req_t req, unsigned char* buf) {

            uint16_t i = 0;
            //version
            *(buf + i) = req.version;
            i++;
            //type
            *(buf + i) = req.type;
            i++;
            //len fill after counting
            //*(uint16_t*)(buf + i) = htons(req.len);
	        req_len_offset = i;
            i += 2;
            //request id
            *(uint32_t*)(buf + i) = htonl(req.id);
            i += 4;
            //options offset
            *(uint16_t*)(buf + i) = htons(12 + 4 + req.src_addr.len 
                                                    + req.data_len);
            i += 2;
            //data offset
            *(uint16_t*)(buf + i) = htons(12 + 4 + req.src_addr.len);
            i += 2;

            //requestor address
            *(uint16_t*)(buf + i) = htons(req.src_addr.type);
            i += 2;
            *(uint16_t*)(buf + i) = htons(req.src_addr.len);
            i += 2;
            memcpy(buf + i, req.src_addr.value, req.src_addr.len);
            i += req.src_addr.len;


            //request payload
            if(req.type == LOOKUP_REQUEST){

                lookup_t* lkup = (lookup_t*)req.data;
                memcpy(buf + i, lkup->guid, GUID_BINARY_SIZE);
                i += GUID_BINARY_SIZE;

            }else if(req.type == INSERT_REQUEST ||
                        req.type == UPDATE_REQUEST) {

                upsert_t* ups = (upsert_t*)req.data;
                memcpy(buf + i, ups->guid, GUID_BINARY_SIZE);
                i += GUID_BINARY_SIZE;
                *(uint32_t*)(buf + i) = htonl(ups->size);
                i += 4;

                //address entries
                addr_tlv_t* addrs = ups->addrs;
                for(int j = 0; j < ups->size; j++) {
                    *(uint16_t*)(buf + i) = htons((addrs + j)->type);
                    i += 2;
                    *(uint16_t*)(buf + i) = htons((addrs + j)->len);
                    i += 2;
                    memcpy(buf + i, (addrs + j)->value, (addrs + j)->len);
                    i += (addrs+j)->len;
                }
            }

            //options
            opt_tlv_t* opts = req.opts;
            for (int j = 0; j < req.num_opts; j++) {
                if (j == (req.num_opts - 1)) {
                    /* last option, set 'final' bit 0x80 */
                            *(uint8_t*)(buf + i) = 
                        (opts + j)->type | FINAL_OPTION_FLAG;
                } else {
                            *(uint8_t*)(buf + i) = (opts + j)->type;
                }
                i++;
                *(uint8_t*)(buf + i) = (opts + j)->len;
                i++;
                memcpy(buf + i, (opts + j)->value, (opts + j)->len);
                i += (opts+j)->len;
            }
            //fill in len 
            *(uint16_t*)(buf + req_len_offset) = htons(i);
            return i;
        }

        void parse_response_msg(unsigned char* buf, int len, resp_t& rsp){

            rsp.status = RESPONSE_INCOMPLETE;
            int i = 0; // index
            //version
            if (len >= (i + 1)) {
                rsp.version = *(buf + i);
            } else {
                cerr << "ERROR: incomplete response: @ version" << endl;
                //TODO throw exception
                return;
            }
            i++;
            //message type
            if (len >= (i+1)) {
                rsp.type = *(buf + i);
            } else {
                cerr << "ERROR: incomplete response: @ message type" << endl;
                return;
            }
            i++;
            //message len
            if (len >= (i+2)) {
                rsp.len = ntohs(*(uint16_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response: @response len" << endl;
                return;
            }
            i += 2;
            //request id
            if (len >= (i+4)) {
                rsp.req_id = ntohl(*(uint32_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response: @ request id" << endl;
                return;
            }
            i += 4;
        /*

            //options offset
            uint16_t opts_offset;
            if (len >= (i+2)) {
                opts_offset = ntohs(*(uint16_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response" << endl;
                return;
            }
            i += 2;
            //data offset
            uint16_t data_offset;
            if (len >= (i+2)) {
                data_offset = ntohs(*(uint16_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response" << endl;
                return;
            }
            i += 2;
	*/
            //origin addr
            //T type
            if (len >= (i+2)) {
                rsp.src_addr.type = ntohs(*(uint16_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response:@ origin addr:T" << endl;
                return;
            }
            i += 2;
            //L len
            if (len >= (i+2)) {
                rsp.src_addr.len = ntohs(*(uint16_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response: @ origin addr:L" << endl;
                return;
            }
            i += 2;
            //V value
            if (len >= (i + rsp.src_addr.len)) {
                rsp.src_addr.value = buf + i;
            } else {
                cerr << "ERROR: incomplete response: @ origin addr:V" << endl;
                return;
            }
            i += rsp.src_addr.len;

            //response code
            if (len >= (i+4)) {
		    //16bit value and 2 byte pad - pick up only the first 2
                rsp.code = ntohs(*(uint16_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response: @ response code " << endl;
                return;
            }
            i += 4;

	        //response payload
            if (rsp.type == LOOKUP_RESPONSE) {
                if (len >= (i+4)) {
                    rsp.lkup_data.size = ntohl(*(uint32_t *)(buf + i));
                } else {
                    cerr << "ERROR: incomplete response" << endl;
                    return;
                }
                i += 4;
                for(int j = 0; (j < rsp.lkup_data.size) 
                            && (j < MAX_LOOKUP_RESPONSE_ADDRS); j++){

                    //addr
                    //T type
                    if (len >= (i+2)) {
                        rsp.lkup_data.addrs[j].type = 
                            ntohs(*(uint16_t *)(buf + i));
                    } else {
                        cerr << "ERROR: incomplete response" << endl;
                        return;
                    }
                    i += 2;
                    //L len
                    if (len >= (i+2)) {
                        rsp.lkup_data.addrs[j].len = 
                            ntohs(*(uint16_t *)(buf + i));
                    } else {
                        cerr << "ERROR: incomplete response" << endl;
                        return;
                    }
                    i += 2;
                    //V value
                    if (len >= (i + rsp.lkup_data.addrs[j].len)) {
                        rsp.lkup_data.addrs[j].value = buf + i;
                    } else {
                        cerr << "ERROR: incomplete response" << endl;
                        return;
                    }
                    i += rsp.lkup_data.addrs[j].len;
                }
            } else if (rsp.type == INSERT_RESPONSE) {
                //nothing to do here
            } else if (rsp.type == UPDATE_RESPONSE) {
                //TODO
            }

        /*

            //options TODO currently no options in responses.
		    //also, no count, have to check for final flag on opt type
            //num options
            if (len >= (i+1)) {
                rsp.num_opts = ntohl(*(uint16_t *)(buf + i));
            } else {
                cerr << "ERROR: incomplete response" << endl;
                return;
            }
            i += 2;
            for(int j = 0; (j < rsp.num_opts) && (j < MAX_RESPONSE_OPTS); j++){

                //option
                //T type
                if (len >= (i+2)) {
                    rsp.opts[j].type = 
                        ntohs(*(uint16_t *)(buf + i));
                } else {
                    cerr << "ERROR: incomplete response" << endl;
                    return;
                }
                i += 2;
                //L len
                if (len >= (i+2)) {
                    rsp.opts[j].len = 
                        ntohs(*(uint16_t *)(buf + i));
                } else {
                    cerr << "ERROR: incomplete response" << endl;
                    return;
                }
                i += 2;
                //V value
                if (len >= (i + rsp.opts[j].len)) {
                    rsp.opts[j].value = buf + i;
                } else {
                    cerr << "ERROR: incomplete response" << endl;
                    return;
                }
                i += rsp.opts[j].len;
            }
	*/
            rsp.status = RESPONSE_COMPLETE;
        }
};

#endif //GNRS_CXX_H
