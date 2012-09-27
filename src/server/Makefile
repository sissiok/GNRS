all:
	g++ -W -Wall -std=c++98 -lpthread -DLINUX -DTIME_OPERATIONS -O2 -lrt -lconfig++ -I/usr/include/cppconn  -I/usr/local/BerkeleyDB.5.2/include -I. -L/usr/lib/mysql -L/usr/local/BerkeleyDB.5.2/lib -lmysqlclient_r -lmysqlcppconn -ldb_cxx -ldb_stl ./GNRS_daemon.cpp  ./daemon.cpp  ./announced_prefix_table.cpp ./Hash*.cpp ./linux_thread.cpp ./statistics.cpp ../common/*.cpp ../common/profiler/*.cpp ./LPM/*.cc -o gnrsd
	g++ -W -Wall -std=c++98 -lpthread -lrt -lconfig++ -I/usr/include/cppconn -I/usr/local/BerkeleyDB.5.2/include -I. -L/usr/lib/mysql -L/usr/local/BerkeleyDB.5.2/lib -lmysqlclient_r -lmysqlcppconn -ldb_cxx -ldb_stl ./LNRS_daemon.cpp ./daemon.cpp  ./HashMap.cpp ../common/*.cpp -o lnrsd



clean:
	rm -f *.o *~ gnrsd lnrsd
