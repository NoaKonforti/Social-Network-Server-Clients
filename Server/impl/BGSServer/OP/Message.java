package bgu.spl.net.impl.BGSServer.OP;

import java.util.List;

public interface Message {
    public void set(String source);
    public short getOpCode();
    public Message process();
    public String getMessage();
    public List<Message> getStat();


    }
