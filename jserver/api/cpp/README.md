# GNRS C++ API #
A C++ interface providing messaging based-access to the Global Name 
Resolution Service which provides a fast distributed directory to store 
and retrieve GUID-to-locator mappings for the MobilityFirst network 
architecture. It is intended as a prototype implementation and is
not suitable for commercial or enterprise use.

## API ##

Here's an example showing basic use of the api:

	/* required header files */
	#include "guid.h"
	#include "net_addr.h"
	#include "gnrs_cxx.h"

	.....

	int main() {

		/* local and server network endpoint addresses */
	    string server_addr_s("127.0.0.1:5001");
	    string local_addr_s("192.168.1.1:3001");

		/* Note: addresses can also be constructued as binary encoded
		 * byte arrays.
		 */
	    NetAddr server_addr(NET_ADDR_TYPE_IPV4_PORT, server_addr_s);
	    NetAddr local_addr(NET_ADDR_TYPE_IPV4_PORT, local_addr_s); 

		/* GNRS service constructor */
	    Gnrs gnrs(server_addr, local_addr);

		/* use helper functions to prepare GUID */
	    Guid guid = Guid::fromUnsignedInteger(23483098); 

	    /* insert operation: stores id-locator mapping in GNRS */.
	    list<NetAddr> addrs;
		/* add list of locators (network addresses) mapped to the GUID */
	    addrs.push_back(local_addr);

		/* 
		 * The 'add' method appends specifed locators to set of other 
		 * locators previously mapped to the GUID. 
		 * To replace existing mappings, use 'replace' method (to be
		 * implemented).
		 */
	    gnrs.add(guid, addrs);

		/*
		 * The 'lookup' operation retrieves current set of locator 
		 * mappings for a given GUID.
		 */
	    list<NetAddr> lkup_addrs = gnrs.lookup(guid);

	    for (list<NetAddr>::const_iterator it = lkup_addrs.begin(); 
				    				it != lkup_addrs.end(); it++) {
			cout << (*it).value << endl;
	    }
		return 0;
	}

## Test Client ##
A test client is included to test the API - test.cpp

This can be compiled as:
	g++ gnrs_cxx.cpp test.cpp -o test_client	

The client takes 2 command-line arguments: server address and local address:

	usage: ./test_client <server ip:port> <self ip:port>

* server address - This is the network address at which one of the service
  instances can be reached. Presently the API supports only IPv4-UDP endpoints 
  for messaging. Example server address: 192.168.1.1:5001. Obtain server 
  endpoint information from GNRS service operator.
* local address - Specify a local IPv4-UDP endpoint for receiving response
  messages from the server. E.g., 192.168.1.1:3000

Note the above address configurations indicate the client can be run on
the same machine as the server.

Unless otherwise noted in the application or in the source code, all 
other resources, including but not limited to source code, artwork, 
documentation, are copyright (C) 2013 Wireless Information Laboratory (WINLAB)
and Rutgers University.  All rights reserved.
# GNRS C++ API #
