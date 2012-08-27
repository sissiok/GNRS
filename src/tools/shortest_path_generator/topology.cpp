/*
	TOPOLOGY CLASS
		-Read input from connectivity file having <AS#,AS#,length> format
		-Compute shotest path between nodes 
		-Provide getShortestPath() query, that return a sequence of ASes
*/
#include <iostream>
#include <fstream>
#include <string>
#include <queue>
#include <map>
#include "memory.h"
#include "topology.h"
#include "edge.h"
#include "dj.h"

 
using namespace std;

Topology::Topology(){ //please compiler 
	//do nothing 
}
/*
	Constructor: 
		-Read input from Edge file
		-TODO: Read input from BGP file 
		-TODO: Verify BGP table match with edge file in terms of AS numbers 
		-Compute routing table
*/
void Topology::init(const char* filename){
	//Build topology graph
	readEdges(filename); 
	//compute routing table 
	if (DEBUG == 1 ) 
		cout<< " start compute routes " <<endl;  
	
	string routeFile(filename);
	routeFile += ".route"; 
	ifstream rFile(routeFile.c_str()); 
	if (rFile.is_open()){
		rFile.close(); 
		cout <<"Reading routing table" << endl; 
		readRoutingTable(routeFile.c_str(), nas);
		cout <<"Finished Reading routing table" << endl; 

	}
	else{
		//recompute the routing table
		//write it to a file as well
		DJ newRoutingTable(nas, conn_matx, routeFile.c_str());
		dist_matx = newRoutingTable.dist_matx; 
		pre_matx = newRoutingTable.pre_matx; 
	}
	//computeRoutes(nas, conn_matx,&dist_matx,&pre_matx);
	//dijkstraComputeRoutes(nas, conn_matx,&dist_matx,&pre_matx);
}
/*
	-Read topology from edge file
	-Indexing the ASnumber with AS index
*/
void Topology::readEdges(const char* filename){
	//READING INPUT FROM FILE
	ifstream topoFile(filename); //input File stream 
	string tempt_line;			//temp	
	u32b _nas=-1;				//Note: AS index start from 0
	int _as1, _as2;
	u32b _weight;
	Edge _tmpEdge; 
	vector<Edge> _edges; 
	//store mapping 
	map<asNum,asIdx>::iterator _mIt; 
	
	vector<Edge>::iterator _vIt; 
	int noLine =0; 
	if (topoFile.is_open())	{
		while (topoFile.good()){
			noLine++; 
			if ((DEBUG ==1 ) && (noLine%1000 == 0))
				cout << "percentage : " << (float)noLine*100/93600 <<endl;

			getline(topoFile,tempt_line);  //get line in topo file
			if (DEBUG == 1) cout << tempt_line << endl;
			//PARSE THE LINE 			
			if (cvertTopoLine2AsandWeight(tempt_line,' ', _as1,_as2,_weight) != 0){ 
				if (DEBUG ==1){ cerr <<"Topology input file error !!!\n" << endl; return; }
			}
			else {
				_tmpEdge.src_AS = _as1; _tmpEdge.dst_AS=_as2; _tmpEdge.weight = _weight; 
				_edges.push_back(_tmpEdge);  //store the edge
				
				if (as_idx_map.count(_as1) == 0){ //indexing ASnumb
					_nas++; 
					as_idx_map.insert(pair<asNum,asIdx>(_as1,_nas)); 
				}
				if (as_idx_map.count(_as2)== 0){
					_nas++; 
					as_idx_map.insert(pair<asNum,asIdx>(_as2,_nas)); 
				}
				
			}
		}	//end if can openfile 

		if ((DEBUG ==1 ))
				cout << "percentage : 100%"<< endl;

		/////TODO: Edge, Test Vector
		topoFile.close();
		nas = _nas+1;	//Number of ASes - Note "plus 1" compensates _nas		

		if ((DEBUG ==1 ))
				cout << "Number of ASes : 100%"<<nas << endl;

		//BUILD CONNECTIVIY MATRIX
		//a[i,j] is assigned MAX_EDGE_LENGTH if there is no EDGE
		init2DArray(nas, nas, &conn_matx); //allocate mem for connection matrix

		if ((DEBUG ==1 ))
				cout << "Just init"<< endl;

		idx_as_arr = new u32b[nas];				//allocate mem for idx->as array		
		
		for(_vIt = _edges.begin(); _vIt < _edges.end(); _vIt++){ //construc conn_matrix from edges vector 
			u32b _rowIdx = cvertASnum2idx(_vIt->src_AS); 
			u32b _colIdx = cvertASnum2idx(_vIt->dst_AS);
			conn_matx[_rowIdx][ _colIdx] = _vIt->weight; 	
			//Symetric
			conn_matx[ _colIdx][_rowIdx] = conn_matx[_rowIdx][ _colIdx]; 
		}
		if (DEBUG ==1){ 
			cout <<"//construc conn_matrix from edges vector" <<endl; 
			for (int _r=0; _r<nas; _r++){
				for(int _c=0; _c<nas;_c++){
					cout << conn_matx[_r][_c] << " " ; 
				}
				cout << endl; 
			}
		}
		
		if (DEBUG ==1) cout <<"//construct as->as mapping" <<endl; 

		for(_mIt = as_idx_map.begin(); _mIt != as_idx_map.end(); _mIt++){ //construct as->as mapping
			idx_as_arr[_mIt->second] = _mIt->first;
			if (DEBUG ==1) cout <<"Index:value " << _mIt->second << ":" << _mIt->first << endl;
		}
		ofstream as_File("AS_arr.data");
		for (int i=0;i<nas;i++)
			 as_File<<idx_as_arr[i]<<" ";
		 as_File<<endl;
		 as_File.close();
	 }
	else {cerr << "Unable to open file" <<endl; return; } //error 

	
}

