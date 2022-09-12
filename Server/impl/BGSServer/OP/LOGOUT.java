package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.util.List;

public class LOGOUT implements Message{
    private final short opCode = 3;
    private String name = null;


    public LOGOUT(String name){this.name = name;}
    public LOGOUT(){}

    public Message process() {
        DataBaseManager db = DataBaseManager.getInstance();
        if (!db.isRegistered(name)) {
            return (new ERROR(opCode));
        }

        User user = db.getUser(name);
        if(!user.isLoggedIn()){
            return (new ERROR(opCode));
        }

        else {
            user.logOut();
            return (new ACK(opCode));
            //// the client should terminate after this action !
        }
    }

    @Override
    public void set(String source) {
        name = source;
    }

    @Override

    public short getOpCode() {
        return opCode;
    }

    @Override
    public List<Message> getStat() {
        return null;
    }


    @Override
    public String getMessage() {return null;};
}
