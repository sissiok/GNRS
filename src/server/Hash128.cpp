#include <iostream>
#include <fstream>
#include <vector>
#include "MD5.h"

#include "../common/time_measure.h"
#include "../common/gnrsconfig.h"
#include "Hash128.h"

using namespace std;

//extern struct timeval starttime_,endtime_;
//extern struct timespec starttime,endtime,starttime_,endtime_;
//extern ofstream ProcLagFile;

string Hash128::HashG2Server(char *GUID)
{      

	//clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime_);
       char* var1 = md5.digestString(GUID);
	//clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &endtime_);
	//ProcLagFile<<timeval_diff(starttime_,endtime_)<<' ';
		
       char var2[8];
       for (int i=0;i<8;i++)
              var2[i]=var1[i];
	   
//clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &starttime_);
        int hashval= globalHash(var2,server_list.size());
       //cout<<hashval<<endl; 
       if(DEBUG>=1) cout<<"inside Hash function"<<server_list.at(hashval)<<endl; 
       string var3 = server_list.at(hashval); 
	if(DEBUG>=1) cout << "Hash returning IP: " << var3 << " for GUID: " << GUID << endl;
//clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &endtime_);
//ProcLagFile<<timeval_diff(starttime_,endtime_)<<' ';
      return var3; 
}

//one md5 hash can generate 4 IP addresses, the hashIndex choose one from those 4.
uint32_t Hash128::HashG2IP(char *GUID, u8b hashIndex)
{
       char* var1 = md5.digestString(GUID);
       //cout<<var1<<endl;
       char var2[8];
       for (int i=0;i<8;i++)
              var2[i]=var1[i+8*hashIndex];

        return globalHash(var2);
}


uint32_t Hash128::HashIP2IP(uint32_t IP)
{
	char bitstringIP[32];
	uint32_t tmp = IP; 
	for (int i=31;i>=0;i--){
		bitstringIP[i] = tmp & 1;
		tmp = tmp >> 1; 
	}
       char* var1 = md5.digestString(bitstringIP);
       //cout<<var1<<endl;
       char var2[8];
       for (int i=0;i<8;i++)
              var2[i]=var1[i];

        return globalHash(var2);
}


string Hash128::MapAS2Server(asNum asNumber)
{
	return(server_list.at(asNumber%server_list.size()));

}

//int main()
//{
 //         Hash128 h;
  //        string vari;
   //      char *GUID="G88878787542";   
  //    vari =h.Hash(GUID); 
  //     cout<<vari<<endl;
//        return 0;
//}


int mapfunction(char var)
{
       switch(var)
       {
           case '0':
                      return 0;break;
           case '1':
                      return 1;break;
           case '2':
                      return 2;break;
           case '3':
                      return 3;break;
           case '4':
                      return 4;break;
           case '5':
                      return 5;break;
           case '6':
                      return 6;break;
           case '7':
                      return 7;break;
           case '8':
                      return 8;break;
           case '9':
                      return 9;break;
           case 'a' :
                      return 10;break;
           case 'b':
                      return 11;break;
           case 'c':
                      return 12;break;
           case 'd':
                      return 13;break;
           case 'e':
                      return 14;break;
           case 'f':
                      return 15;break;
           case 'g':
                      return 16;break; 
           case 'h':
                      return 17;break;
           case 'i':
                      return 18;break;
           case 'j':
                      return 19;break;
           case 'k':
                      return 20;break;
           case 'l':
                      return 21;break; 
           case 'm':
                      return 22;break;
           case 'n':
                      return 23;break;
           case 'o':
                      return 24;break;
           case 'p':
                      return 25;break;
           case 'q':
                      return 26;break; 
           case 'r':
                      return 27;break;
           case 's':
                      return 28;break;
           case 't':
                      return 29;break;
           case 'u':
                      return 30;break;
           case 'v':
                      return 31;break;
           case 'w':
                      return 32;break;
           case 'x':
                      return 33;break;
           case 'y':
                      return 34;break;
           case 'z': 
                      return 35;break;
           default :
                      cout<<"Invalid character"<<endl;
                      return 36;
                      break;
       }

}


//used in HashG2Server
int Hash128::globalHash(char guid[],int count)
{
	int mappedval;
       unsigned long value=0; 
        for(int i=0;i<8;i++)
        {
             mappedval= mapfunction(guid[i]);
             value=value + (mappedval* pow(16,i));
        } 
      int val=value%count;
       return val;
}


//used in HashG2IP
uint32_t Hash128::globalHash(char guid[])
{
	int mappedval;
       uint32_t value=0; 
        for(int i=0;i<8;i++)
        {
             mappedval= mapfunction(guid[i]);
             value=value + (mappedval* pow(16,i));
        } 
       return value;
}
