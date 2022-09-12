package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class PM implements Message {
    private final short opCode = 6;
    private String sender = null;
    private String reciepient;
    private String content;
    private String date;

    public PM(String src, String rcp, String cnt, String time) {
        sender = src;
        reciepient = rcp;
        content = cnt;
        date = time;
    }

    public PM(String rcp, String cnt, String time) {
        reciepient = rcp;
        content = cnt;
        date = time;
    }

    public Message process() {
        DataBaseManager db = DataBaseManager.getInstance();
        if ((db.getUser(sender) == null) || !db.getUser(sender).isLoggedIn() || db.getUser(reciepient) == null || !db.getUser(sender).isFollowing(reciepient))
            return (new ERROR(opCode));
        else  {
            content = db.filter(content);
            db.getUser(sender).sendPM(reciepient, (content + " " + date));
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

    public String getReciepient() {
        return reciepient;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public void set(String source) {
        this.sender = source;
    }

    @Override
    public List<Message> getStat() {
        return null;
    }
}
