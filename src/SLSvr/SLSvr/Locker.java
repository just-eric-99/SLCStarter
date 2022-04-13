package SLSvr.SLSvr;

import Common.LockerSize;

import java.net.Socket;

public class Locker {
    private final String id;
    private int largeLocker;
    private int mediumLocker;
    private int smallLocker;
    private Socket socket;

    public Locker(String id, int largeLocker, int mediumLocker, int smallLocker) {
        this.id = id;
        this.largeLocker = largeLocker;
        this.mediumLocker = mediumLocker;
        this.smallLocker = smallLocker;
    }

    public String getID() {
        return id;
    }

    public boolean reserveLocker(LockerSize size) {
        if (size == LockerSize.Large && largeLocker > 0)
            largeLocker--;
        else if (size == LockerSize.Medium && mediumLocker > 0)
            mediumLocker--;
        else if (size == LockerSize.Small && smallLocker > 0)
            smallLocker--;
        else
            return false;
        return true;
    }

    public void releaseLocker(LockerSize size) {
        if (size == LockerSize.Large)
            largeLocker++;
        else if (size == LockerSize.Medium)
            mediumLocker++;
        else if (size == LockerSize.Small)
            smallLocker++;
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
