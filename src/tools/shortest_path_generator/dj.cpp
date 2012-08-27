#include "dj.h"

//Constructor
DJ::DJ(u32b num_ofAS, u32b **connectivityMatrix, const char *outputFileName){
	//init number of ASes
	numb_of_as = num_ofAS; 

	//Init connectivity table
	connMatrix = connectivityMatrix;
	for(asIdx i = 0; i<num_ofAS; i++){
		neigbor_list tmp; 
		neighbor.push_back(tmp);
	}
	//Init Neighbour list
	for(asIdx i = 0; i<num_ofAS; i++)
		for(asIdx j = 0; j<num_ofAS; j++)
			if(connectivityMatrix[i][j] != MAX_EDGE_WEIGHT )
				neighbor[i].push_back(j);
				//neighbor[i].size();

	//init visited vector -- mem allocation
	for(asIdx i = 0; i<num_ofAS; i++)
		visited.push_back(false);

	//init prev vector -- mem allocation
	for(asIdx i = 0; i<num_ofAS; i++)
		prevAs.push_back(-1); 

	//init prev vector -- mem allocation
	for(asIdx i = 0; i<num_ofAS; i++)
		distReslt.push_back(MAX_EDGE_WEIGHT); 

	//Init result storage
		asIdx nn = numb_of_as; //lazy
	//initialize matrix
	dist_matx = (u32b*)malloc(sizeof(u32b) * nn* nn); 
	pre_matx = (int*) malloc(sizeof(int) * nn * nn ); 
	//zeros out 
	memset(dist_matx, 0, sizeof(u32b) * nn * nn);	
	memset(pre_matx, -1, sizeof(int) * nn * nn);
	
	//compute all paths
	if (allshortestPathsNtoN() == 0) 
		//write to the file
		exportRouteToFile(outputFileName);

}

/*
ShortestPaths from sourceAS to all other ASes
Result is stored on prevAS and distReslt array
Return: 
	0 if can find path to all other ASes
	-1 if network is partitioned
*/
int DJ::shortestPaths1toN(const asIdx sourceASidx){
	cout << "DJ: "<< sourceASidx <<endl; 
	iter_asIdx_weight_map_t tmp_it; //tmp
	u16b num_of_visited = 0; 
	

	//Init
	for(asIdx i=0;i< numb_of_as; i++){
		distanceToOtherAS[i] = connMatrix[sourceASidx][i];  //map -- to all ASes
		tmp_it = distanceToOtherAS.find(i);
		distanceQueue.push(pair<u32b,iter_asIdx_weight_map_t>(tmp_it->second,tmp_it)); //queue  -- to all ASes
		prevAs[i] = sourceASidx; 
		visited[i]= false;
	}


	//Dijkstra algorithm
	while((!distanceQueue.empty()) &&(num_of_visited <numb_of_as)){
		pair_weight_map_t cur_queue_element; 
		cur_queue_element = distanceQueue.top(); //pop the one with smallest distance
		distanceQueue.pop(); 
		
		asIdx curr_ASidx = (cur_queue_element.second)->first; //index of AS

		if (!visited[curr_ASidx]){

			u32b minDistance = (cur_queue_element.second)->second; //distance to source Node

			//Harvest result here 
			distReslt[curr_ASidx] = minDistance;
			visited[curr_ASidx]=true; //mark visited
			num_of_visited++; 

			if (cur_queue_element.first == MAX_EDGE_WEIGHT){  //can't find distance to all ASes
				if (DEBUG == 2) 
					cerr << "NETWORK PARTITIONED!!!!!!!!!!!!!!!!!!!!!!!" << endl; 
				break; 
			}
			
			//update distances 
			for (u16b i=0; i<neighbor[curr_ASidx].size();i++) //scan through neighbor of that AS
				if (!visited[neighbor[curr_ASidx][i]]){  //need to be updated
					tmp_it = distanceToOtherAS.find(neighbor[curr_ASidx][i]); 
					if (tmp_it->second > minDistance + connMatrix[curr_ASidx][neighbor[curr_ASidx][i]]){
						tmp_it->second = minDistance + connMatrix[curr_ASidx][neighbor[curr_ASidx][i]]; //update distance
						prevAs[neighbor[curr_ASidx][i]] = curr_ASidx; //prev 
						distanceQueue.push(pair<u32b,iter_asIdx_weight_map_t>(tmp_it->second,tmp_it));  
					}
				} //end inner if not visited
		} //end outter if not visited
	}  //end while

	if (num_of_visited < numb_of_as ){  //can't find distance to all ASes
		if (DEBUG == 2) 
			cerr << "NETWORK PARTITIONED!!!!!!!!!!!!!!!!!!!!!!!" << endl; 
		return -1; 
	}
		
	return 0; 
}

int DJ::allshortestPathsNtoN(){

	//Initialize algorithm
	for (asIdx i=0; i < numb_of_as; i++) {
		if (shortestPaths1toN(i) != 0)
			return -1; 
		for (asIdx j=0; j < numb_of_as; j++) {
			dist_matx[i*numb_of_as + j] = distReslt[j];
			pre_matx[i*numb_of_as + j] = prevAs[j];
		}
	}
	return 0; 
}

int DJ::exportRouteToFile(const char* fileName){

	ofstream File(fileName);

	if (File.is_open()){
		File << numb_of_as << endl; //size 

		for (asIdx i=0; i < numb_of_as; i++) { //print distance matrix 
			cout << "writing route to file - Distance - row : " << i << endl;
			for (asIdx j=0; j < numb_of_as; j++) {
			File << dist_matx[i*numb_of_as + j] << " "; 
			}
			File << endl; 
		}

		for (asIdx i=0; i < numb_of_as; i++) { //print dist first 
			cout << "writing route to file - Predicate - row : " << i << endl;
			for (asIdx j=0; j < numb_of_as; j++) {
			//File << pre_matx[i*numb_of_as + j] << " "; 
			}
			//File << endl; 
		}
		File.close();
		return 0; 
	}
	else {
		cerr << "Unable to open file" <<endl;
		return -1; 
	}
}
	
