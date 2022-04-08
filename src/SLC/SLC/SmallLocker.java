package SLC.SLC;

public class SmallLocker {
    private final String lockerID;
    private int passcode;
    private String barcode;
    private long arriveTime;

    public SmallLocker(String lockerID) {
        this.lockerID = lockerID;
        passcode = -1;
        barcode = null;
        arriveTime = -1;
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

    public void addPackage(String barcode, int passcode) {
        if (this.barcode == null) {
            this.barcode = barcode;
            this.passcode = passcode;
            arriveTime = System.currentTimeMillis();
        }
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
}

