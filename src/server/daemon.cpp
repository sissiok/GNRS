#include "daemon.h"

/*
locator_extractor returns the global locator or local locator from net_addr.
level=0: global level; level=1: local level
at the local level, the net_addr will be modified: the delimiter ':' will be replace as '\0'.
*/
char* daemon::locator_extractor(char* net_addr, int level)
{
	if (DEBUG >=1) cout<<"locator extractor: net_addr="<<net_addr<<",level="<<level<<endl;
	if(level==0)
	{
	   /* char* g_locator;
	    g_locator=new char[SIZE_OF_NET_ADDR];
	    memset(g_locator,'\0',SIZE_OF_NET_ADDR);
	    for(int i=0;net_addr[i]!=':'&&i<SIZE_OF_NET_ADDR;i++)
			g_locator[i]=net_addr[i];
		return(g_locator);  */
	
	    return(net_addr);

	}
	else
	{
		char* l_locator;
		l_locator=new char[SIZE_OF_NET_ADDR];
		memset(l_locator,'\0',SIZE_OF_NET_ADDR);
		int i;
		for(i=0;net_addr[i]!=':'&&i<SIZE_OF_NET_ADDR;i++) ;
		int n=i;
		net_addr[i]='\0';    //modify the net_addr
		for(i++;net_addr[i]!='\0'&&i<SIZE_OF_NET_ADDR;i++)
				l_locator[i-n-1]=net_addr[i];
		return(l_locator);
	}
}


/*
database_manager insert/update the mysql database.
there are 2 databases: global_guid_locators_map and local_guid_locators_map
level=0: global level; level=1: local level
present=0: insert to database; present=1: update the database
*/
void* daemon::database_manager(HashMap& hm,char* guid, vector<string*>* locators,vector<unsigned int*>* TTLs,vector<unsigned short*>* weights, int level, int present)
{
		if(DEBUG>=1) printf("enter database manager!\n");
		char *var1=guid;
                char *var2=new char[locators->size()*SIZE_OF_NET_ADDR];
		char *var3=new char[TTLs->size()*20];
		char *var4=new char[weights->size()*4];
		 memset(var2,'\0',locators->size()*SIZE_OF_NET_ADDR);
		 memset(var3,'\0',TTLs->size()*20);
		 memset(var4,'\0',weights->size()*4);
		vector<string*>::iterator it;
		//realize locator concat/append to the original mapping
		for(it=locators->begin();it<locators->end();it++)
		{
			strcat(var2,(*it)->c_str());
			if(it<locators->end()-1)
				strcat(var2," ");
		}
		vector<unsigned int*>::iterator it_;
		for(it_=TTLs->begin();it_<TTLs->end();it_++)
		{
			char str[20];
			//itoa(**it_,str,10);
			//sprintf(str,"%d",**it_);
			sprintf(str,"%u",**it_);
			strcat(var3,str);
			if(it_<TTLs->end()-1)
				strcat(var3," ");
		}

		vector<unsigned short*>::iterator it_weight;
		for(it_weight=weights->begin();it_weight<weights->end();it_weight++)
		{
			char str[4];
			sprintf(str,"%hu",**it_weight);
			strcat(var4,str);
			if(it_weight<weights->end()-1)
				strcat(var4," ");
		}
		char buffer[1024];
		if(DEBUG>=1) printf("prepare to enter mysql mutex!\n");
		pthread_mutex_lock(&mysql_mutex);
		if(DEBUG>=1) printf("enter mysql mutex!\n");
		if(present==0)  {
			try  {
				hm.get_value(guid);
				present=1;  }
			catch(const exception &ex) {}
		}

		if(level==1)
		{
			if(present){
				// replace the locator entry for the already present mapping. 
				//TODO  may imply concat/append semantics
	                 	 		sprintf(buffer,"REPLACE INTO local_guid_locators_map(guid,locators,TTLs,weights)values('%s','%s','%s','%s')",var1,var2,var3,var4);
			}else{
	                 	 		sprintf(buffer,"INSERT INTO local_guid_locators_map(guid,locators,TTLs,weights)values('%s','%s','%s','%s')",var1,var2,var3,var4);
			}
			updatecount =stmt->executeUpdate(buffer);
	              if (DEBUG >=1) cout<<"INSERTED INTO THE LNRS DATABASE SUCCESSFULLY"<<endl;
		}
		else
		{
         		if(present){
				// replace the locator entry for the already present mapping. 
				//TODO  may imply concat/append semantics
                     	 		sprintf(buffer,"REPLACE INTO global_guid_locators_map(guid,locators,TTLs,weights)values('%s','%s','%s','%s')",var1,var2,var3,var4);
			}else{
                     	 		sprintf(buffer,"INSERT INTO global_guid_locators_map(guid,locators,TTLs,weights)values('%s','%s','%s','%s')",var1,var2,var3,var4);
			}
			updatecount =stmt->executeUpdate(buffer);
                     if (DEBUG >=1) cout<<"INSERTED INTO THE GNRS DATABASE SUCCESSFULLY"<<endl;
		}
		if(DEBUG>=1) printf("prepare to release mysql mutex!\n");
		pthread_mutex_unlock(&mysql_mutex);
		if(DEBUG>=1) printf("release mysql mutex!\n");

		delete[] var2;
		delete[] var3;
		delete[] var4;

}

