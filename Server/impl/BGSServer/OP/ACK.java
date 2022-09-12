package bgu.spl.net.impl.BGSServer.OP;

import java.util.List;

public class ACK implements Message {
    private final short opCode = 10;
    private short successOp;
    private String optional;

    public ACK(short op, String s) {
        successOp = op;
        optional = s;
    }

    public ACK(short op){
        successOp = op;
        optional = null;
    }

    public short getOpCode() {
        return opCode;
    }

    public short getSuccessOp() {
        return successOp;
    }

    public String getOptional() {
        return optional;
    }

    @Override
    public void set(String source) {}

    @Override
    public Message process() {return this;};

    @Override
    public String getMessage() {
        String msg = ("ACK " + successOp);;
        if (optional != null)
            msg = (msg + " " + optional);
        return msg;
    }

    @Override
    public List<Message> getStat() {
        return null;
    }
}
