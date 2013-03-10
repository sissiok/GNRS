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
#ifndef UDPIPV4_ENDPOINT_H
#define UDPIPV4_ENDPOINT_H

#include <iostream>
#include <stdlib.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <time.h> 

#include "net_addr.h"

using namespace std;

class UdpIpv4Endpoint {

    private:
	    int sock;
	    struct sockaddr_in dest;
	    struct sockaddr_in local;

    public:
        UdpIpv4Endpoint(NetAddr dest_addr, NetAddr local_addr) {

            if(dest_addr.type != NET_ADDR_TYPE_IPV4_PORT ||
                local_addr.type != NET_ADDR_TYPE_IPV4_PORT) {
                cerr << "FATAL: Address type (dest/lcl) '" 
                    << dest_addr.type
                    << "/" << local_addr.type
                    << "' not supported!" << endl;
                exit(EXIT_FAILURE);
            }

            struct hostent *dest_h;
            if (!(dest_h = gethostbyname(
                            dest_addr.get_hostname_or_ip().c_str()))) {
                cerr << "FATAL: gethostbyname failed for host: " 
                    << dest_addr.get_hostname_or_ip()
                    << endl;
                exit(EXIT_FAILURE);
            }

            dest.sin_family = AF_INET;
            memcpy((char*)&dest.sin_addr.s_addr, dest_h->h_addr_list[0], 
                                                    dest_h->h_length);
            dest.sin_port = htons(dest_addr.get_port());

            struct hostent *local_h;
            if (!(local_h = gethostbyname(
                            local_addr.get_hostname_or_ip().c_str()))) {
                cerr << "FATAL: gethostbyname failed for host: " 
                    << local_addr.get_hostname_or_ip()
                    << endl;
                exit(EXIT_FAILURE);
            }

            local.sin_family = AF_INET;
            memcpy((char*)&local.sin_addr.s_addr, local_h->h_addr_list[0], 
                                                    local_h->h_length);
            local.sin_port = htons(local_addr.get_port());

            if((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0){
                cerr << "FATAL: cannot create socket!" 
                    << endl;
                exit(EXIT_FAILURE);
            }

            //bind local addr/port
            if (bind(sock, (struct sockaddr *) &local, sizeof(local)) < 0) {
                cerr << "FATAL: local bind failed!" 
                    << endl;
                exit(EXIT_FAILURE);
            }

        }

        int _send(unsigned char* buf, int len) {

            int rc;

            if ((rc = sendto(sock, buf, len, 0, (struct sockaddr *) & dest,
                                                    sizeof(dest))) < 0) {
                cerr << "ERROR: sendto failed, err: " << strerror(errno) 
                    << endl;
            }
            return rc;
        }

        int _recv(unsigned char* buf, int len) {

            int rc;

            if ((rc = recv(sock, buf, len, 0)) < 0) {
                cerr << "ERROR: recv failed, err: " << strerror(errno) << endl;
            }
            return rc;
        }
};

#endif //UDPIPV4_ENDPOINT_H