/*
	--------------Convert A Topology line into a 3-tuple <AS1,AS2,Weight>----------------
*/
int Topology::cvertTopoLine2AsandWeight(const string &s, char delim, int &as1, int &as2,u32b &wei )
{
	
	stringstream ss(s); //convert to stream
	string item; 
	vector<string> storeLine; 
	
	while(getline(ss,item,delim)){ //extract from stream until it's empty
		if (DEBUG ==1) cout <<"item : " << item <<endl; 
		item.erase(item.find_last_not_of(" \n\r\t")+1); //eliminate spaces	
		if (item.size() >0)	storeLine.push_back(item);
	}	
	if (storeLine.size() != 3 )	{
		if (DEBUG == 1) cerr <<"Error on reading lines on Topology input file, size: " << storeLine.size() <<endl;
		return 1; 
	}
	//read as1
	item = *(storeLine.begin());
	as1 = atoi(item.c_str()); 
	if (DEBUG ==1) cout << "AS1: " << item << endl;
	//read as2
	item = *(storeLine.begin()+1); 
	as2 = atoi(item.c_str());
	if (DEBUG ==1) cout << "AS2: " << item << endl;
	//read weight
	item = *(storeLine.begin()+2);
	const char* temptcc = item.c_str(); 
	wei = (u32b)atoi(temptcc);	
	if (DEBUG ==1) cout << "Weight: " << item << endl;
	//done 
	return 0; 
}







