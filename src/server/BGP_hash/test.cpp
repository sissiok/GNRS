#include <iostream>
#include "common.h"
#include "hash.h"

int main()
{
   // u64b GUID=
    u32b destIP = Hash::hashG2IP(12,1);
    cout<<"hashed IP="<<destIP<<endl;
}
