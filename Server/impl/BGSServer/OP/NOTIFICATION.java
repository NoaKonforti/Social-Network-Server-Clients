package bgu.spl.net.impl.BGSServer.OP;

import java.util.List;

public class NOTIFICATION implements Message{
    private final short opCode = 9;
    private char type;
    private String postingUser;
    private String content;

    public NOTIFICATION(String type, String postingUser, String content) {
        this.postingUser = postingUser;
        this.content = content;
        if (type.equals("Public"))
            this.type = '1';
        else this.type = '0';
    }

    @Override
    public String getMessage() {
        return ("NOTIFICATION " + type + " " + postingUser + " " + content);
    }

    @Override
    public Message process() {
        return this;
    };

    @Override
    public void set(String source) {}

    public short getOpCode() {
        return opCode;
    }

    @Override
    public List<Message> getStat() {
        return null;
    }

    public char getType() {
        return type;
    }

    public String getPostingUser() {
        return postingUser;
    }

    public String getContent() {
        return content;
    }
}
