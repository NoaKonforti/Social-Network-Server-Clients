package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.util.List;

public class POST implements Message{
    private final short opCode = 5;
    private String postingUser = null;
    private String content;

    public POST(String postingUser,String content) {
        this.content = content;
        this.postingUser = postingUser;
    }

    public POST(String content) {
        this.content = content;
    }

    public Message process() { //can return NULL!
        DataBaseManager db = DataBaseManager.getInstance();
        if (db.getUser(postingUser)== null)
            return (new ERROR(opCode));
        else {
            User user = db.getUser(postingUser);
            if (!user.isLoggedIn())
                return (new ERROR(opCode));
            else user.post(content);
            db.saveMessage(this);
            return (new ACK(opCode));
        }
    }


    @Override
    public String getMessage() {return null;};

    public short getOpCode() {
        return opCode;
    }

    public String getContent() {
        return content;
    }

    public String getPostingUser() {
        return postingUser;
    }

    @Override
    public void set(String source) {
        this.postingUser = source;
    }

    @Override
    public List<Message> getStat() {
        return null;
    }
}
