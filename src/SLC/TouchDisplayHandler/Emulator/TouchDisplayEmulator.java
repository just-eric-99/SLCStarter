package SLC.TouchDisplayHandler.Emulator;

import SLC.SLC.Screen;
import SLC.SLCStarter;
import SLC.TouchDisplayHandler.TouchDisplayHandler;
import AppKickstarter.misc.Msg;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
	reloadStage("TouchDisplayEmulator.fxml", "");
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
    private void reloadStage(String fxmlFName, String showMsg) {
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
                    touchDisplayEmulatorController.initialize(id, slcStarter, log, touchDisplayEmulator, pollResp, showMsg);
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

        String[] tokens = msg.getDetails().split(",");
        String page = tokens[0].trim();

        String showMsg = tokens.length > 1 ? tokens[1].trim() : "";

        switch (Screen.valueOf(page)) {
            case Welcome_Page:
                reloadStage("TouchDisplayEmulator.fxml", showMsg);
                break;

            case Main_Menu:
                reloadStage("TouchDisplayMainMenu.fxml", showMsg);
                break;

            case Confirmation:
                reloadStage("TouchDisplayConfirmation.fxml", showMsg);
                break;

            case Enter_Passcode:
                reloadStage("TouchDisplayEnterPasscode.fxml", showMsg);
                break;

            case Payment:
                reloadStage("TouchDisplayPayment.fxml", showMsg);
                break;

            case Payment_Succeeded:
                reloadStage("TouchDisplayPaymentSucceeded.fxml", showMsg);
                break;

            case Payment_Failed:
                reloadStage("TouchDisplayPaymentFailed.fxml", showMsg);
                break;

            case Scan_Barcode:
                reloadStage("TouchDisplayScanBarcode.fxml", showMsg);
                break;

            case Show_Locker:
                reloadStage("TouchDisplayShowLocker.fxml", showMsg);
                break;

            case Locker_Not_Close:
                reloadStage("TouchDisplayLockerNotClose.fxml", showMsg);
                break;

            case Server_Down:
                reloadStage("TouchDisplayServerDown.fxml", showMsg);
                break;

            case Admin_Login:
                reloadStage("TouchDisplayAdminLogin.fxml", showMsg);
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

    @Override
    public void changeScreen(Screen s) {
        super.changeScreen(s);
    }
} // TouchDisplayEmulator