//though the time field in insert/lookup message is TTL, the time field at the server is expiring timestamp. The server needs to do the translation.
void* daemon::insert_handler(HashMap& hm,insert_message_t *ins, int level)
{

		struct timeval ts;
		gettimeofday(&ts, NULL);
		unsigned int expires;

		int i=0;
		vector<string*> *temp = new vector<string*>();
		vector<unsigned int*>* temp_ttl=new vector<unsigned int*>();
		vector<unsigned short*>* temp_weight=new vector<unsigned short*>();
		
		if (DEBUG >=1) cout<<"the number of locators:"<<htons(ins->na_num)<<endl;
		NA* nas=(NA *)(ins+1);
		while(i<htons(ins->na_num))
		{
			expires = ts.tv_sec+ntohl(nas[i].TTL);  //translation of TTL to timestamp
			temp->push_back(new string(locator_extractor(nas[i].net_addr,level)));							
			temp_ttl->push_back(new unsigned int(expires));
			temp_weight->push_back(new unsigned short(ntohs(nas[i].weight)));
			i++;
		}
		
                //insert the GUID:vector of ID's to HashMap datastructure
        	bool present = false;
		value v;
        	vector<string*>* locators = new vector<string*>();
		vector<unsigned int*>* TTLs=new vector<unsigned int*>();
		vector<unsigned short*>* weights=new vector<unsigned short*>();
        	try{
			v=hm.get_value(ins->guid);
			for(i=0;i<v.locator->size();i++)  {
				locators->push_back(new string(v.locator->at(i)->c_str()));
				TTLs->push_back(new unsigned int(*(v.expire->at(i))));
				weights->push_back(new unsigned short(*(v.weight->at(i))));
			}
			//locators = v.locator;
			//TTLs=v.expire;
			//weights=v.weight;
			while(!locators->empty())
			{
				//avoid duplicate locator
				int flag=0,j=0;
				if(DEBUG>=1) cout<<"temp size:"<<temp->size()<<",locator size:"<<locators->size()<<endl;
				while(flag==0&&j<temp->size())
				{
					if(DEBUG>=1) {
						printf("temp->at(%d):%s\n",j,temp->at(j)->c_str());
						printf("locators->back():%s\n",locators->back()->c_str());
						//cout<<"j="<<j<<endl<<"temp->at(j)->c_str():"<<temp->at(j)->c_str()<<",locators->back()->c_str():"<<locators->back()->c_str()<<endl;
					}
					if(strcmp( temp->at(j)->c_str() ,locators->back()->c_str())==0)
						flag=1;
					j++;
				}
				if(flag==0)
				{
					temp->push_back(locators->back());
					temp_ttl->push_back(TTLs->back());
					temp_weight->push_back(weights->back());
				}
				locators->pop_back();
				TTLs->pop_back();
				weights->pop_back();
			}
			present = true;
		}catch(const exception &ex){
		//doesn't exist
		//go ahead with insertion
		}
		hm.put(ins->guid,temp,temp_ttl,temp_weight);   //insert into hashmap
		if(DEBUG>=1) printf("insert GUID mapping: %s to hashmap.\n",ins->guid);

		//deal with mysql database
		database_manager(hm, ins->guid, temp, temp_ttl, temp_weight, level,present);
		delete locators;
		delete TTLs;
		delete weights;
}


