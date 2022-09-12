package bgu.spl.net.srv.bidi;
import bgu.spl.net.srv.ConnectionHandler;



public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);

    void connect(int connectionId, ConnectionHandler<T> handler);

    ConnectionHandler<T> getConnection(int connectionId);
}
