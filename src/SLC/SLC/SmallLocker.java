package SLC.SLC;

import Common.LockerSize;

import java.io.Serializable;

public class SmallLocker implements Serializable {
    private final String lockerID;
    private int passcode;
    private String barcode;
    private long arriveTime;
    private LockerSize size;
    private boolean locked;
    private boolean isWorkNormal;

    public SmallLocker(String lockerID, LockerSize size) {
        this.lockerID = lockerID;
        this.size = size;
        passcode = -1;
        barcode = null;
        arriveTime = -1;
        locked = true;
    }

    public String getLockerID() {
        return lockerID;
    }

    public String getBarcode() {
        return barcode;
    }

    public boolean passcodeIsSame(int passcode) {
        return this.passcode == passcode;
    }

    public boolean isOccupied() {
        return barcode != null;
    }

    public long getArriveTime() {
        return arriveTime;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public LockerSize getSize() {
        return size;
    }

    public void addPackage(String barcode, int passcode) {
        this.barcode = barcode;
        this.passcode = passcode;
        arriveTime = System.currentTimeMillis();
    }

    public boolean isContainSamePackage(String barcode) {
        return this.barcode != null && this.barcode.equals(barcode);
    }

    public String pickUpPackage() {
        String tempBarcode = barcode;
        barcode = null;
        passcode = -1;
        arriveTime = -1;
        return tempBarcode;
    }

    public int getPayment(){
        // 24 hours = 86400000 milliseconds, default $15 a day
        return (int) (Math.ceil((System.currentTimeMillis()-arriveTime)/86400000.0) - 1) * 15;
    }

    public boolean isWorkNormal() {
        return isWorkNormal;
    }

    public void setWorkNormal(boolean workNormal) {
        isWorkNormal = workNormal;
    }

    @Override
    public String toString() {
        return "SmallLocker{" +
                "lockerID='" + lockerID + '\'' +
                ", passcode=" + passcode +
                ", barcode='" + barcode + '\'' +
                ", arriveTime=" + arriveTime +
                ", locked=" + locked +
                ", lockerSize='" + size + '\'' +
                '}';
    }
}

