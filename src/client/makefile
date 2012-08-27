all: gnrs-client gbench

gnrs-client:
	g++ -lpthread -lrt -lconfig++ -I. main.cpp gnrsclient.cpp receiver.cpp ../common/*.cpp -o gnrs-client

gbench:
	g++ -lpthread -DLINUX -DTIME_OPERATIONS -O2 -lrt -lconfig++ -I. gbench.cpp gnrsclient.cpp receiver.cpp ../common/*.cpp ../common/profiler/*.cpp -o gbench

clean:
	rm -f *.o *.so  *~ gnrs-client gbench
