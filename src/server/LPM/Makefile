
# Specify the compiler
CC = g++

CCOPTS = -g -Wall -fpermissive -lrt

# Make the source
all:	test

string.o : string.hh  string.cc
	$(CC) $(CCOPTS) -c string.cc

straccum.o : straccum.hh  straccum.cc
	$(CC) $(CCOPTS) -c straccum.cc

vector.o : vector.hh  vector.cc
	$(CC) $(CCOPTS) -c vector.cc

ipaddress.o : ipaddress.hh ipaddress.cc
	$(CC) $(CCOPTS) -c ipaddress.cc

iproutetable.o : iproutetable.hh iproutetable.cc
	$(CC) $(CCOPTS) -c iproutetable.cc

radixiplookup.o : radixiplookup.hh radixiplookup.cc
	$(CC) $(CCOPTS) -c radixiplookup.cc

#PrefixMatching.o : PrefixMatching.h PrefixMatching.cpp
#	$(CC) $(CCOPTS) -c PrefixMatching.cpp

test:  test.cpp string.o straccum.o vector.o ipaddress.o iproutetable.o radixiplookup.o
	$(CC) $(CCOPTS) string.o straccum.o vector.o ipaddress.o iproutetable.o radixiplookup.o test.cpp -o test

clean :
	rm -f vector.o iproutetable.o radixiplookup.o test
