package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class REGISTER implements Message {
    private final short opCode = 1;
    private String name;
    private String password;
    private Date birthDate;


    public REGISTER(String name, String password, Date date) {
        this.name = name;
        this.password = password;
        birthDate = date;
    }

    public Message process() {
        DataBaseManager db = DataBaseManager.getInstance();
        if (db.isRegistered(name)) {
            return (new ERROR(opCode));
        }
        else {
            db.register(new User(name, password, birthDate));
            return (new ACK(opCode));
        }
    }

    @Override
    public void set(String source) { }

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
