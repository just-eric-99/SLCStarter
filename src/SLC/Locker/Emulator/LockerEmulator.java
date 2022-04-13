package SLC.Locker.Emulator;

import AppKickstarter.misc.Msg;
import SLC.Locker.LockerDriver;
import SLC.SLC.HWStatus;
import SLC.SLCStarter;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class LockerEmulator extends LockerDriver {
    private SLCStarter slcStarter;
    private String id;
    private Stage myStage;
    private LockerEmulatorController lockerEmulatorController;

    public LockerEmulator(String id, SLCStarter slcStarter) {
        super(id, slcStarter);
        this.slcStarter = slcStarter;
        this.id = id;
    }

    public void start() throws Exception {
        Parent root;
        myStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        String fxmlName = "LockerEmulator.fxml";
        loader.setLocation(LockerEmulator.class.getResource(fxmlName));
        root = loader.load();
        lockerEmulatorController = (LockerEmulatorController) loader.getController();
        lockerEmulatorController.initialize(id, slcStarter, log, this);
        myStage.initStyle(StageStyle.DECORATED);

        myStage.setScene(new Scene(root, 837, 641));
        myStage.setTitle("Locker");
        myStage.setResizable(false);

        myStage.setOnCloseRequest((WindowEvent event) -> {
            slcStarter.stopApp();
            Platform.exit();
        });

        myStage.show();
    }

    protected void handlePoll() {
        switch (lockerEmulatorController.getPollResp()) {
            case "ACK":
                slc.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
                lockerInnerState = HWStatus.Active;
                break;

            case "NAK":
                slc.send(new Msg(id, mbox, Msg.Type.PollNak, id + " is down!"));
                lockerInnerState = HWStatus.Fail;
                break;

            case "Ignore":
                // Just ignore.  do nothing!!
                break;
        }
    } // handlePoll

    protected void sendHasCloseMsg(String lockerId) {
        super.sendHasCloseMsg(lockerId);
        slc.send(new Msg(id, mbox, Msg.Type.L_HasClose, lockerId));
    }

    @Override
    protected void handleUnlock(String locker) {
        super.handleUnlock(locker);
        lockerEmulatorController.appendTextArea("Opening Locker #" + locker + "...");
        lockerEmulatorController.appendTextArea("Locker #" + locker + " opened...");
    }
}
