package SLC.OctopusCardReaderDriver.Emulator;

import AppKickstarter.misc.Msg;
import SLC.OctopusCardReaderDriver.OctopusCardReaderDriver;
import SLC.SLC.HWStatus;
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
    // handleCardFailed
    public void handleCardOK(String cardID, String amount) {
        super.handleCardOK(cardID, amount);
        slc.send(new Msg(id, mbox, Msg.Type.OCR_CardOK, cardID + "\t" + amount));
    } // handleCardFailed


    //------------------------------------------------------------
    // handleCardFailed
    public void handleCardFailed(String failMsg) {
        super.handleCardFailed(failMsg);
        slc.send(new Msg(id, mbox, Msg.Type.OCR_CardFailed, failMsg));
    } // handleCardFailed


    //------------------------------------------------------------
    // handleTransactionRequest
    protected void handleTransactionRequest(double amount) {
        super.handleTransactionRequest(amount);
        String respond = octopusCardReaderEmulatorController.getActivationResp();
        handleRespond(respond, amount + "");
    } // handleTransactionRequest


    //------------------------------------------------------------
    // handleGoStandby
    protected void handleGoStandby() {
        super.handleGoStandby();
        String respond = octopusCardReaderEmulatorController.getStandbyResp();
        handleRespond(respond, "");
    } // handleGoStandby


    //------------------------------------------------------------
    // handleRespond
    private void handleRespond(String respond, String amount) {
        switch (respond) {
            case "Activated":
                octopusCardReaderEmulatorController.appendTextArea("Octopus Card Reader Activated");
                octopusCardReaderEmulatorController.setOctopusCardRequestAmountField(amount);
                octopusCardReaderEmulatorController.setOctopusCardRequestAmountField(amount);
                octopusCardReaderEmulatorController.goActive();
                slc.send(new Msg(id, mbox, Msg.Type.OCR_WaitingTransaction, ""));
                ocrInnerStatus = HWStatus.Active;
                break;

            case "Standby":
                octopusCardReaderEmulatorController.appendTextArea("Octopus Card Reader Standby");
                octopusCardReaderEmulatorController.goStandby();
                slc.send(new Msg(id, mbox, Msg.Type.OCR_IsStandby, ""));
                ocrInnerStatus = HWStatus.Standby;
                break;

            case "Ignore":
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + octopusCardReaderEmulatorController.getPollResp());
        }
    } // handleRespond


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
