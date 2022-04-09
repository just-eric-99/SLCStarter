package SLC.OctopusCardReaderDriver.Emulator;

import AppKickstarter.misc.Msg;
import SLC.OctopusCardReaderDriver.OctopusCardReaderDriver;
import SLC.SLCStarter;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class OctopusCardReaderEmulator extends OctopusCardReaderDriver {
    private SLCStarter slcStarter;
    private String id;
    private Stage myStage;
    private OctopusCardReaderEmulatorController octopusCardReaderEmulatorController;
    public static volatile boolean isCardChosen = false;

    public OctopusCardReaderEmulator(String id, SLCStarter slcStarter) {
        super(id, slcStarter);
        this.slcStarter = slcStarter;
        this.id = id;
    }


    //------------------------------------------------------------
    // start
    public void start() throws Exception {
        Parent root;
        myStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        String fxmlName = "OctopusCardReaderEmulator.fxml";
        loader.setLocation(OctopusCardReaderEmulator.class.getResource(fxmlName));
        root = loader.load();
        octopusCardReaderEmulatorController = (OctopusCardReaderEmulatorController) loader.getController();
        octopusCardReaderEmulatorController.initialize(id, slcStarter, log, this);
        myStage.initStyle(StageStyle.DECORATED);
        myStage.setScene(new Scene(root, 453, 539));
        myStage.setTitle("Octopus Card Reader");
        myStage.setResizable(false);
        myStage.setOnCloseRequest((WindowEvent event) -> {
            slcStarter.stopApp();
            Platform.exit();
        });
        myStage.show();
    } // OctopusCardReaderEmulator

    //------------------------------------------------------------
    // handleGoActive
    protected void handleGoActive() {
        // fixme
        super.handleGoActive();
        slc.send(new Msg(id, mbox, Msg.Type.OCR_GoActive, id + " go active"));
        octopusCardReaderEmulatorController.appendTextArea("Octopus Card Reader Activated");
    } // handleGoActive


    //------------------------------------------------------------
    // handleGoStandby
    protected void handleGoStandby() {
        // fixme
        super.handleGoStandby();
        slc.send(new Msg(id, mbox, Msg.Type.OCR_GoActive, id + " go standby"));
        octopusCardReaderEmulatorController.appendTextArea("Octopus Card Reader Standby");
    } // handleGoStandby

    //------------------------------------------------------------
    // handleGoStandby
    protected void handleCardFailed() {
        super.handleCardFailed();
        slc.send(new Msg(id, mbox, Msg.Type.OCR_CardFailed, id + " Card Failed"));
        octopusCardReaderEmulatorController.appendTextArea("Octopus Card Failed");
    } // handleGoStandby

    //------------------------------------------------------------
    // handleTransactionRequest
    protected void handleTransactionRequest(int amount) {
        //wait for a card to be chosen
        slc.send(new Msg(id, mbox, Msg.Type.OCR_WaitingTransaction, amount+""));
        while(!isCardChosen);

        super.handleTransactionRequest(amount);
        octopusCardReaderEmulatorController.setOctopusCardRequestAmountField(String.valueOf(amount));

        float cardAmount = octopusCardReaderEmulatorController.getCardAmount();

        float remaining = cardAmount - (float)amount;

        if(cardAmount<=0 || remaining<=-50){
            slc.send(new Msg(id, mbox, Msg.Type.OCR_CardFailed, "Card doesn't have enough money!"));
            octopusCardReaderEmulatorController.appendTextArea("Octopus Card doesn't have enough money");
            isCardChosen = false;
            return;
        }

        slc.send(new Msg(id, mbox, Msg.Type.OCR_CardOK, id + " Card OK"));
        octopusCardReaderEmulatorController.setCardAmount(remaining);
        octopusCardReaderEmulatorController.appendTextArea("Octopus Card OK, remaining amount: "+remaining);
        isCardChosen = false;
    } // handleGoStandby


    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() {
        // super.handlePoll();

        switch (octopusCardReaderEmulatorController.getPollResp()) {
            case "ACK":
                slc.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
                break;

            case "NAK":
                slc.send(new Msg(id, mbox, Msg.Type.PollNak, id + " is down!"));
                break;

            case "Ignore":
                // Just ignore.  do nothing!!
                break;
        }
    } // handlePoll
}
