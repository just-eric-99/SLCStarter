package SLC.SLC;

import Common.LockerSize;

import java.io.Serializable;

public class SmallLocker implements Serializable {
    private final String lockerID;
    private int passcode;
    private String barcode;
    private long arriveTime;
    private boolean locked;
    private LockerSize size;

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
        // fixme chg back MUST!!!!!!!!!!
//        return (int) (Math.ceil((System.currentTimeMillis()-arriveTime)/86400000.0) - 1) * 15;
        return (int) (Math.ceil((System.currentTimeMillis()-arriveTime)/60000.0) - 1) * 15;
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

