#ifndef DJ_H
#define DJ_H

#include <map>
#include <queue>
#include "common.h"

//Mapping NODE with EDGE
typedef map<asIdx, u32b>::iterator iter_asIdx_weight_map_t; 

//<EDGE, <NODE, EDGE>>  - element type of Priority Queue
typedef pair<u32b, iter_asIdx_weight_map_t> pair_weight_map_t;   

//comparison for Priority Queue
struct Rank : public binary_function<pair_weight_map_t, pair_weight_map_t, bool> {	
       bool operator()(const pair_weight_map_t& x, const pair_weight_map_t& y) const {
          return x.first > y.first;		//accending oder Priority Queue			
       }
};

//Priority queue
typedef priority_queue<pair_weight_map_t, vector<pair_weight_map_t>, Rank> my_priority_queue_t; 


typedef vector<asIdx> neigbor_list; 


class DJ{

public:
		//Constructor
		DJ(u32b num_ofAS, u32b **connectivityMatrix, const char *outputFileName);
		/*************MEMBERS***************/
		map<asIdx, u32b> distanceToOtherAS; //maps of distance from source to other AS
		my_priority_queue_t distanceQueue;  //queue of distances from source to other AS
		u32b **connMatrix;					//Connectity matrix 
		u16b numb_of_as; 
		vector<int> prevAs;	//immediate prev
		vector<u32b> distReslt; //distance to source node
		vector<bool> visited; 
		vector<neigbor_list> neighbor; 
		
		//Result storage
		u32b *dist_matx;				//shotest distance matrix 
		int *pre_matx;				//predicate matrix for path recompute
		

		/*************OPERATIONS***************/
		bool isEmptyQueue(); 
		void insertEdgeToQueue(asIdx _AS_index, u32b _weight); //inserting/update edge to a queue
		void getTopQueue(pair_weight_map_t &returnASidx);
		void updateEdge(asIdx _AS_index, u32b _newWeight); 
		int shortestPaths1toN(const asIdx sourceASidx); 
		int allshortestPathsNtoN(); 
		int exportRouteToFile(const char* fileName);

};		//end class definition

#endif //DJ_H