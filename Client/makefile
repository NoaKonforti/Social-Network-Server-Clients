CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread

all: BGSClient
	g++ -o bin/BGSClient bin/connectionHandler.o bin/BGSClient.o $(LDFLAGS)

BGSClient: bin/connectionHandler.o bin/BGSClient.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/BGSClient.o: src/BGSClient.cpp
	g++ $(CFLAGS) -o bin/BGSClient.o src/BGSClient.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
