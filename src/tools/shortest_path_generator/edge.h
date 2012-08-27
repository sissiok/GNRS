/* 
	Link between two ASes
	Directional Link (A->B link exist is different from B->A link)
*/
#include "common.h"
typedef struct edge_t {
	asNum src_AS;  //AS number
	asNum dst_AS;    //AS number
	u32b weight; // Link latency
		//double length; 
		//int weight;
} Edge;