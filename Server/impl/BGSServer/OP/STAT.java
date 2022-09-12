package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.util.ArrayList;
import java.util.List;


public class STAT implements Message{
    private final short opCode = 8;
    private String name = null;
    private ArrayList<String> usersList ;

    public STAT (String name, ArrayList<String> usersList){
        this.name = name;
        this.usersList = usersList;
    }

    public STAT (ArrayList<String> usersList){
        this.usersList = usersList;
    }

    @Override
    public Message process() {
        return this;
    }

    public List<Message> getStat() {
        DataBaseManager db = DataBaseManager.getInstance();
        ArrayList<Message> ret = new ArrayList<>();

        if (!db.isRegistered(name)) {
            ret.add(new ERROR(opCode));
            return ret;
        }

        User user = db.getUser(name);
        if(!user.isLoggedIn()){
            ret.add(new ERROR(opCode));
            return ret;
        }

        else {
            for (String userName :  usersList){
                if (!user.isBlocked(userName)) {
                    User u = db.getUser(userName);
                    String stat = u.getStat();
                    ret.add(new ACK(opCode, stat));
                }
                else ret.add(new ERROR(opCode));
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

    @Override
    public String getMessage() {return null;};
}
