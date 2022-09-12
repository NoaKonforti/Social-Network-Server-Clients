package bgu.spl.net.srv;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.bidi.Connections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private static class ConnectionsHolder { //Singleton Holder
        private static ConnectionsImpl instance = new ConnectionsImpl();
    }

    private ConcurrentHashMap<Integer, ConnectionHandler> handlers = new ConcurrentHashMap<>();


    public static Connections getInstance() {
        return ConnectionsHolder.instance;
    }


    @Override
    public boolean send(int connectionId, T msg) {
        if (handlers.get(connectionId)==null)
            return false;
        handlers.get(connectionId).send(msg);
        return true;
    }

    @Override
    public void broadcast(T msg) {
        for (Map.Entry e: handlers.entrySet()) {
            ((ConnectionHandler)e.getValue()).send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        handlers.remove(connectionId);
    }

    @Override
    public ConnectionHandler getConnection(int connectionId) {
        return handlers.get(connectionId);
    }

    public void connect(int connectionId, ConnectionHandler handler) {
        handlers.put(connectionId, handler);
    }
}
