all: content_provider content_requestor

content_requestor:
	g++ -lpthread -lrt -lconfig++ -I. content_requestor.cpp NAS.cpp ../client/gnrsclient.cpp ../client/receiver.cpp ../common/*.cpp -o content_requestor

content_provider:
	g++ -lpthread -lrt -lconfig++ -I. content_provider.cpp NAS.cpp ../client/gnrsclient.cpp ../client/receiver.cpp ../common/*.cpp -o content_provider

clean:
	rm -f *.o *.so  *~ content_requestor content_provider
