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
#include <string.h>
#include <stdlib.h>

using namespace std;

void
print_usage(char* exec_name) {

	cout << "usage: " << exec_name 
        << " <server ip:port> <self ip:port> <guid: int> "
        << "'[guid1, guid2, ...]'" << endl
        << " E.g.,: " << exec_name 
        << " 127.0.0.1:5001 127.0.0.1:3009 234234 '[23, 3474, 234]'" 
        << endl;
}

int
main(int argc, char* argv[]) {

	if (argc < 5) {
		print_usage(argv[0]);
		return 1;
	}
	int guid_num;
	guid_num = atoi(argv[3]);
    /* 
     * process the guid list 
     * expecting: [guid1,guid2,guid3...guidN]
     * extra spaces are ok 
     */
    int num_guid_locators = 0;
    const char* str_index = argv[4];
    const char* start_index;
    /* get index beyond the start separator '[' */
    if (!(start_index = strchr(str_index, '['))) {
        /* syntax issue */
        print_usage(argv[0]);
        return 1;
    }
    list<NetAddr> addrs;
    str_index = start_index + 1;
    while (str_index < (argv[4] + strlen(argv[4]))) {
        const char* end_index;
        if ((end_index = strchr(str_index, ','))
                || (end_index = strchr(str_index, ']'))) {
            /* we have a guid followed by a separator */
            char tmp[32]; 
            memset(tmp, 0, 32);
            memcpy(tmp, str_index, end_index - str_index);
            string guid_str(tmp);
            cout << "DEBUG: \t found locator: " << guid_str << endl;
            NetAddr na(NET_ADDR_TYPE_GUID, guid_str);
            addrs.push_back(na);
            num_guid_locators++;
            str_index = end_index + 1;
        } else {
            //syntax issue
            print_usage(argv[0]);
            return 1;
        }
    }
    cout << "DEBUG: Total of " << num_guid_locators 
        << " locator mappings will be added" << endl;
    
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
    cout << "INFO: Executing add op..." << endl; 
    gnrs.add(guid, addrs);

    //lookup operation
    cout << "INFO: Executing lookup op (to validate add)..." << endl; 
    list<NetAddr> lkup_addrs = gnrs.lookup(guid);

    cout << "INFO: " << lkup_addrs.size() 
	<< " result(s) from lookup for guid: " 
        << guid.str.c_str() << endl;
    int num_addrs = 0;
    for (list<NetAddr>::const_iterator it = lkup_addrs.begin(); 
                            it != lkup_addrs.end(); it++) {
        num_addrs++;
        cout << "INFO: \t Addr #" << num_addrs << " : " << (*it).value << endl;
    }

    return 0;
}
