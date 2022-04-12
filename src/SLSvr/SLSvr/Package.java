package SLSvr.SLSvr;

import Common.LockerSize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Package implements Serializable {
    private final String barcode;
    private String lockerID;
    private LockerSize size;

    // Save in server for reference
    private int lockerPasscode;
    private Date arriveTime;
    private Date pickUpTime;
    private List<Payment> paymentList;

    public Package(String barcode, String lockerID, LockerSize size) {
        this.barcode = barcode;
        this.lockerID = lockerID;
        this.size = size;
        lockerPasscode = -1;
        arriveTime = null;
        pickUpTime = null;
        paymentList = new ArrayList<>();
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLockerID() {
        return lockerID;
    }

    public void setLockerID(String lockerID) {
        this.lockerID = lockerID;
    }

    public LockerSize getSize() {
        return size;
    }

    public void setSize(LockerSize size) {
        this.size = size;
    }

    public int getLockerPasscode() {
        return lockerPasscode;
    }

    public void setLockerPasscode(int lockerPasscode) {
        this.lockerPasscode = lockerPasscode;
    }

    public void setArriveTime(Date arriveTime) {
        this.arriveTime = arriveTime;
    }

    public void setPickUpTime(Date pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public void addPayment(Payment p) {
        paymentList.add(p);
    }

    public boolean equals(String id) {
        return this.barcode.equals(id);
    }

    @Override
    public String toString() {
        String str = "Package information\n" +
                "Barcode: " + barcode + "\n" +
                "Locker ID: " + lockerID + "\n" +
                "Locker Size: " + size + "\n" +
                "Passcode: " + (lockerPasscode == -1? "N/A" : lockerPasscode) + "\n" +
                "Arrive Time: " + (arriveTime == null? "N/A" : arriveTime) + "\n" +
                "Pick Up Time: " + (pickUpTime == null? "N/A" : pickUpTime) + "\n";
        for (Payment p : paymentList)
            str += p + "\n";
        return str;
    }
}
