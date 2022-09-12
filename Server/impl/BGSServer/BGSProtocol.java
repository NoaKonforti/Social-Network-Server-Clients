package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.BGSServer.OP.ERROR;
import bgu.spl.net.impl.BGSServer.OP.REGISTER;
import bgu.spl.net.srv.bidi.BidiMessagingProtocol;
import bgu.spl.net.srv.bidi.Connections;
import bgu.spl.net.impl.BGSServer.OP.LOGIN;
import bgu.spl.net.impl.BGSServer.OP.Message;

import java.util.List;

public class BGSProtocol implements BidiMessagingProtocol {
    private Connections connections;
    private int connectionId;
    private boolean shouldTerminate = false;

    @Override
    public void start(int connectionId, Connections connections) {
        if (this.connections == null) {
            this.connectionId = connectionId;
            this.connections = connections;
        }
    }

    @Override
    public void process(Object message) {
        boolean shouldContinue = true;
        if (message.getClass() == LOGIN.class)
           ((LOGIN) message).setConnectionId(connectionId);
       else if (message.getClass() != REGISTER.class) {
           if (DataBaseManager.getInstance().getLoggedUser(connectionId) != null)
               ((Message) message).set(DataBaseManager.getInstance().getLoggedUser(connectionId).getUsername());
           else { //NO USER LOGGED IN FROM THIS CLIENT
               connections.send(connectionId, new ERROR(((Message)message).getOpCode()));
               shouldContinue = false;
           }
       }
       if (shouldContinue) {
           Message response = ((Message) message).process();
           if (response.getOpCode() != 2 && response.getOpCode() != 8 && response.getOpCode() != 7) {
               connections.send(connectionId, response);
               if (((Message) message).getOpCode() == 3) {
                   connections.disconnect(connectionId);
                   shouldTerminate = true;
               }
           } else {
               List<Message> messages = ((Message) message).getStat();
               for (Message m : messages) {
                   connections.send(connectionId, m);
               }
           }
       }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
