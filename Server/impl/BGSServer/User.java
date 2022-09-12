package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.BGSServer.OP.NOTIFICATION;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.ConnectionsImpl;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class User {
    private String username;
    private String password;
    private Date birthDate;
    private ConcurrentHashMap<String, User> following = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, User> followers = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<NOTIFICATION> pendingNotifications = new ConcurrentLinkedDeque<>();
    private ConcurrentHashMap<String, User> blocked = new ConcurrentHashMap<>();
    private boolean loggedIn;
    private int posts = 0;
    private ConnectionHandler currentConnection= null;
    private int currentConnectionId;

    public User(String name, String pass, Date date) {
        username = name;
        password = pass;
        birthDate = date;
        loggedIn = false;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean checkPassword(String password) {
        return (this.password.equals(password));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void logIn(int connectionId) {
        loggedIn = true;
        currentConnectionId = connectionId;
        currentConnection = ConnectionsImpl.getInstance().getConnection(connectionId);
        pendingNotifications = new ConcurrentLinkedDeque<>();
    }

    public void logOut() {
        loggedIn = false;
        DataBaseManager.getInstance().removeLoggedUser(currentConnectionId);
        currentConnection = null;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isFollowedBy(String user) {
        return (followers.get(user) != null);
    }

    public boolean isFollowing(String user) {
        return (following.get(user) != null);
    }

    public void follow(User user) {
        following.put(user.getUsername(), user);
    }

    public void unFollow(User user) {
        following.remove(user.username,user);
    }

    public void addFollower(User user) {
        followers.put(user.getUsername(), user);
    }

    public void removeFollower(User user) {
        followers.remove(user.username,user);
    }

    public void addNotification(NOTIFICATION notification) {
        pendingNotifications.addLast(notification);
    }

    public ConcurrentLinkedDeque<NOTIFICATION> getPendingNotifications() {
        return pendingNotifications;
    }

    public String getStat(){
        LocalDate localDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int age = Period.between(localDate, LocalDate.now()).getYears();
        return (age + " " + posts + " " + followers.size() + " " + following.size());
    }

    public void post(String content) {
        NOTIFICATION notification = new NOTIFICATION("Public",username,content);
        DataBaseManager db = DataBaseManager.getInstance();
        ConcurrentLinkedDeque<User> users = new ConcurrentLinkedDeque<>();
        String copy = content;
        int next = copy.indexOf("@");
        while (next != -1) {
            copy = copy.substring(next+1);
            next = copy.indexOf(" ");
            String nextUser;
            if (next != -1)
                nextUser = copy.substring(0, next);
            else
                nextUser = copy;
            if (!nextUser.equals(username) && followers.get(nextUser) == null)
                users.addLast(db.getUser(nextUser));
            next = -1;
        }
        for (Map.Entry<String,User> e: followers.entrySet()) {
            User user = e.getValue();
            users.addLast(user);
        }
        while (!users.isEmpty()) {
            User user = users.removeFirst();
            if (!user.isLoggedIn())
                user.addNotification(notification);
            else user.sendNotification(notification);
        }
        posts++;
    }

    public void sendPM(String reciepient, String content) {
        NOTIFICATION notification = new NOTIFICATION("PM",username,content);
        User user = DataBaseManager.getInstance().getUser(reciepient);
        if (!user.isLoggedIn())
            user.addNotification(notification);
        else user.sendNotification(notification);
    }

    public void sendNotification(NOTIFICATION notification) { //TAKE CARE OF THIS
        if (currentConnection != null)
            currentConnection.send(notification);
        else pendingNotifications.addLast(notification);
    }

    public boolean isBlocked(String name) {
        return (blocked.get(name)!=null);
    }

    public void addBlocked (User user){
        blocked.put(user.getUsername(),user);
    }

    public void removeBlocked (User user){
        blocked.remove(user);
    }

}

