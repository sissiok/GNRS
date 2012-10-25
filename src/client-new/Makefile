all: gbench

gbench:
	g++ -lpthread -DLINUX -DTIME_OPERATIONS -O2 -lrt -lconfig++ -I. gbench.cpp gnrsclient.cpp receiver.cpp statistics.cpp ../common/*.cpp ../common/profiler/*.cpp -o gbench

sampling:
	g++ -lpthread -DSAMPLING -DLINUX -DTIME_OPERATIONS -O2 -lrt -lconfig++ -I. gbench.cpp gnrsclient.cpp receiver.cpp statistics.cpp ../common/*.cpp ../common/profiler/*.cpp -o gbench

clean:
	rm -f *.o *.so  *~ gbench