/*
	------------Initialize 2 dimensional array of init elements---------------
*/
void Topology::init2DArray(int nRow, int nCol, u32b ***a){
	if ((DEBUG ==1 ))
				cout << "Init 2-D array%"<< endl;
	u32b **_a; 
	_a = new u32b*[nRow];
	
	if ((DEBUG ==1 )){
				cout << "Init 2-D array 2%"<< endl;
				if (_a == 0) cout<< "Init can't allocate that big trunk of memory%"<< endl;
	}
	_a[0] = new u32b[nRow*nCol];
	for(int i=1;i<nRow;i++)	{//may be don't need this since we don't access by indexes
		_a[i] = _a[i-1]+nCol;
	
	}
	if ((DEBUG ==1 ))
				cout << "Init 2-D array 3%"<< endl;
	for (int i=0; i<nRow;i++)
		for(int j=0;j<nCol;j++)
			if (i!=j) 
				_a[i][j] = MAX_EDGE_WEIGHT;  //no edge
			else 
				_a[i][j]=0;					//to itself is 0
	*a = _a; 
	return;
	
}
/*
-----------------ASconverter number to index---------------
return AS index if it is found 
and return total number of AS if it is not found
*/
asIdx Topology::cvertASnum2idx(asNum _asNumber){	
	map<asNum,asIdx>::iterator tmpIter = as_idx_map.find(_asNumber); 
	if (tmpIter != as_idx_map.end())
		return tmpIter->second; //return the index 
	else
	{
		cerr << "Can't find the ASNumber in <ASNum,ASIdx> mapping" << endl; 
		abort(); 
	}
	return 0; 
}
/*****************************************************************************************/
/*
	Dijkstra algoritm to compute routes between 2 ASes
	Input: **connectivity_matrix loaded in the previous step
	Output **shotest distance between any 2ASes
		   **predicate matrix helping rebuid the path
*/
void Topology::dijkstraComputeRoutes(u32b nn, u32b **cmat, u32b **distance_m, int **predicate_m){
	//u32b *_dist;  //storing result afterward 
	//int *_pred; 
	////initialize result matrix
	//_dist = (u32b*)malloc(sizeof(u32b) * nn* nn);  //shortest distance temporary variable
	//_pred = (int*) malloc(sizeof(int) * nn * nn );  //predicate 
	//memset(_dist, 0, sizeof(u32b) * nn * nn);	//zeros out
	//memset(_pred, -1, sizeof(int) * nn * nn);	//Zeros out
	//
	////dijkstra's variable 
	//u32b *dist_arr; //shortest distance from a vertex
	//int *pred_arr;  //predicessor along a path 
	//bool *marked; 
	//dist_arr = new u32b[nn];	
	//pred_arr = new int[nn];
	//marked = new bool[nn];

	//u16b startNode; 
	//
	////Considering all ASes as sources
	//for (startNode =0; startNode <nn; startNode ++){
	//	
	//	memset(marked, false, sizeof(bool) * nn);  //Checked or not 
	//	memset(dist_arr, MAX_EDGE_WEIGHT, sizeof(u32b)*nn); //maximize distances to other nodes
	//	memset(pred_arr, -1, sizeof(u32b)*nn);    //pref is undefined
	//	prioQue.empty(); 
	//	marked[startNode] = true;	

	//	//for(u16b i = 0; i< nn; i++){
	//	//	dist_arr[i] = cmat[startNode][i];
	//	// pushtoQUeue
	//	//}
	//	
	//	awMap_Iter_t it; 
	//	it-
	//	wawPair_t topQue;
	//	it->first = startNode; 
	//	it->second = 0; 
	//	awMap.insert(it); 
	//	prioQue.push(make_pair(0,it)); 
	//	
	//	while (!prioQue.empty()){
	//		topQue = prioQue.pop(); 	//get the minimum distance 		
	//		asIdx currAS = (topQue.second)->first; 
	//		marked[currAS] = true;		//markit 
	//		cout << "Pop: " << currAS <<endl;
	//		cout << "Distance from "<< startNode << " to " << currAS << " is: " << topQue.first << endl; 
	//		if (topQue.second == MAX_EDGE_WEIGHT){
	//			if (DEBUG == 1) 
	//				cout << "NETWORK PARTITIONED!!!!!!!!!!!!!!!!!!!!!!!" << endl; 
	//			break; 
	//		}
	//		
	//		for (u16b i=0; i<nn; i++){
	//			if ((!marked[i]) && (cmat[currAS][i] != MAX_EDGE_WEIGHT)){  //update neighbor
	//				it = awMap.find(i); //find if it is in the queue
	//				if (it == awMap.end())
	//					
	//				//change value in queue
	//				//push(n, cur->Y + e); 
	//					//find that element in queue
	//					//update distance value
	//					//push it back 
	//			}
	//		}

	//		

	//	}

	//	//while (!dijkstra.empty()) {
	//	// 
	//	// int curNode = dijkstra.pop();
	//	// marked[curNode] = true;
	//	// cout << "Pop: " << curNode <<endl;
	//	// cout << "Distance from "<< startNode << " to " << curNode << " is: " << dijkstra.dist() << endl; 
	//	// for (int i = 0; i < nn; i++)
 // //        if ((cmat[curNode][i] >= 0) && (!marked[i])) // "i" is a neighbor of curNode
 // //           dijkstra.link(i, cmat[curNode][i]); // add weighted edge
	//	//  }
	//	//u32b *distance = dijkstra.getDistance(); 
	//	
	//}
}


