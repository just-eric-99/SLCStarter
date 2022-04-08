package SLC.TouchDisplayHandler;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;


//======================================================================
// TouchDisplayHandler
public class TouchDisplayHandler extends HWHandler {
    //------------------------------------------------------------
    // TouchDisplayHandler
    public TouchDisplayHandler(String id, AppKickstarter appKickstarter) throws Exception {
        super(id, appKickstarter);
    } // TouchDisplayHandler


    //------------------------------------------------------------
    // processMsg

    // only receive message
    protected void processMsg(Msg msg) {
//        String selectScreen = TouchDisplayEmulatorController.getSelectedScreen();
//        switch (selectScreen) {
//            case "Scan Barcode":
//                switch (msg.getType()) {
//                    case TD_DisplayBarcode:
//
//                }
//        }


        switch (msg.getType()) {
            case TD_MouseClicked:
                String[] stringTokens = msg.getDetails().split(",");

                slc.send(new Msg(id, mbox, Msg.Type.TD_MouseClicked, msg.getDetails()));
                break;

            case TD_UpdateDisplay:
                handleUpdateDisplay(msg);
                break;

            case TD_SendPasscode:
                handleSendPasscode(msg);
                break;

            case TD_SendBarcode:
                handleSendBarcode(msg);
                break;

            case TD_DisplayBarcode:
                handleDisplayBarcode(msg);
                break;

            case TD_VerifiedPasscode:
                handleVerifyPasscode(msg);
                break;

            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
    } // processMsg


    //------------------------------------------------------------
    // handleUpdateDisplay
    protected void handleUpdateDisplay(Msg msg) {
        log.info(id + ": update display -- " + msg.getDetails());
    } // handleUpdateDisplay

    // fixme
    protected void handleSendPasscode(Msg msg) {
        log.info(id + ": received passcode -- " + msg.getDetails());
        slc.send(new Msg(id, mbox, Msg.Type.TD_SendPasscode, msg.getDetails()));
    }

    // fixme
    protected void handleSendBarcode(Msg msg) {
        log.info(id + ": received barcode -- " + msg.getDetails());
    }

    protected void handleCheckIn(Msg msg) {
        log.info(id + ": Handle go check in.");
        slc.send(new Msg(id, mbox, Msg.Type.TD_GoCheckIn, ""));
    }


    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() {
        log.info(id + ": Handle Poll");
    } // handlePoll

    protected void handleDisplayBarcode(Msg msg) {
        log.info(id + ": Display barcode after scanning, barcode: " + msg.getDetails());
    }

    protected void handleVerifyPasscode(Msg msg) {}
} // TouchDisplayHandler
