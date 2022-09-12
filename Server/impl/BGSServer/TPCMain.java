package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.BGSServer.OP.Message;
import bgu.spl.net.srv.Server;

public class TPCMain {

    public static void main(String[] args) {
        int port = Integer.decode(args[0]);
        Server<Message> server = Server.threadPerClient(port, () -> new BGSProtocol(), () -> new EncoderDecoderBGS());
        server.serve();
    }

}
