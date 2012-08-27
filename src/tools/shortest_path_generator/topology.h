/*
 Representing topology graph by edge matrix file
 each line is a 3-tuple of ASes: <AS#,AS#,length> 
 AS2 - to - AS3. This is directional edge 
*/
#ifndef TOPOLOGY_H
#define TOPOLOGY_H
#include <map>
#include "common.h"


using namespace std;

//typedef map<asIdx, u32b>::iterator awMap_Iter_t; //Node and distance to that node
//typedef pair<u32b, awMap_Iter_t> wawPair_t;					//Iterator that has that Edge val
//struct Rank : public binary_function<wawPair_t, wawPair_t, bool> {
//			bool operator()(const wawPair_t& x, const wawPair_t& y) const {
//				return x.X < y.X;
//			}
//		};
//
//typedef priority_queue<wawPair_t, vector<wawPair_t>, Rank> MyPrioQueue_t;

class Topology
{
public:	
	//Get Topology from file with above format 
	//then COMPUTE ROUTING TABLE
	Topology();
	void init(const char* filename); 
	
	//*********************MEMBER*******************
	u32b nas;						//number of ASes
	map<asNum,asIdx> as_idx_map;   //indexing ASes
	u32b *idx_as_arr;				// for ASidx -> asNumber lookup
	

	////*********************OPERATION*******************

	//make sure only and all ASes on BGP table is included
	//bool isLegal(const BGPtable* inpBGP);
	
	//Read Routing Table from file
	void readRoutingTable(const char *routeFileName, u32b nn);

	//Floyd-Warshall algorithm to compute route betweeen any pair of ASes
	void computeRoutes(u32b nn, u32b **cmat, u32b **distance_m, int **predicate_m);

	//Dijkstra computing routes 
	void dijkstraComputeRoutes(u32b nn, u32b **cmat, u32b **distance_m, int **predicate_m);

	// read edges from input file 
	void readEdges(const char* filename);

	//get distance between ASes in terms of number of TICKS 
	u64b getShortestDistance(asNum srcAS, asNum destAS);  //using AS number
	u64b getShortestDistUsingASidx(asIdx srcAS, asIdx destAS); //using   AS index  
	
	//get vector of Indexes of ASes that a packet must go through from src->dst 
	vector<asIdx> getASidxOnPathfromAs2AS(asNum srcAS, asNum destAS);
	void getShortestPath(asIdx srcASidx, asIdx destASidx,vector<asIdx> &pathContainer); 
	//get nexthop on path between 2 ASes
	asIdx getNexthop(asIdx as1, asIdx as2); //using asIndexes
	
	//ASconverter number to index
	asIdx cvertASnum2idx(asNum number);

	string printPath(vector<asNum> path);

private:

	//*********************MEMBER*******************
	u32b **conn_matx;				//connectivity matrix 
	u32b *dist_matx;				//shotest distance matrix 
	int *pre_matx;				//predicate matrix for path recompute

	//*********************OPERATION*******************
	//Converting topology line to tuple of <AS,AS,Weight> - used by constructor
	int cvertTopoLine2AsandWeight(const string &s, char delim, int &as1, int &as2,u32b &wei );
	//Initialize double array
	void init2DArray(int nRow, int nCol, u32b ***a);
	string intToString(u32b num);
	
	/*MyPrioQueue_t prioQue;
	map<asIdx,u32b> awMap; */
	

};


#endif //TOPOLOGY_H
