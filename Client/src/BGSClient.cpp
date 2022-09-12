#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <thread>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

//Fields which will be used by both threads:
bool shouldStop;
ConnectionHandler* connectionHandler = nullptr;

void readFromKeyboard() {
    while (!shouldStop) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!(*connectionHandler).sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            shouldStop = true;
        }
        if (line == "LOGOUT")
            sleep(2);
        // connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
    }
}

void readFromSocket() {
    while (!shouldStop) {
        std::string answer;
        (*connectionHandler).getLine(answer);
        if (!answer.empty()) {
            std::cout << answer << std::endl << std::endl;
            if (answer == "ACK 3") { //Logout Acknowledgement
                std::cout << "Exiting...\n" << std::endl;
                shouldStop = true;

            }
        }
    }
}

int main(int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    connectionHandler = new ConnectionHandler(host, port);
    if (!(*connectionHandler).connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    shouldStop = false;

    std::thread keyboard (readFromKeyboard);
    std::thread socket (readFromSocket);

    socket.join();
    keyboard.join();

    delete connectionHandler;
}
