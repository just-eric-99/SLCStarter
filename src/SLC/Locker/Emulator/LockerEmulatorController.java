package SLC.Locker.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import SLC.Locker.Locker;
import SLC.Locker.LockerDriver;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class LockerEmulatorController {

    private String id;
    private AppKickstarter appKickstarter;
    private Logger log;
    private LockerEmulator lockerEmulator;
    private MBox lockerMBox;

    private String pollResp;
    private String unlockResp;
    private String closeResp;

    public ChoiceBox pollRespCBox;
    public ChoiceBox unlockRespCBox;
    public ChoiceBox closeRespCBox;
    public TextArea lockerTextArea;


    public void initialize(String id, AppKickstarter appKickstarter, Logger log, LockerEmulator lockerEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.lockerEmulator = lockerEmulator;
        this.lockerMBox = appKickstarter.getThread("LockerDriver").getMBox();

        this.pollRespCBox.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> {
                    pollResp = pollRespCBox.getItems().get(newValue.intValue()).toString();
                    appendTextArea("Poll Response set to " + pollRespCBox.getItems().get(newValue.intValue()).toString());
                });

        this.pollResp = pollRespCBox.getValue().toString();
    }



    public void appendTextArea(String status) {
        lockerTextArea.appendText(status+"\n");
    }

    public String getPollResp() {
        return pollResp;
    }

    public String getUnlockResp() {return unlockResp;}

    public String getCloseResp() {return closeResp;}

//    public void buttonPressed(ActionEvent actionEvent) {
//        Button btn = (Button) actionEvent.getSource();
//        int index = Integer.parseInt(btn.getId());
//        ArrayList<Locker> lockers = LockerDriver.getLockers();
//
//        if (!lockers.get(index).isOpened()) {
//            appendTextArea("Locker #" + index + " is not opened.");
//        } else {
//            lockers.get(index).setOpened(false);
//            appendTextArea("Closing Locker #" + index + "...");
//            appendTextArea("Locker #" + index + " is closed.");
//        }
//    }

    public void buttonPressed(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();
        String index = btn.getId();
        HashMap<String, Boolean> lockers = LockerDriver.getLockers();
//        true close, false opened
        if (!lockers.get(index)) {
            appendTextArea("Closing Locker #" + index + "...");
            appendTextArea("Locker #" + index + " is closed.");
            lockers.put(index, true);
            lockerMBox.send(new Msg(id, lockerMBox, Msg.Type.L_HasClose, index));
        } else {
            appendTextArea("Locker #" + index + " is not opened.");
        }
    }


}
