package SLC.BarcodeReaderDriver.Emulator;

import AppKickstarter.misc.Msg;
import SLC.SLCStarter;
import SLC.BarcodeReaderDriver.BarcodeReaderDriver;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;


//======================================================================
// BarcodeReaderEmulator
public class BarcodeReaderEmulator extends BarcodeReaderDriver {
    private SLCStarter slcStarter;
    private String id;
    private Stage myStage;
    private BarcodeReaderEmulatorController barcodeReaderEmulatorController;

    //------------------------------------------------------------
    // BarcodeReaderEmulator
    public BarcodeReaderEmulator(String id, SLCStarter slcStarter) {
        super(id, slcStarter);
        this.slcStarter = slcStarter;
        this.id = id;
    } // BarcodeReaderEmulator


    //------------------------------------------------------------
    // start
    public void start() throws Exception {
        Parent root;
        myStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        String fxmlName = "BarcodeReaderEmulator.fxml";
        loader.setLocation(BarcodeReaderEmulator.class.getResource(fxmlName));
        root = loader.load();
        barcodeReaderEmulatorController = (BarcodeReaderEmulatorController) loader.getController();
        barcodeReaderEmulatorController.initialize(id, slcStarter, log, this);
        myStage.initStyle(StageStyle.DECORATED);
        myStage.setScene(new Scene(root, 350, 470));
        myStage.setTitle("Barcode Reader");
        myStage.setResizable(false);
        myStage.setOnCloseRequest((WindowEvent event) -> {
            slcStarter.stopApp();
            Platform.exit();
        });
        myStage.show();
    } // BarcodeReaderEmulator


    //------------------------------------------------------------
    // handleGoActive
    protected void handleGoActive() {
        super.handleGoActive();
        String respond = barcodeReaderEmulatorController.getActivationResp();
        handleRespond(respond);
    } // handleGoActive


    //------------------------------------------------------------
    // handleGoStandby
    protected void handleGoStandby() {
        super.handleGoStandby();
        String respond = barcodeReaderEmulatorController.getStandbyResp();
        handleRespond(respond);
    } // handleGoStandby

    private void handleRespond(String respond) {
        switch (respond) {
            case "Activated":
                barcodeReaderEmulatorController.appendTextArea("Barcode Reader Activated");
                barcodeReaderEmulatorController.goActive();
                slc.send(new Msg(id, mbox, Msg.Type.BR_IsActive, ""));
                break;

            case "Standby":
                barcodeReaderEmulatorController.appendTextArea("Barcode Reader Standby");
                barcodeReaderEmulatorController.goStandby();
                slc.send(new Msg(id, mbox, Msg.Type.BR_IsStandby, ""));
                break;

            case "Ignore":
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + barcodeReaderEmulatorController.getPollResp());
        }
    }

    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() {
        // super.handlePoll();

        switch (barcodeReaderEmulatorController.getPollResp()) {
            case "ACK":
                slc.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
                break;

            case "NAK":
                slc.send(new Msg(id, mbox, Msg.Type.PollNak, id + " is down!"));
                break;

            case "Ignore":
                // Just ignore.  do nothing!!
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + barcodeReaderEmulatorController.getPollResp());
        }
    } // handlePoll
} // BarcodeReaderEmulator
