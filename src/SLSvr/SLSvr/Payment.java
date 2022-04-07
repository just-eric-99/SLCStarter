package SLSvr.SLSvr;

public class Payment {
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
}
