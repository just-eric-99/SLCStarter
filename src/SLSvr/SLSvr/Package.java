package SLSvr.SLSvr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Package {
    // TODO get from cfg file
    public static final int defaultDuration = 24;

    private final String barcode;
    private String lockerID;
    private double fee;
    private int duration;   // In hour

    // Save in server for reference
    private int lockerPasscode;
    private Date arriveTime;
    private Date pickUpTime;
    private List<Payment> paymentList;

    public Package(String barcode, String lockerID, double fee, int duration) {
        this.barcode = barcode;
        this.lockerID = lockerID;
        this.fee = fee;
        this.duration = duration;
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

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
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

    public int getDuration() {
        return duration;
    }

    public void addPayment(Payment p) {
        paymentList.add(p);
    }

    public boolean equals(String id) {
        return this.barcode.equals(id);
    }
}
