OBJS = gnrsd_mon.o
CC = g++
DEBUG = -g
INCLUDEDIRS = 
CFLAGS = -Wall -c $(INCLUDEDIRS) $(DEBUG)
LFLAGS = -Wall $(DEBUG) -loml2

all: gnrsd_mon

gnrsd_mon: $(OBJS)
	$(CC) $(LFLAGS) $(OBJS) -o gnrsd_mon

gnrsd_mon.o: gnrsd_mon.cpp 
	$(CC) $(CFLAGS) gnrsd_mon.cpp

clean:
	rm -f *.o *~ gnrsd_mon
