package SLC.SLC;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;

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

    private List<SmallLocker> smallLockers = new ArrayList<>();

//    private Stage stage = Stage.Welcome_Page;
    private Stage stage = Stage.Main_Menu;
    boolean brIsActive = false;


    //------------------------------------------------------------
    // SLC
    public SLC(String id, AppKickstarter appKickstarter) throws Exception {
        super(id, appKickstarter);
        pollingTime = Integer.parseInt(appKickstarter.getProperty("SLC.PollingTime"));
        loadLockerInfo();
    } // SLC

    private void loadLockerInfo() {
        int total = Integer.parseInt(appKickstarter.getProperty("locker.Count"));
        for (int i = 0; i < total; i++)
            smallLockers.add(new SmallLocker(appKickstarter.getProperty("Locker.LockerId" + i)));
    }


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
                // Msg process all the time
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

                case SLS_Fee:
                    setFee(msg);
                    break;

                case TD_GoCheckIn:
                    handleCheckIn();
                    break;

                // send a GetBarcode request to the BarcodeMbox

                case BR_IsActive:
                    // ask the barcode reader to read a barcode
                    // barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.BR_BarcodeRead, ""));
                    brIsActive = true;
                    break;

                case BR_BarcodeRead:
                    handleBarcodeRead(msg);
                    break;

                case SLS_BarcodeVerified:
                    barcodeVerified(msg);
                    break;

                case SLS_InvalidBarcode:
                    // show visible: text msg (invalid barcode)
                    // TODO check type or detail
                    touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, ""));
                    break;

                case Terminate:
                    quit = true;
                    break;

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

    private SmallLocker chooseEmptyLocker() {
        for (SmallLocker l : smallLockers) {
            if (!l.isOccupied())
                return l;
        }
        return null;
    }

    private void barcodeVerified(Msg msg) {
        if (stage != Stage.Scan_Barcode) return;
        // randomly choose an empty locker
        String barcode = msg.getDetails();
        long arriveTime = System.currentTimeMillis();
        SmallLocker sl = chooseEmptyLocker();
        if (sl == null) {
            // TODO send change screen to TD
            return;
        }
        int passcode = generatePasscode();
        Package p = sl.addPackage(barcode, passcode);
        p.setArriveTime(arriveTime);
        touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Show Locker," + sl.getLockerID()));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_Fee, barcode));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_TimeInterval, barcode));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_PackageArrived, barcode + "\t" + arriveTime));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_SendPasscode, barcode + "\t" + passcode));
        lockerMBox.send(new Msg(id, mbox, Msg.Type.L_Unlock, sl.getLockerID()));
        stage = Stage.Show_Locker;
    }

    private void handleCheckIn() {
        // activate barcode reader
        if (stage == Stage.Main_Menu) {
            touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "ScanBarcode"));
            barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.BR_GoActive, ""));
            stage = Stage.Scan_Barcode;
        }
    }

    private void handleBarcodeRead(Msg msg) {
        if (stage == Stage.Scan_Barcode && brIsActive) {
            // process
            // Br_isActive && stage = scan_barcode
            String barcode = msg.getDetails().trim();
            if (findPackage(barcode) != null) {
                // fixme already exist?

                return;
            }
            slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_VerifyBarcode, barcode));
        }
    }

    private void setFee(Msg msg) {
        String[] tokens = msg.getDetails().split("\t");
        Package p = findPackage(tokens[0]);
        if (p != null) {
            p.setFee(Double.parseDouble(tokens[1]));
        }
        // TODO null?
    }

    private int generatePasscode() {
        long x;
        int[] generatedPasscode = new int[1];
        do {
            generatedPasscode[0] = ThreadLocalRandom.current().nextInt(10000000,100000000);
            x = smallLockers.stream().filter(l -> l.isOccupied() && l.passcodeIsSame(generatedPasscode[0])).count();
        } while (x == 0);

        return generatedPasscode[0];
    }

    private Package findPackage(String barcode) {
        for (SmallLocker sl : smallLockers) {
            Package p = sl.isContainPackage(barcode);
            if (p != null)
                return p;
        }
        return null;
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
