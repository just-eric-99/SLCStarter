package SLC.SLC;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;
import SLC.Locker.Locker;
import SLC.Locker.LockerDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


//======================================================================
// SLC
public class SLC extends AppThread {
    private int pollingTime;
    private MBox barcodeReaderMBox;
    private MBox touchDisplayMBox;
    private MBox slSvrHandlerMBox;
    private MBox lockerMBox;
    private MBox octopusCardReaderMBox;

    ArrayList<Locker> lockers = LockerDriver.getLockers();

    Stage stage = Stage.Welcome_Page;
    boolean brIsActive = false;


    //------------------------------------------------------------
    // SLC
    public SLC(String id, AppKickstarter appKickstarter) throws Exception {
        super(id, appKickstarter);
        pollingTime = Integer.parseInt(appKickstarter.getProperty("SLC.PollingTime"));
    } // SLC


    //------------------------------------------------------------
    // run
    public void run() {
        Timer.setTimer(id, mbox, pollingTime);
        log.info(id + ": starting...");

        barcodeReaderMBox = appKickstarter.getThread("BarcodeReaderDriver").getMBox();
        octopusCardReaderMBox = appKickstarter.getThread("OctopusCardReaderDriver").getMBox();
        touchDisplayMBox = appKickstarter.getThread("TouchDisplayHandler").getMBox();
        slSvrHandlerMBox = appKickstarter.getThread("SLSvrHandler").getMBox();
        lockerMBox = appKickstarter.getThread("LockerDriver").getMBox();

        for (boolean quit = false; !quit; ) {
            Msg msg = mbox.receive();

            log.fine(id + ": message received: [" + msg + "].");

            switch (msg.getType()) {
                case TD_MouseClicked:
                    log.info("MouseCLicked: " + msg.getDetails());
                    processMouseClicked(msg);
                    break;

                case TimesUp:
                    Timer.setTimer(id, mbox, pollingTime);
                    log.info("Poll: " + msg.getDetails());
                    barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
                    touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
                    octopusCardReaderMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
                    lockerMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
                    break;

                case PollAck:
                    log.info("PollAck: " + msg.getDetails());
                    break;

                case Terminate:
                    quit = true;
                    break;

                case TD_GetBarcode:
                    // activate barcode reader
                    if (stage == Stage.Main_Menu) {
                        barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.BR_GoActive, ""));
                        stage = Stage.Scan_Barcode;
                    }
                    break;

                // send a GetBarcode request to the BarcodeMbox

                case BR_IsActive:
//                     ask the barcode reader to read a barcode
                    barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.BR_BarcodeRead, ""));

                    break;

                case BR_BarcodeRead:
                    // process
                    // Br_isActive && stage = scan_barcode
                    String barcode = msg.getDetails();
                    // **verified? -> exist?
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_VerifyBarcode, barcode));
                    break;
                case SLS_BarcodeVerified:
                    // randomly choose an empty locker
                    String barcode1 = msg.getDetails();
                    String lockerId = chooseEmptyLocker();
                    int passcode = generatePasscode();
                    updateLocker(lockerId, passcode);
                    touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Show Locker," + lockerId));
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_Fee, barcode1));
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_TimeInterval, barcode1));
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_PackageArrived, barcode1 + "\t" + System.currentTimeMillis()));
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_SendPasscode, barcode1 + "\t" + passcode));
                    break;

                case SLS_InvalidBarcode:
                    // show visible: text msg (invalid barcode)
                    // TODO check type or detail
                    touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, ""));
                    break;

                case SLS_Fee:


                default:
                    log.warning(id + ": unknown message type: [" + msg + "]");
            }
        }

        // declaring our departure
        appKickstarter.unregThread(this);
        log.info(id + ": terminating...");
    } // run


    //------------------------------------------------------------
    // processMouseClicked
    private void processMouseClicked(Msg msg) {
        // *** process mouse click here!!! ***
    } // processMouseClicked

    private void loadLockers() {
        if (lockers != null) {
            for (int i = 0; i < 44; i++) {
                String nameInProperty = "Locker.LockerId" + i;
                String occupiedInProperty = "Locker.LockerId" + i + ".Occupied";

                String name = appKickstarter.getProperty(nameInProperty);
                boolean occupied = (Integer.parseInt(appKickstarter.getProperty(occupiedInProperty))) == 1;

                lockers.add(new Locker(name, occupied));
            }
        }
    }

    private String chooseEmptyLocker() {
        for (Locker locker : lockers) {
            if (!locker.isOccupied()) {
                return locker.getLockerId();
            }
        }
        return null;
    }

    private int generatePasscode() {
        long x;
        int[] generatedPasscode = new int[1];
        do {
            generatedPasscode[0] = ThreadLocalRandom.current().nextInt(10000000,100000000);
            x = lockers.stream().filter(locker -> locker.isOccupied() && (locker.getPasscode() == generatedPasscode[0])).count();
        } while (x == 0);

        return generatedPasscode[0];
    }

    void updateLocker(String lockerId, int passcode) {
        lockers.stream()
                .filter(locker -> locker.getLockerId().equals(lockerId))
                .forEach(locker -> {
                    locker.setOccupied(true);
                    locker.setPasscode(passcode);
                    locker.setArrivalTime(System.currentTimeMillis());
                });
    }

    private enum Stage {
        Welcome_Page,
        Main_Menu,
        Confirmation,
        Enter_Passcode,
        Payment,
        Payment_Succeeded,
        Payment_Failed,
        Scan_Barcode,
        Show_Locker,
        Locker_Not_Close,
    }
} // SLC
