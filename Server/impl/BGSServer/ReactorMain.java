package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int numberOfThreads = Integer.parseInt(args[1]);
        Server server = Server.reactor(numberOfThreads,port, () -> new BGSProtocol(), () -> new EncoderDecoderBGS());
        server.serve();
    }
}
