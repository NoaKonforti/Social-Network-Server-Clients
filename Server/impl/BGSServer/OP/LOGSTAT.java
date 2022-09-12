package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LOGSTAT implements Message{

    private final short opCode = 7;
    private String name = null;

    public LOGSTAT (String name){this.name = name;}

    public LOGSTAT (){}

    @Override
    public Message process() {
        return this;
    }

    @Override
    public String getMessage() {return null;};

    public List<Message> getStat() {
        DataBaseManager db = DataBaseManager.getInstance();
        List<Message> ret = new ArrayList<>();

        if (!db.isRegistered(name) ) {
            ret.add(new ERROR(opCode));
            return ret;
        }

        User user = db.getUser(name);
        if(!user.isLoggedIn()){
            ret.add(new ERROR(opCode));
        }

        else {
            ConcurrentHashMap<Integer, User> loggedUsers = db.getLoggedUsers();
            for (Map.Entry mapElement : loggedUsers.entrySet()) {
                User u = ((User) mapElement.getValue());
                if (!user.isBlocked(u.getUsername())) {
                    String stat = u.getStat();
                    ret.add(new ACK(opCode, stat));
                }
            }
        }
        return ret;
    }

    @Override
    public void set(String source) {
        name = source;
    }

    public short getOpCode() {
        return opCode;
    }

}
