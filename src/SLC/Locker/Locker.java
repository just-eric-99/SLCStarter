package SLC.Locker;

public class Locker {
    private String lockerId;
    private boolean isOpened;

    public Locker(String lockerId) {
        this.lockerId = lockerId;
        this.isOpened = false;
    }

    public String getLockerId() {
        return lockerId;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }
}
