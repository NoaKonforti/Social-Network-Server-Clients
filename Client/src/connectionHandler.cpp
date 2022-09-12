#include <connectionHandler.h>
#include <iomanip>
#include <ctime>
#include <sstream>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
 
ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}
 
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    std::cerr << "Connected. Please Register or Log-In" << "\n";
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
    //    std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, ';');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, ';');
}

bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    std::string input = frame;
    std::size_t next = input.find(" ");
    std::string op = input.substr(0, next);
    input = input.substr(next+1,input.back());
    if (op == "REGISTER") {
        char opCode[2];
        shortToBytes(1, opCode);
        next = input.find(" ");
        std::string username = input.substr(0, next);
        input = input.substr(next+1,input.back());
        next = input.find(" ");
        std::string password = input.substr(0, next);
        std::string birthday = (input.substr(next+1,input.back()));
        int size = 3 + username.size()+1 + password.size()+1 + birthday.size()+1;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result=sendBytes(sizeArr,4);
        if(!result) return false;
        result=sendBytes(opCode,2);
        if(!result) return false;
        result = sendBytes(username.c_str(),username.size()+1);
        if(!result) return false;
        result = sendBytes(password.c_str(),password.size()+1);
        if(!result) return false;
        result = sendBytes(birthday.c_str(),birthday.size()+1);
        if(!result) return false;
    }
    else if (op == "LOGIN") {
        char opCode[2];
        shortToBytes(2,opCode);
        next = input.find(" ");
        std::string username = input.substr(0, next);
        input = input.substr(next+1,input.back());
        next = input.find(" ");
        std::string password = input.substr(0, next);
        std::string captcha = input.substr(next+1, input.back());
        char captchaC[1] = {captcha.at(0)};
        int size = 3 + username.size()+1 + password.size()+1 + 1;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result=sendBytes(sizeArr,4);
        if(!result) return false;
        result = sendBytes(opCode,2);
        if(!result) return false;
        result = sendBytes(username.c_str(),username.size()+1);
        if(!result) return false;
        result = sendBytes(password.c_str(),password.size()+1);
        if(!result) return false;
        result = sendBytes(captchaC,1);
        if(!result) return false;
    }
    else if (op == "LOGOUT") {
        char opCode[2];
        shortToBytes(3, opCode);
        int size = 3;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result=sendBytes(sizeArr,4);
        if(!result) return false;
        result=sendBytes(opCode,2);
        if(!result) return false;
    }
    else if (op == "FOLLOW") {
        char opCode[2];
        shortToBytes(4, opCode);
        int size = 3 + input.size()+1;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result = sendBytes(sizeArr,4);
        if(!result) return false;
        result = sendBytes(opCode,2);
        if(!result) return false;
        result = sendBytes(input.c_str(),input.size()+1);
        if(!result) return false;
    }
    else if (op == "POST") {
        char opCode[2];
        shortToBytes(5, opCode);
        int size = 3 + input.size()+1;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result = sendBytes(sizeArr,4);
        if(!result) return false;
        result = sendBytes(opCode,2);
        if(!result) return false;
        result = sendBytes(input.c_str(),input.size()+1);
        if(!result) return false;
    }
    else if (op == "PM") {
        char opCode[2];
        shortToBytes(6,opCode);
        next = input.find(" ");
        std::string username = input.substr(0, next);
        input = input.substr(next+1,input.back());
        //std::string content = input.substr(0, next);
        auto t = std::time(nullptr); //Save local time
        auto tm = *std::localtime(&t);
        std::ostringstream oss;
        oss << std::put_time(&tm, "%d-%m-%Y %H-%M-%S");
        std::string time = oss.str();
        int size = 3 + username.size()+1 + input.size()+1 + time.size()+1;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result = sendBytes(sizeArr,4);
        result = sendBytes(opCode,2);
        if(!result) return false;
        result = sendBytes(username.c_str(),username.size()+1);
        if(!result) return false;
        result = sendBytes(input.c_str(),input.size()+1);
        if(!result) return false;
        result = sendBytes(time.c_str(),time.size()+1);
        if(!result) return false;
    }
    else if (op == "LOGSTAT") {
        char opCode[2];
        shortToBytes(7, opCode);
        int size = 3;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result=sendBytes(sizeArr,4);
        result = sendBytes(opCode,2);
        if(!result) return false;
    }
    else if (op == "STAT") {
        char opCode[2];
        shortToBytes(8, opCode);
        int size = 3 + input.size()+1;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result = sendBytes(sizeArr,4);
        if(!result) return false;
        result = sendBytes(opCode,2);
        if(!result) return false;
        result = sendBytes(input.c_str(),input.size()+1);
        if(!result) return false;
    }
    else if (op == "BLOCK") {
        char opCode[2];
        shortToBytes(12, opCode);
        int size = 3 + input.size()+1;
        char sizeArr[4];
        sizeToBytes(size, sizeArr);
        bool result = sendBytes(sizeArr,4);
        if(!result) return false;
        result = sendBytes(opCode,2);
        if(!result) return false;
        result = sendBytes(input.c_str(),input.size()+1);
        if(!result) return false;
    }
    return sendBytes(&delimiter,1);
}

bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char op[2];
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        if(!getBytes(&ch, 1)){
            return false;
        }
        op[0] = ch;
        if(!getBytes(&ch, 1)){
            return false;
        }
        op[1] = ch;
        short opCode = bytesToShort(op);
        if (opCode == 9){//NOTIFICATION
            if(!getBytes(&ch, 1)){
                return false;
            }
            short message = bytesToShort(&ch);
            std::string  s;
            if(ch == '0'){
                s = "PM ";
            }
            if(ch == '1'){
                s = "Public ";
            }

            frame =  "NOTIFICATION "+ s;
            char c;
            int zeroCount = 0;
            while (c != delimiter) {
                if (!getBytes(&c, 1))
                    return false;
                if (c ==  '\0'){
                    c = ' ';
                    zeroCount++;
                }
                if (zeroCount < 2)
                    frame.append(1, c);
            }
        }
        if (opCode ==10){//ACK
            char otherOp[2];
            frame =  "ACK ";
            if(!getBytes(&ch, 1))
                return false;
            otherOp[0] = ch;
            if(!getBytes(&ch, 1))
                return false;
            otherOp[1] = ch;
            frame = frame + std::to_string((int)bytesToShort(otherOp));
            std::string optional;
            char c;
            if (!getBytes(&c, 1))
                return false;
            if (c != delimiter) {
                frame = frame + " ";
                frame.append(1, c);
            }
            while (c != delimiter) {
                if (!getBytes(&c, 1))
                    return false;
                if (c != delimiter && c != '\0')
                    frame.append(1, c);
            }
        }

        if (opCode == 11){//ERROR
            char otherOp[2];
            frame =  "ACK ";
            if(!getBytes(&ch, 1))
                return false;
            otherOp[0] = ch;
            if(!getBytes(&ch, 1))
                return false;
            otherOp[1] = ch;
            short message = bytesToShort(otherOp);
            std::string  s =  std::to_string(message);
            frame = "ERROR "+ s;
            while (ch != delimiter)
                if(!getBytes(&ch,1))
                    return false;
        }

    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

short ConnectionHandler::bytesToShort(char* bytesArr){
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}
void ConnectionHandler::shortToBytes(short num, char* bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

void ConnectionHandler::sizeToBytes(int size, char* bytes) {
    bytes[0] = (size >> 24);
    bytes[1] = (size >> 16);
    bytes[2] = (size >> 8);
    bytes[3] = (size);
}
