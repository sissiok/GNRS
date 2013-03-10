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
#ifndef GUID_H
#define GUID_H

#include <sstream>
#include <string>
#include <string.h>

using namespace std;

#define GUID_BINARY_SIZE 20

class Guid {

    public:
        uint8_t bytes[GUID_BINARY_SIZE];
        string str;

        static Guid fromUnsignedInteger(uint32_t guid) {

            Guid g;
		    memset(g.bytes, 0x00, 16);	
            for(int i = 1; i < 5; i++) {  
                g.bytes[GUID_BINARY_SIZE - i] = (guid << (8*(4-i))) >> 24; 
            }
            g.str = static_cast<ostringstream*>
                        (&(ostringstream() << guid))->str();
            return g;
        }

        static Guid fromLongUnsignedInteger(uint64_t guid) {

            Guid g;
		    memset(g.bytes, 0x00, 12);	
            for(int i = 1; i < 9; i++) {  
                g.bytes[GUID_BINARY_SIZE - i] = (guid << (8*(4-i))) >> 56; 
            }
            g.str = static_cast<ostringstream*>
                        (&(ostringstream() << guid))->str();
            return g;
        }
};


#endif //GUID_H