//though the time field in insert/lookup message is TTL, the time field at the server is expiring timestamp. The server needs to do the translation.
bool daemon::lookup_handler(HashMap& hm,lookup_message_t *lkup, lookup_response_message_t* &resp,int level)
{
      // vector<string*>* locators=new vector<string*>();
//	vector<unsigned int*>* TTLs=new vector<unsigned int*>();
//	vector<unsigned short*>* weights=new vector<unsigned short*>();
        value v;
	vector<string*>* locators;
	vector<unsigned int*>* TTLs;
	vector<unsigned short*>* weights;
    	int i=0;
	bool expire_flag=false, valid_flag=false; //expire_flag=true: some NA in the entry expires. valid_flag=true: there is valid NA in the entry
	struct timeval ts;
        gettimeofday(&ts, NULL);
	try{
		v=hm.get_value(lkup->guid);
		locators = v.locator;
		TTLs=v.expire;
		weights=v.weight;
		//locators= hm.get_locator(lkup->guid);
		//TTLs=hm.get_ttl(lkup->guid);
		//weights=hm.get_weight(lkup->guid);
		unsigned int _TTL;
		while(i<TTLs->size())
		{
			//cout<<*(TTLs->at(i))<<" "<<ts.tv_sec<<endl;
			_TTL=*(TTLs->at(i))-ts.tv_sec;
			//cout<<_TTL<<endl;
			if(_TTL>0)
			{
				valid_flag=true;
				i++;
			}
			else
			{
				locators->erase(locators->begin()+i);
				TTLs->erase(TTLs->begin()+i);
				weights->erase(weights->begin()+i);
				expire_flag=true;
			}
		}	
   	}
	catch(exception &ex){  //not found   
	}
	//TODO: hm update + database update														
	if(expire_flag==true)
	{
		cout<<"GUID mapping fires!"<<endl;
		//cout<<"guid: "<<lkup->guid<<",locator size:"<<locators->size()<<endl;
		if(locators->size()==0) {
			hm.erase(lkup->guid);
			char buffer[1024];
			if(level==1)
				sprintf(buffer,"DELETE FROM global_guid_locators_map where guid='%s'",lkup->guid);
			else
				sprintf(buffer,"DELETE FROM global_guid_locators_map where guid='%s'",lkup->guid);
			pthread_mutex_lock(&mysql_mutex);
			updatecount =stmt->executeUpdate(buffer);
			pthread_mutex_unlock(&mysql_mutex);
		}
		else {
			hm.put(lkup->guid,locators,TTLs,weights);
			database_manager(hm, lkup->guid,locators,TTLs,weights,level,1);
		}
	}

	if(valid_flag==true)  {
		if(DEBUG>=1) cout<<"locator size:"<<locators->size()<<";TTL size"<<TTLs->size()<<";weight size:"<<weights->size()<<endl;

		resp = (lookup_response_message_t*)malloc(sizeof(lookup_response_message_t)+locators->size()*sizeof(NA));
		if(resp==NULL) {
			cout<<"create lookup response packet fail!"<<endl;
			exit(0);
		}
	 	resp->na_num=htons(locators->size());
 	
		NA* nas=(NA *)(resp+1);
		i=0;
		while(i<locators->size())
		{
			 strcpy(nas[i].net_addr, locators->at(i)->c_str());
			 nas[i].TTL=htonl(*TTLs->at(i)-ts.tv_sec);  //translation of timestamp to TTL
			 nas[i].weight=htons(*weights->at(i));
			 i++;
		}
		resp->resp_code = SUCCESS;
	}
	else {
		resp = (lookup_response_message_t*)malloc(sizeof(lookup_response_message_t));
		if(resp==NULL) {
			cout<<"create lookup response packet fail!"<<endl;
			exit(0);
		}
	 	resp->na_num=0;
 		resp->resp_code = ERROR;

	}

	strcpy(resp->c_hdr.sender_addr, GNRSConfig::server_addr.c_str());
	 resp->c_hdr.req_id = lkup->c_hdr.req_id;
	 resp->c_hdr.type = LOOKUP_RESP;
	 if(level==0)
		resp->c_hdr.sender_listen_port=htonl(GNRSConfig::daemon_listen_port+1);
	 else
		resp->c_hdr.sender_listen_port=htonl(GNRSConfig::daemon_listen_port);

	return(valid_flag);
}

