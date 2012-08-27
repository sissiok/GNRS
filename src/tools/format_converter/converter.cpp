#include <stdio.h>
#include <string.h>
#include <string>
#include <vector>
#include <iostream>
#include <fstream>
#include <sstream>

using namespace std;

int main()
{
	ifstream in_file("omf_52");
	ofstream out_file("omf_53");
	char in[3000];

        // get length of file:
	in_file.seekg (0, ios::end);
	int length = in_file.tellg();
	in_file.seekg (0, ios::beg);

	memset(in,'\0',3000);
	in_file.read(in,length);
	//cout<<in<<endl<<in_file.gcount()<<endl;

	int i=0,j=0,num=0;
	char x[3],y[3];
	x[2]=y[2]='\0';
	
	while(i<length)  {
		x[0]='\0';
		x[1]='\0';
		y[0]='\0';
		y[1]='\0';
		while(in[j]!=',')  j++;
		memcpy(x,in+i+1,j-i-1);
		i=j;
		while(in[j]!=']')  j++;
		memcpy(y,in+i+1,j-i-1);
		if(num>0)  out_file<<",";
		out_file<<"node"<<x<<"-"<<y<<".grid.orbit-lab.org";
		num++;
		i=j+2;
		j=i;
	}
	in_file.close();
	out_file.close();
		

}
