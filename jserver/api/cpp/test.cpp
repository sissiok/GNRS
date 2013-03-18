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
#include "guid.h"
#include "net_addr.h"
#include "gnrs_cxx.h"

#include <iostream>
#include <string>
#include <stdlib.h>

using namespace std;

void
print_usage(char* exec_name) {

	cout << "usage: " << exec_name 
        << " <server ip:port> <self ip:port> [guid: int]" << endl;
}

int
main(int argc, char* argv[]) {

	if (argc < 3) {
		print_usage(argv[0]);
		return 1;
	}
	int guid_num = 23483098;
	if(argc > 3){
		guid_num = atoi(argv[3]);
	}
    
    //string server_addr_s("127.0.0.1:5001");
    //string local_addr_s("192.168.1.1:3001");
    string server_addr_s(argv[1]);
    string local_addr_s(argv[2]);
    NetAddr server_addr(NET_ADDR_TYPE_IPV4_PORT, server_addr_s);
    NetAddr local_addr(NET_ADDR_TYPE_IPV4_PORT, local_addr_s); 

    //configure service endpoints
    Gnrs gnrs(server_addr, local_addr);

    Guid guid = Guid::from_unsigned_int(guid_num); 

    //insert operation
    list<NetAddr> addrs;
    addrs.push_back(local_addr);
    gnrs.add(guid, addrs);

    //lookup operation
    list<NetAddr> lkup_addrs = gnrs.lookup(guid);

    cout << lkup_addrs.size() << " result(s) from lookup for guid: " 
        << guid.str.c_str() << " :" << endl;
    int num_addrs = 0;
    for (list<NetAddr>::const_iterator it = lkup_addrs.begin(); 
                            it != lkup_addrs.end(); it++) {
        num_addrs++;
        cout << "\t" << num_addrs << ":\t" << (*it).value << endl;
    }

    return 0;
}
