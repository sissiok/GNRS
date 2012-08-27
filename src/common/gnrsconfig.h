#ifndef GNRSCONFIG_H
#define GNRSCONFIG_H

#include <iostream>
#include <string>
#include <cstring>
#include <stdio.h>
#include <stdlib.h>

using namespace std;

#define DEFAULT_DATABASE_HOST "localhost"
#define DEFAULT_DATABASE_PORT 3306

#define DEFAULT_SERVER_ADDR "127.0.0.1"

#define DEFAULT_DAEMON_LISTEN_PORT 5000
#define DEFAULT_CLIENT_LISTEN_PORT 9000

#define DEFAULT_SERVERS_LIST_FILE "./servers.lst"

#define DEFAULT_SERVER_HASH 0

#define DEFAULT_STL_FUNC 0

/**
 * Define defaults for global config variables and assign
 * values readin from configuration files
 */

class GNRSConfig{

public:
  //DB configuration
  static string db_host;
  static int    db_port;
  static string db_name;
  static string db_user; 
  static string db_passwd;
  static string db_mappings_table;

  /* daemon configuration */
  static int    daemon_listen_port;
  static string server_addr; //self identity/addr
  static int hash_func;
  static int stl_func;

  /* gnrs client configuration */
  static int client_listen_port;
  static string  client_addr;

  /* file listing of addresses of gnrs servers */
  static string servers_list_file;

  static void init_defaults(){

	  db_host.assign(DEFAULT_DATABASE_HOST);
	  db_port = DEFAULT_DATABASE_PORT;
	  
	  daemon_listen_port = DEFAULT_DAEMON_LISTEN_PORT;
	  client_listen_port = DEFAULT_CLIENT_LISTEN_PORT;

	  server_addr = DEFAULT_SERVER_ADDR;

	  servers_list_file = DEFAULT_SERVERS_LIST_FILE;

	  hash_func=DEFAULT_SERVER_HASH;

	  stl_func=DEFAULT_STL_FUNC;
  }

  static void read_from_file(const char*  filename);
};


#endif //GNRSCONFIG_H
