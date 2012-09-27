#include "gnrsconfig.h"

#include <libconfig.h++>

using namespace std;

//DB configuration
string GNRSConfig::db_host;
int    GNRSConfig::db_port;
string GNRSConfig::db_name;
string GNRSConfig::db_user; 
string GNRSConfig::db_passwd;
string GNRSConfig::db_mappings_table;

/* GNRS daemon configuration */
int GNRSConfig::daemon_listen_port;
string GNRSConfig::server_addr; //self identity/addr
int GNRSConfig::hash_func;
int GNRSConfig::stl_func;
int GNRSConfig::thread_pool_size;
int GNRSConfig::service_req_num;

/* GNRS client configuration */
int GNRSConfig::client_listen_port;
string GNRSConfig::client_addr; 

/* file listing of addresses of GNRS servers */
string GNRSConfig::servers_list_file;

/*
 * Read configuration settings from file and update any
 * default settings.
 * 
 * Exits program with error code either on i/o or parse errors while 
 * reading the file, or when a required setting hasn't been specified
 */ 

void GNRSConfig::read_from_file(const char*  filename){

	libconfig::Config cfg;
	try{
		cfg.readFile(filename);
	}catch(const libconfig::FileIOException &fioex){
		cerr << "I/O error while reading file." << endl;
		exit(1);
	}catch(const libconfig::ParseException &pex){
		cerr << "Parse error at " << pex.getFile() << ":" 
			<< pex.getLine() << " - " << pex.getError() << endl;
		exit(1);
	}

	/* read in database related configurations */
	if(!cfg.lookupValue("DB_HOSTNAME", db_host)){
		cout << "INFO: " 
			<< "DB_HOSTNAME not defined in config, using default: " 
			<< db_host << endl;
	}
	if(!cfg.lookupValue("DB_PORT", db_port)){
		cout << "INFO: " 
			<< "DB_PORT not defined in config, using default: " 
			<< db_port << endl;
	}
	if(!cfg.lookupValue("DB_NAME", db_name)){
		cout << "FATAL: " << "DB_NAME not defined in config, exiting" 
			<< endl;
		exit(1);
	}
	if(!cfg.lookupValue("DB_USER", db_user)){
		cout << "FATAL: " << "DB_USER not defined in config, exiting" 
			<< endl;
		exit(1);
	}
	if(!cfg.lookupValue("DB_PASSWD", db_passwd)){
		cout << "FATAL: " << "DB_PASSWD not defined in config, exiting" 
			<< endl;
		exit(1);
	}
	if(!cfg.lookupValue("DAEMON_LISTEN_PORT", daemon_listen_port)){
		cout << "DAEMON_LISTEN_PORT not defined in config,"
			<< "using default: " 
			<< daemon_listen_port << endl;
	}

	if(!cfg.lookupValue("SERVER_ADDR", server_addr)){
		cout << "SERVER_ADDR not defined in config,"
			<< "using default: " 
			<< server_addr << endl;
	}

	if(!cfg.lookupValue("HASH_FUNC", hash_func)){
		cout << "HASH_FUNC not defined in config,"
			<< "using default: " 
			<< hash_func << endl;
	}


	if(!cfg.lookupValue("STL_FUNC", stl_func)){
		cout << "STL_FUNC not defined in config,"
			<< "using default: " 
			<< stl_func << endl;
	}

	if(!cfg.lookupValue("THREAD_POOL_SIZE", thread_pool_size)){
		cout << "THREAD_POOL_SIZE not defined in config,"
			<< "using default: " 
			<< thread_pool_size << endl;
	}

	if(!cfg.lookupValue("SERVICE_REQ_NUM", service_req_num)){
		cout << "SERVICE_REQ_NUM not defined in config,"
			<< "using default: " 
			<< service_req_num << endl;
	}

	if(!cfg.lookupValue("CLIENT_LISTEN_PORT", client_listen_port)){
		cout << "CLIENT_LISTEN_PORT not defined in config," 
			<< "using default: " 
			<< client_listen_port << endl;
	}
	
	if(!cfg.lookupValue("CLIENT_ADDR", client_addr)){
		cout << "CLIENT_ADDR not defined in config," 
			<< "using default: " 
			<< client_addr << endl;
	}

	if(!cfg.lookupValue("SERVERS_LIST_FILE", servers_list_file)){
		cout << "SERVERS_LIST_FILE not defined in config," 
			<< "using default: " 
			<< servers_list_file << endl;
	}
}

