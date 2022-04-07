package SLC.SLC;

public class SmallLocker {
    private final String lockerID;
    private int passcode;
    private Package p;

    public SmallLocker(String lockerID) {
        this.lockerID = lockerID;
        passcode = -1;
        p = null;
    }

    public String getLockerID() {
        return lockerID;
    }

    public boolean passcodeIsSame(int passcode) {
        return this.passcode == passcode;
    }

    public boolean isOccupied() {
        return p != null;
    }

    public Package getPackage() {
        return p;
    }

    public Package addPackage(String barcode, int passcode) {
        if (p == null) {
            p = new Package(barcode);
            this.passcode = passcode;
            return p;
        }
        return null;
    }

    public Package isContainPackage(String barcode) {
        return p.getBarcode().equals(barcode)? p : null;
    }

    public Package emptyLocker() {
        Package temp = p;
        p = null;
        passcode = -1;
        return temp;
    }
}