/*****************************************************************************************/


/*
	Floyd-Warshall algorithm to compute route betweeen any pair of ASes
	Note: Must pass in the empty pointers ditance_m and predicate_m as this
	function will handle malloc() calls 
*/
void Topology::computeRoutes(u32b nn, u32b **cmat, u32b **distance_m, int **predicate_m){

	u32b *_dist;
	int *_pred; 
	int i,j,k; //loop counters

	//initialize matrix
	_dist = (u32b*)malloc(sizeof(u32b) * nn* nn); 
	_pred = (int*) malloc(sizeof(int) * nn * nn ); 
	memset(_dist, 0, sizeof(u32b) * nn * nn);	//zeros out 
	memset(_pred, -1, sizeof(int) * nn * nn);	

	//Initialize algorithm
	for (i=0; i < nn; i++) {
		for (j=0; j < nn; j++) {
			if (cmat[i][j] != 0.0)	//exist edge between 2 ASes 
				_dist[i*nn+j] = cmat[i][ j];
			else
				_dist[i*nn+j] = MAX_EDGE_WEIGHT; //disconnected

			if (i==j)  //diagonal case
				_dist[i*nn+j] = 0;

			 if ((_dist[i*nn + j] > 0.0) && (_dist[i*nn+j] < MAX_EDGE_WEIGHT))
				_pred[i*nn+j] = i;

			  if (DEBUG == 1) cout <<"Entry "<< i<<","<<j<<" is " <<_dist[i*nn+j] <<endl; 
		}
	}

	//Main loop 
	for(k=0; k< nn; k++){
		if (DEBUG ==1)
			cout << "Trying k = : " << k <<endl; 

		for(i=0;i<nn;i++){
			for (j=0;j<nn;j++){
				if (_dist[i*nn+j] > (_dist[i*nn+k]+ _dist[k*nn+j])){
					 _dist[i*nn+j] = _dist[i*nn+k] + _dist[k*nn+j];
					 _pred[i*nn+j] = k; 
					 if (DEBUG == 1) cout <<"updated entry "<< i<<","<<j<<" to distance " <<_dist[i*nn+j] << "with K: "<< k << "which means AS# " << idx_as_arr[k] <<endl; 
				}
			}
		}
	}
	
	//Print out the result
	if (DEBUG ==1) {
		cout << "The routing table with shortest distance" <<endl; 
		for (i=0; i < nn; i++) {
			for (j=0; j < nn; j++)
				cout << _dist[i*nn+j] << " "; 
				cout << endl; 
		} 
		cout << "Predicate Matrix " <<endl; 
		for (i=0; i < nn; i++) {
			for (j=0; j < nn; j++)
				cout << _pred[i*nn+j] << " "; 
				cout << endl; 
		} 
	}

	//set the dist and pred matrices
	*distance_m = _dist; 
	*predicate_m = _pred; 

	return; 
}

