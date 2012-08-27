CC=g++
CFLAGS=-c -Wall -fPIC -DLINUX -I /usr/lib/jvm/java-6-openjdk/include  -I ../client/
LDFLAGS=-shared -Wl,-soname,libgnrs-jni.so -lconfig++
SOURCES=GNRSJNIInterfaceImpl.cpp ../client/gnrsclient.cpp ../client/address.cpp ../client/incomingconnection.cpp ../client/packet.cpp ../client/socketconnection.cpp ../client/packetheader.cpp ../client/transmissiontimer.cpp ../client/gnrsconfig.cpp ../client/outgoingconnection.cpp ../client/receiver.cpp
OBJECTS=$(SOURCES:.cpp=.o)
SHAREDLIB=libgnrs-jni.so

all: $(SHAREDLIB)
	
$(SHAREDLIB): GNRSJNIInterface.class GNRSJNIInterface.h $(OBJECTS) 
	$(CC) $(LDFLAGS) *.o -o $@

GNRSJNIInterface.h: GNRSJNIInterface.java
	javah -jni GNRSJNIInterface

GNRSJNIInterface.class: GNRSJNIInterface.java
	javac GNRSJNIInterface.java

.cpp.o:
	$(CC) $(CFLAGS) $<

clean:
	rm -rf *.o *.class $(SHAREDLIB)
