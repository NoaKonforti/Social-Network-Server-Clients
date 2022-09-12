package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.util.List;

public class FOLLOW implements Message {
    private final short opCode = 4;
    private String source = null;
    private boolean shouldFollow; //true = follow, false = unfollow;
    private String target;

    public FOLLOW(String source, boolean shouldUnFollow, String target) {
        this.source = source;
        shouldFollow = !shouldUnFollow;
        this.target = target;
    }

    public FOLLOW(boolean shouldFollow, String target) {
        this.shouldFollow = shouldFollow;
        this.target = target;
    }

    public Message process() {
        DataBaseManager db = DataBaseManager.getInstance();
        User sourceUser = db.getUser(source);
        if ((shouldFollow && sourceUser.isFollowing(target)) || (!shouldFollow && !sourceUser.isFollowing(target))) //checks if cannot be processed.
            return new ERROR(opCode);
        else {
            User targetUser = db.getUser(target);
            String msg = target;
            if (shouldFollow) {
                sourceUser.follow(targetUser);
                targetUser.addFollower(sourceUser);
            }
            else {
                sourceUser.unFollow(targetUser);
                targetUser.removeFollower(sourceUser);
            }
            return (new ACK(opCode,msg));
        }
    }
    @Override
    public void set(String source) {
        this.source = source;
    }

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
