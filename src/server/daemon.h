#ifndef DAEMON_H
#define DAEMON_H

#include<iostream>
#include<iomanip>
#include<fstream>
#include<string.h>
#include<stdio.h>
#include<stdlib.h>
#include <sys/time.h>


#include "../common/outgoingconnection.h"
#include "../common/incomingconnection.h"
#include "../common/Messages.h"
#include "../common/gnrsconfig.h"
#include "HashMap.h"
#include "../common/common.h"

#include<pthread.h>
#include <sstream>
#include <memory>
#include <stdexcept>
#include <cppconn/driver.h>
#include <cppconn/connection.h>
#include <cppconn/statement.h>
#include <cppconn/prepared_statement.h>
#include <cppconn/resultset.h>
#include <cppconn/metadata.h>
#include <cppconn/resultset_metadata.h>
#include <cppconn/exception.h>
#include <cppconn/warning.h>

//static int flag2;
using namespace std;
using namespace sql;

extern Driver *driver;
extern Connection *con;
extern Statement *stmt;
extern ResultSet *res;
extern PreparedStatement *prep_stmt;
extern int updatecount;

extern pthread_mutex_t mysql_mutex;

/* db global vars, set through configuration file */

#define NUMOFFSET 100
#define COLNAME 200 

class daemon{
public:
	static char* locator_extractor(char* net_addr, int level);
	static void* database_manager(HashMap& hm, char* guid, vector<string*>* locators,vector<unsigned int*>* TTLs,vector<unsigned short*>* weights, int level, int present);
	static void* insert_handler(HashMap& hm,insert_message_t *ins, int level);
	static bool lookup_handler(HashMap& hm,lookup_message_t *lkup, lookup_response_message_t* &resp,int level);
};

#endif

