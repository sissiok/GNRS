OBJS = gbench.o Request.o Sender.o Receiver.o Config.o
CC = g++
DEBUG = -g
INCLUDEDIRS = 
CFLAGS = -Wall -c $(INCLUDEDIRS) $(DEBUG)
LFLAGS = -Wall $(DEBUG) -lboost_thread -lboost_system -lboost_filesystem -lboost_regex -lconfig++ 

all: bench

bench: $(OBJS)
	$(CC) $(LFLAGS) $(OBJS) -o gbench

gbench.o: gbench.cpp gbench.h Request.h ../common/Config.h
	$(CC) $(CFLAGS) gbench.cpp

Request.o: Request.cpp Request.h ../common/Messages.h
	$(CC) $(CFLAGS) Request.cpp

Sender.o: Sender.cpp Sender.h Request.h ../common/Config.h
	$(CC) $(CFLAGS) Sender.cpp

Receiver.o: Receiver.cpp Receiver.h Request.h ../common/Messages.h ../common/Config.h
	$(CC) $(CFLAGS) Receiver.cpp

Config.o: ../common/Config.cpp ../common/Config.h 
	$(CC) $(CFLAGS) ../common/Config.cpp

clean:
	rm -f *.o *~ gbench
