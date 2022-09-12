package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LOGIN implements Message{
    private final short opCode = 2;
    private String name;
    private String password;
    private boolean captcha;
    private int connectionId;

    public LOGIN(String name, String password, boolean captcha){
        this.name = name;
        this.password = password;
        this.captcha = captcha;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public Message process() {
        return this;
    }

    @Override
    public List<Message> getStat() {
        List<Message> ret = new ArrayList<>();
        DataBaseManager db = DataBaseManager.getInstance();
        if (!db.isRegistered(name) || !captcha) {
            ret.add(new ERROR(opCode));
            return ret;
        }

        User user = db.getUser(name);
        if(!user.checkPassword(password) || user.isLoggedIn()){
            ret.add(new ERROR(opCode));
            return ret;
        }

        else {
            ConcurrentLinkedDeque<NOTIFICATION> notifications = user.getPendingNotifications();
            user.logIn(connectionId);
            db.addLoggedUser(connectionId,user);
            ret.add(new ACK(opCode));
            for (NOTIFICATION n: notifications) {
                ret.add(n);
            }
            return ret;
        }
    }
    @Override
    public void set(String source) {}

    public short getOpCode() {
        return opCode;
    }

    @Override
    public String getMessage() {return null;};
}
