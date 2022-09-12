package bgu.spl.net.impl.BGSServer.OP;

import bgu.spl.net.impl.BGSServer.DataBaseManager;
import bgu.spl.net.impl.BGSServer.User;

import java.util.List;

public class BLOCK implements Message{
    private final short opCode = 12;
    private String name = null;
    private String user2block;

    public BLOCK (String name, String user2block){
        this.name = name;
        this.user2block = user2block;
    }

    public BLOCK (String user2block){
        this.user2block = user2block;
    }

    public Message process() {
        DataBaseManager db = DataBaseManager.getInstance();

        if (!db.isRegistered(name) || !db.isRegistered(user2block)) {
            return (new ERROR(opCode));
        }

        User user = db.getUser(name);
        if(!user.isLoggedIn()){
            return (new ERROR(opCode));
        }
        User userBlock = db.getUser(user2block);
        user.addBlocked(userBlock);
        userBlock.addBlocked(user);
        if (user.isFollowing(user2block)){
            user.unFollow(userBlock);
            userBlock.removeFollower(user);
        }
        if(user.isFollowedBy(user2block)){
            user.removeFollower(userBlock);
            userBlock.unFollow(user);
        }
        return (new ACK(opCode,""));
    }
    @Override
    public void set(String source) {
        name = source;
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