void Topology::readRoutingTable(const char *routeFileName, u32b nn){
		
		cout << "route filename: " << routeFileName << endl; 
		ifstream openedFile(routeFileName);
		u32b numberOfas; 
		openedFile >> numberOfas; 
		
		cout << " number of AS read from routing file: "  << numberOfas << endl; 
		cout << " number of AS read from topology file: " << nn << endl; 
		
		if (numberOfas != nn ) {
			cerr << "Error, inconsistence between Routing file and topology file" << endl; 
			abort(); 
		}
		
		cout << "Reading routing table - Init Matrix " << endl;  
		//init
		dist_matx = (u32b*)malloc(sizeof(u32b) * nn* nn); 
		pre_matx = (int*) malloc(sizeof(int) * nn * nn ); 
		//zeros out 
		memset(dist_matx, 0, sizeof(u32b) * nn * nn);	
		memset(pre_matx, -1, sizeof(int) * nn * nn);
		
		cout << "reading Routing table - Distance " <<endl; 
		
		u32b dist_curr; 
		for (asIdx i=0; i < nn; i++) { //Read distance matrix 
			//cout <<"reading dist: " << i << ":" <<endl;
			for (asIdx j=0; j < nn; j++){
				openedFile >> dist_curr; 
				dist_matx[i*nn + j] = dist_curr; 
				 
			}
		}
		
		cout << "reading Routing table - Predicate " << endl; 
		int pred_curr;
		for (asIdx i=0; i < nn; i++) { //Read Predicate matrix 
			//cout <<"reading predicate: " << i << ":"<<endl; 
			for (asIdx j=0; j < nn; j++){
				openedFile >>  pred_curr; 
				pre_matx[i*nn + j] =  pred_curr; 
				
			}
		}
}


//get shotest path 
void Topology::getShortestPath(asIdx srcASidx, asIdx destASidx,vector<asIdx> &pathContainer ){	

	//Put dest in first
	//pathContainer.insert(pathContainer.begin(), destASidx); 
	//Init intermediate
	int _intermediate = pre_matx[srcASidx*nas + destASidx];
	
	//loop until reach the destination or reach it self
	while ((_intermediate != srcASidx) && (_intermediate != -1)){
		//cout << "getShortestPath:: intermediate"<<_intermediate <<endl;
		pathContainer.insert(pathContainer.begin(), _intermediate);
		//pathContainer.push_back(_intermediate);
		_intermediate = pre_matx[srcASidx*nas + _intermediate];
	}
}
/*
	Get a vector of AS indexes from source to destnation AS 
*/
vector<asIdx> Topology::getASidxOnPathfromAs2AS(asNum srcAS, asNum destAS){
	vector<asIdx> path;
	asIdx _srcIdx = cvertASnum2idx(srcAS);
	asIdx _destIdx = cvertASnum2idx(destAS);
	
	path.insert(path.begin(), _destIdx); //Dest Index 
	getShortestPath(_srcIdx, _destIdx, path);
	path.insert(path.begin(), _srcIdx);
	//printPath(path);
	return path; 
}
/*
	Get the shortest distance between 2 ASes
	Using AsNumber 
*/
u64b Topology::getShortestDistance(asNum srcAS, asNum destAS){
	asIdx _srcIdx = cvertASnum2idx(srcAS);
	asIdx _destIdx = cvertASnum2idx(destAS);

	return (u64b)dist_matx[_srcIdx*nas+_destIdx]; 
}
/*
	Get the shortest distance between 2 ases 
	Using AS indexes 
*/
u64b Topology::getShortestDistUsingASidx(asIdx srcAS, asIdx destAS){
	return (u64b)dist_matx[srcAS*nas +destAS]; 
}

string Topology::intToString(u32b num){
	std::string s;
	std::stringstream out;
	out << num;
	s = out.str();
	return s; 
}

/* 
print path for DEBUG only
*/
string Topology::printPath(vector<asIdx> path){
	string returnPath = ""; 
	vector<asNum>::iterator _iVtor; 
	for(_iVtor = path.begin(); _iVtor < path.end(); _iVtor++){
		// cout << idx_as_arr[*(_iVtor)] << "-->" <<endl; 
		returnPath = returnPath + intToString(idx_as_arr[*(_iVtor)]) + "\t" ;
	}
	return returnPath;
}
///** Testing in Main*/
//int main(){
//	const char* fname="topo5.data";
//	Topology myTopo; 
//	myTopo.init(fname);
//	myTopo.getASidxOnPathfromAs2AS(5,1); 
//	return 0; 
//	
//}
