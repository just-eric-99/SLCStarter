package SLSvr.SLSvr;

import java.net.Socket;

public class Locker {
    private final String id;
    private Socket socket;

    public Locker(String id){
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean equals (String id) {
        return this.id.equals(id);
    }
}
