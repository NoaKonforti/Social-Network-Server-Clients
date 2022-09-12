package bgu.spl.net.impl.BGSServer.OP;

import java.util.List;

public class ERROR implements Message {
    private final short opCode = 11;
    private short failedOp;
    public ERROR(short op) {
        failedOp = op;
    }

    public short getOpCode() {
        return opCode;
    }

    public short getFailedOp() {
        return failedOp;
    }

    @Override
    public void set(String source) {}

    @Override
    public String getMessage() {
        return ("ERROR " + failedOp);
    }

    @Override
    public Message process() {return this;};

    @Override
    public List<Message> getStat() {
        return null;
    }
}
