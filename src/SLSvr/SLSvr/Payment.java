package SLSvr.SLSvr;

import java.io.Serializable;

public class Payment implements Serializable {
    private String octopusID;
    private double amount;

    public Payment(String octopusID, double amount) {
        this.octopusID = octopusID;
        this.amount = amount;
    }

    public String getOctopusID() {
        return octopusID;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Payment\n" +
                "octopusID: " + octopusID + "\n" +
                "Amount: " + amount;
    }
}
