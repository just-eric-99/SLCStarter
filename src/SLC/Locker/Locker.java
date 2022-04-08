package SLC.Locker;

public class Locker {
    private String lockerId;
    private boolean isOccupied;
    private boolean isOpened;
    private int passcode;
    private long arrivalTime;

    public Locker(String lockerId, boolean isOccupied, int passcode) {
        this.lockerId = lockerId;
        this.isOccupied = isOccupied;
        this.isOpened = false;
        this.passcode = passcode;
    }

    public Locker(String lockerId, boolean isOccupied) {
        this.lockerId = lockerId;
        this.isOccupied = isOccupied;
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

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public int getPasscode() {
        return passcode;
    }

    public void setPasscode(int passcode) {
        this.passcode = passcode;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
