package SLC.TouchDisplayHandler.Emulator;

import SLC.SLCStarter;
import SLC.TouchDisplayHandler.TouchDisplayHandler;
import AppKickstarter.misc.Msg;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;


//======================================================================
// TouchDisplayEmulator
public class TouchDisplayEmulator extends TouchDisplayHandler {
    private final int WIDTH = 680;
    private final int HEIGHT = 570;
    private SLCStarter slcStarter;
    private String id;
    private Stage myStage;
    private TouchDisplayEmulatorController touchDisplayEmulatorController;

    //------------------------------------------------------------
    // TouchDisplayEmulator
    public TouchDisplayEmulator(String id, SLCStarter slcStarter) throws Exception {
	super(id, slcStarter);
	this.slcStarter = slcStarter;
	this.id = id;
    } // TouchDisplayEmulator


    //------------------------------------------------------------
    // start
    public void start() throws Exception {
	// Parent root;
	myStage = new Stage();
	reloadStage("TouchDisplayEmulator.fxml");
	myStage.setTitle("Touch Display");
	myStage.setResizable(false);
	myStage.setOnCloseRequest((WindowEvent event) -> {
	    slcStarter.stopApp();
	    Platform.exit();
	});
	myStage.show();
    } // TouchDisplayEmulator


    //------------------------------------------------------------
    // reloadStage
    private void reloadStage(String fxmlFName) {
        TouchDisplayEmulator touchDisplayEmulator = this;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info(id + ": loading fxml: " + fxmlFName);

                    // get the latest pollResp string, default to "ACK"
                    String pollResp = "ACK";
                    if (touchDisplayEmulatorController != null) {
                        pollResp = touchDisplayEmulatorController.getPollResp();
                    }

                    Parent root;
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(TouchDisplayEmulator.class.getResource(fxmlFName));
                    root = loader.load();

                    touchDisplayEmulatorController = (TouchDisplayEmulatorController) loader.getController();
                    touchDisplayEmulatorController.initialize(id, slcStarter, log, touchDisplayEmulator, pollResp);
                    myStage.setScene(new Scene(root, WIDTH, HEIGHT));
                } catch (Exception e) {
                    log.severe(id + ": failed to load " + fxmlFName);
                    e.printStackTrace();
                }
            }
        });
    } // reloadStage


    //------------------------------------------------------------
    // handleUpdateDisplay
    protected void handleUpdateDisplay(Msg msg) {
        log.info(id + ": update display -- " + msg.getDetails());
        System.out.println(msg);
        String[] tokens = msg.getDetails().split(",");
        String page = tokens[0].trim();
        if (tokens.length > 1)
            TouchDisplayEmulatorController.setShowMsg(tokens[1].trim());
        else
            TouchDisplayEmulatorController.setShowMsg("");

        switch (page) {
            case "WelcomePage":
                reloadStage("TouchDisplayEmulator.fxml");
                break;

            case "MainMenu":
                reloadStage("TouchDisplayMainMenu.fxml");
                break;

            case "Confirmation":
                reloadStage("TouchDisplayConfirmation.fxml");
                break;

            case "EnterPasscode":
                reloadStage("TouchDisplayEnterPasscode.fxml");
                break;

            case "Payment":
                reloadStage("TouchDisplayPayment.fxml");
                break;

            case "PaymentSucceeded":
                reloadStage("TouchDisplayPaymentSucceeded.fxml");
                break;

            case "PaymentFailed":
                reloadStage("TouchDisplayPaymentFailed.fxml");
                break;

            case "ScanBarcode":
                reloadStage("TouchDisplayScanBarcode.fxml");
                break;

            case "ShowLocker":
//                String lockerId = tokens[1].trim();
//                TouchDisplayEmulatorController.setDisplayLocker(lockerId);
                reloadStage("TouchDisplayShowLocker.fxml");
                break;

            case "LockerNotClose":
                reloadStage("TouchDisplayLockerNotClose.fxml");
                break;

            case "ServerDown":
                reloadStage("TouchDisplayServerDown.fxml");
                break;

            default:
                log.severe(id + ": update display with unknown display type -- " + msg.getDetails());
                break;
        }
    } // handleUpdateDisplay


    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() {
        // super.handlePoll();

        switch (touchDisplayEmulatorController.getPollResp()) {
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

    // fixme
    protected void handleSendBarcode(Msg msg) {
        System.out.println("handleSendBarcode" + msg.getDetails());
//        touchDisplayEmulatorController.scannedBarcodeTextArea.appendText(msg.getDetails());
    }

    // fixme
    @Override
    protected void handleSendPasscode(Msg msg) {
//        switch (msg.getDetails()) {
//            case "true":
//                touchDisplayEmulatorController.getMbox().send(new Msg(id, touchDisplayEmulatorController.getMbox(), Msg.Type.TD_UpdateDisplay, "Payment"));
//                break;
//        }

        super.handleSendPasscode(msg);
    }

    // send message
    protected void mouseClick(String screenType, String receivedMsg) {
        switch (screenType) {
            case "Enter Passcode":
                slc.send(new Msg(id, mbox, Msg.Type.TD_SendPasscode, receivedMsg));
                break;

        }
    }

    protected void handleDisplayBarcode(Msg msg) {
        log.info(id + ": Display barcode after scanning, barcode: " + msg.getDetails());
        System.out.println(msg.getDetails());
    }

    @Override
    protected void handleVerifyPasscode(Msg msg) {
        System.out.println(msg.getDetails());
        String[] tokens = msg.getDetails().split(",");
        String validity = tokens[0].trim();
        switch (validity) {
            case "valid":
                String hasPayment = tokens[1].trim();
                switch (hasPayment) {
                    case "yes":
                        String amount = tokens[2].trim();
                        // display payment page
                        mbox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Payment," + amount));
                        break;

                    case "no":
                        String lockerId = tokens[2].trim();
                        mbox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "ShowLocker," + lockerId));
//                        slc.send(new Msg(id, mbox, Msg.Type.TD_GetLockerId, ""));
                        break;
                }
                break;
            case "invalid":
                touchDisplayEmulatorController.invalidPasscodeText.setVisible(true);
                break;

            default:
                log.severe(id + ": verify passcode with unknown validity -- " + msg.getDetails());
                break;
        }
    }

} // TouchDisplayEmulator
