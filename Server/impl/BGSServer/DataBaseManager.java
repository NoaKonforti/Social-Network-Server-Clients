package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.BGSServer.OP.Message;
import bgu.spl.net.impl.BGSServer.Objects.Filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DataBaseManager {

    private static class DataBaseHolder { //Singleton Holder
        private static DataBaseManager instance = new DataBaseManager();
    }

    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, User> loggedUsers = new ConcurrentHashMap<>();
    private Filter filter = new Filter();
    private ConcurrentLinkedDeque<Message> messages = new ConcurrentLinkedDeque<>();


    private DataBaseManager() {};

    public static DataBaseManager getInstance() {
        return DataBaseHolder.instance;
    }

    public boolean isRegistered(String name) {
        boolean ret = false;
        if (users.get(name)!= null)
            ret = true;
        return ret;
    }

    public void register(User user) {
        users.put(user.getUsername(),user);
    }

    public User getUser(String name){
        return users.get(name);
    }

    public void addLoggedUser (Integer connectionId, User user){
        loggedUsers.put(connectionId, user);
    }

    public User getLoggedUser(Integer connectionId) {
        return loggedUsers.get(connectionId);
    }

    public ConcurrentHashMap<Integer, User> getLoggedUsers() {
        return loggedUsers;
    }

    public void removeLoggedUser (Integer id){
        loggedUsers.remove(id);
    }

    public Filter getFilter() {
        return filter;
    }

    public String filter(String content) {
        return filter.filter(content);
    }

    public void saveMessage(Message message) {
        messages.add(message);
    }
}
