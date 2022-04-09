package SLSvr.SLSvr;

import Common.LockerSize;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Package {
    // TODO get from cfg file
    public static final int defaultDuration = 24;

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
}
