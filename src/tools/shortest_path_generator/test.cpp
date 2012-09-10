#include "common.h"
#include "topology.h"
#include <iostream>

using namespace std;

int main(int argc, char** argv)
{
if(argc < 2){
  cerr << "No input file specified." << endl;
  return -1;
}
Topology topology;
topology.init(argv[1]);

}
