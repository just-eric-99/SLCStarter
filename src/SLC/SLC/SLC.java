package SLC.SLC;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;
import Common.LockerSize;
import Common.SimpleTimer.SimpleTimer;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


//======================================================================
// SLC
public class SLC extends AppThread {
    private int pollingTime;
    private String locationID;
    private MBox barcodeReaderMBox;
    private MBox touchDisplayMBox;
    private MBox slSvrHandlerMBox;
    private MBox lockerMBox;
    private MBox octopusCardReaderMBox;

    private final List<SmallLocker> smallLockers = new ArrayList<>();

    private Screen screen = Screen.Welcome_Page;
    private LockerFunction lockerFunction = LockerFunction.Home;

    private HWStatus brStatus;
    private HWStatus ocrStatus;
    private HWStatus tdStatus;
    private HWStatus lockerStatus;
    private HWStatus serverStatus;

    private String touchScreenPasscode = "";
    private String touchScreenAdmin = "";
    private String readBarcode = "";
    private SmallLocker openLocker = null;

    private final List<Msg> msgQueue = new ArrayList<>();
    private final HashMap<String, JSONObject> diagnostic = new HashMap<>();

    private SimpleTimer screenTimer = null;

    private final Object chgBRStatusLock = new Object();
    private final Object chgOCRStatusLock = new Object();

    private int brCallTime = 0;
    private int octCallTime = 0;


    //------------------------------------------------------------
    // SLC
    public SLC(String id, AppKickstarter appKickstarter) throws Exception {
        super(id, appKickstarter);
        pollingTime = Integer.parseInt(appKickstarter.getProperty("SLC.PollingTime"));
        locationID = appKickstarter.getProperty("Locker.Location");
        loadLockerInfo();
        loadDiagnosticInfo();
    } // SLC

    private void loadLockerInfo() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(locationID + ".db"));
            long size = in.readLong();
            for (long i = 0; i < size; i++)
                smallLockers.add((SmallLocker) in.readObject());
            log.info("Initialize SLC complete (" + size + " lockers information is found).");
        } catch (IOException | ClassNotFoundException e) {
            log.warning("Initialize SLC without locker data.");
            smallLockers.clear();

            int total = Integer.parseInt(appKickstarter.getProperty("Locker.Count"));
            for (int i = 0; i < total; i++) {
                LockerSize size = LockerSize.valueOf(appKickstarter.getProperty("Locker.LockerId" + i + ".Size"));
                smallLockers.add(new SmallLocker(i + "", size));
            }
        }
    }

    private void loadDiagnosticInfo() {
        if (diagnostic.isEmpty()) {
            diagnostic.put("Barcode Reader Driver", null);
            diagnostic.put("Octopus Card Reader", null);
            diagnostic.put("Touch Display", null);
            diagnostic.put("Smart Locker Server", null);
            diagnostic.put("Locker", null);
        }
    }

    //------------------------------------------------------------
    // run
    public void run() {
        Timer.setTimer(id, mbox, 200);
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
                    sendPolling();
                    break;

                case PollAck:
                    handlePollAck(msg);
                    break;

                case PollNak:
                    handlePollNak(msg);
                    break;

                // For hacking purpose
                case TD_ChangeScreen:
                    touchScreenPasscode = "";
                    readBarcode = "";
                    openLocker = null;
                    updateScreen(Screen.valueOf(msg.getDetails()));
                    break;

                case BR_IsActive:
                    handleBRIsActive();
                    break;

                case BR_IsStandby:
                    handleBRIsStandby();
                    break;

                case BR_BarcodeRead:
                    handleBarcodeRead(msg);
                    break;

                case SLS_Connected:
                    handleServerConnected();
                    break;

                case SLS_ConnectionFail:
                    handleConnectionFail(msg);
                    break;

                case SLS_RqDiagnostic:
                    handleSystemDiagnostic();
                    break;

                case SLS_BarcodeVerified:
                    barcodeVerified(msg);
                    break;

                case SLS_InvalidBarcode:
                    updateScreen(Screen.Scan_Barcode, "Barcode is invalid.");
                    break;

                case SLS_FailMsg:
                    handleFailMsg(msg);
                    break;

                case L_Opened:
                    handleLockerOpened(msg);
                    break;

                case L_HasClose:
                    // receive locker close message with lockerId
                    handleLockerClose(msg);
                    break;

                case OCR_WaitingTransaction:
                    handleWaitingTransaction();
                    break;

                case OCR_IsStandby:
                    handelOCRIsStandby();
                    break;

                case OCR_CardFailed:
                    handlePaymentFail(msg);
                    break;

                case OCR_CardOK:
                    handleReceiveFee(msg);
                    break;

                case L_RpDiagnostic:
                case TD_RpDiagnostic:
                case OCR_RpDiagnostic:
                case BR_RpDiagnostic:
                case SH_RpDiagnostic:
                    receiveDiagnostic(msg);
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
        shutDown();
    } // run

    private void sendPolling() {
        barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
        touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
        octopusCardReaderMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
        lockerMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
    }

    private void handlePollAck(Msg msg) {
        log.info("PollAck: " + msg.getDetails());
        String senderID = msg.getSender();
        switch (senderID) {
            case "BarcodeReaderDriver":
                if (brStatus == HWStatus.Fail)
                    new Thread(this::callBRGoStandby).start();
                break;

            case "OctopusCardReaderDriver":
                if (ocrStatus == HWStatus.Fail)
                    new Thread(this::callOCRGoStandby).start();
                break;

            case "TouchDisplayHandler":
                if (tdStatus != HWStatus.Active)
                    tdStatus = HWStatus.Active;
                break;

            case "SLSvrHandler":
                if (serverStatus != HWStatus.Active)
                    serverStatus = HWStatus.Active;
                while (!msgQueue.isEmpty())
                    slSvrHandlerMBox.send(msgQueue.remove(0));
                break;

            case "LockerDriver":
                if (lockerStatus != HWStatus.Active)
                    lockerStatus = HWStatus.Active;
                break;
        }
    }

    private void handlePollNak(Msg msg) {
        log.info("PollNck: " + msg.getDetails());
        String senderID = msg.getSender();
        switch (senderID) {
            case "BarcodeReaderDriver":
                brStatus = HWStatus.Fail;
                break;

            case "OctopusCardReaderDriver":
                ocrStatus = HWStatus.Fail;
                break;

            case "TouchDisplayHandler":
                tdStatus = HWStatus.Fail;
                break;

            case "SLSvrHandler":
                serverStatus = HWStatus.Fail;
                break;
        }
    }

    private void handleSystemDiagnostic() {
        log.info("Handle System Diagnostic.");
        barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.SLS_RqDiagnostic, ""));
        touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.SLS_RqDiagnostic, ""));
        octopusCardReaderMBox.send(new Msg(id, mbox, Msg.Type.SLS_RqDiagnostic, ""));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_RqDiagnostic, ""));
        lockerMBox.send(new Msg(id, mbox, Msg.Type.SLS_RqDiagnostic, ""));

        SimpleTimer timer = new SimpleTimer(120)
                .wakeIf(() -> (int) diagnostic.entrySet().parallelStream().filter(data -> data.getValue() == null).count() == 0)
                .afterWake(this::generateDiagnostic);
        timer.start();
    }

    private void receiveDiagnostic(Msg msg) {
        switch (msg.getType()) {
            case BR_RpDiagnostic:
                if (diagnostic.get("Barcode Reader Driver") == null) {
                    diagnostic.put("Barcode Reader Driver", new JSONObject(msg.getDetails()));
                }
                break;

            case OCR_RpDiagnostic:
                if (diagnostic.get("Octopus Card Reader") == null) {
                    diagnostic.put("Octopus Card Reader", new JSONObject(msg.getDetails()));
                }
                break;

            case TD_RpDiagnostic:
                if (diagnostic.get("Touch Display") == null) {
                    diagnostic.put("Touch Display", new JSONObject(msg.getDetails()));
                }
                break;

            case SH_RpDiagnostic:
                if (diagnostic.get("Smart Locker Server") == null) {
                    diagnostic.put("Smart Locker Server", new JSONObject(msg.getDetails()));
                }
                break;

            case L_RpDiagnostic:
                if (diagnostic.get("Locker") == null) {
                    diagnostic.put("Locker", new JSONObject(msg.getDetails()));
                }
                break;
        }
    }

    private void generateDiagnostic() {
        JSONObject combined = new JSONObject();
        combined.put("Barcode Reader Driver", diagnostic.get("Barcode Reader Driver"));
        combined.put("Octopus Card Reader", diagnostic.get("Octopus Card Reader"));
        combined.put("Touch Display", diagnostic.get("Touch Display"));
        combined.put("Smart Locker Server", diagnostic.get("Smart Locker Server"));
        combined.put("Locker", diagnostic.get("Locker"));

        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_SendDiagnostic, combined.toString()));
    }

    private void handleServerConnected() {
        if (serverStatus == HWStatus.Active)
            return;
        log.info("Connected to server.");
        serverStatus = HWStatus.Active;
    }

    private void handleConnectionFail(Msg msg) {
        log.warning(msg.getDetails());
        if (serverStatus == HWStatus.Disconnected)
            return;

        serverStatus = HWStatus.Disconnected;
        SimpleTimer timer = new SimpleTimer(10).isRepeat(true)
                .wakeIf(() -> serverStatus != HWStatus.Disconnected)
                .periodAction(() -> {
                    log.info("Reconnecting to server...");
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_Reconnect, ""));
                });
        timer.start();
    }

    //------------------------------------------------------------
    // processMouseClicked
    private void processMouseClicked(Msg msg) {
        // *** process mouse click here!!! ***
        StringTokenizer st = new StringTokenizer(msg.getDetails());
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());
        switch (screen) {
            case Welcome_Page:
                processWelcomePage();
                break;

            case Main_Menu:
                processMainMenu(x, y);
                break;

            case Enter_Passcode:
                processEnterPasscode(x, y);
                break;

            case Scan_Barcode:
                processScanBarcode(x, y);
                break;

            case Admin_Login:
                processAdminLogin(x, y);
                break;
        }
    } // processMouseClicked

    private void processWelcomePage() {
        updateScreen(Screen.Main_Menu);
    }

    private void processMainMenu(int x, int y) {
        int checkInXLeft = 20;
        int checkInXRight = 304;
        int checkInYTop = 269;
        int checkInYBottom = 341;

        int pickUpXLeft = 335;
        int pickUpXRight = 619;
        int pickUpYTop = 269;
        int pickUpYBottom = 341;

        int loginXLeft = 257;
        int loginXRight = 383;
        int loginYTop = 392;
        int loginYBottom = 447;

        if (x > checkInXLeft && x < checkInXRight && y > checkInYTop && y < checkInYBottom)
            handleCheckIn();
        else if (x > pickUpXLeft && x < pickUpXRight && y > pickUpYTop && y < pickUpYBottom) {
            touchScreenPasscode = "";
            updateScreen(Screen.Enter_Passcode);
            lockerFunction = LockerFunction.Pick_Up;
        } else if (x > loginXLeft && x < loginXRight && y > loginYTop && y < loginYBottom) {
            handleLogin();
        }
    }

    private boolean isRtnHome(int x, int y) {
        int homeXLeft = 46;
        int homeXRight = 87;
        int homeYTop = 82;
        int homeYBottom = 114;

        if (x > homeXLeft && x < homeXRight && y > homeYTop && y < homeYBottom) {
            updateScreen(Screen.Main_Menu);
            lockerFunction = LockerFunction.Home;
            return true;
        }
        return false;
    }

    private boolean isEnterKey(int x, int y) {
        int enterXLeft = 492;
        int enterXRight = 580;
        int enterYTop = 125;
        int enterYBottom = 184;

        return x > enterXLeft && x < enterXRight && y > enterYTop && y < enterYBottom;
    }

    private String getNumPad(int x, int y) {
        int numPad1XLeft = 1;
        int numPad1XRight = 215;
        int numPad1YTop = 247;
        int numPad1YBottom = 306;

        int numPad2XLeft = 215;
        int numPad2XRight = 429;
        int numPad2YTop = 247;
        int numPad2YBottom = 306;

        int numPad3XLeft = 429;
        int numPad3XRight = 643;
        int numPad3YTop = 247;
        int numPad3YBottom = 306;

        int numPad4XLeft = 1;
        int numPad4XRight = 215;
        int numPad4YTop = 306;
        int numPad4YBottom = 365;

        int numPad5XLeft = 215;
        int numPad5XRight = 429;
        int numPad5YTop = 306;
        int numPad5YBottom = 365;

        int numPad6XLeft = 429;
        int numPad6XRight = 643;
        int numPad6YTop = 306;
        int numPad6YBottom = 365;

        int numPad7XLeft = 1;
        int numPad7XRight = 215;
        int numPad7YTop = 365;
        int numPad7YBottom = 424;

        int numPad8XLeft = 215;
        int numPad8XRight = 429;
        int numPad8YTop = 365;
        int numPad8YBottom = 424;

        int numPad9XLeft = 429;
        int numPad9XRight = 643;
        int numPad9YTop = 365;
        int numPad9YBottom = 424;

        int numPad0XLeft = 215;
        int numPad0XRight = 429;
        int numPad0YTop = 424;
        int numPad0YBottom = 483;

        int numPadBKSpaceXLeft = 1;
        int numPadBKSpaceXRight = 215;
        int numPadBKSpaceYTop = 424;
        int numPadBKSpaceYBottom = 483;

        int numPadClearXLeft = 429;
        int numPadClearXRight = 643;
        int numPadClearYTop = 424;
        int numPadClearYBottom = 483;

        if (x > numPad1XLeft && x < numPad1XRight && y > numPad1YTop && y < numPad1YBottom) {
            return "1";
        } else if (x > numPad2XLeft && x < numPad2XRight && y > numPad2YTop && y < numPad2YBottom) {
            return "2";
        } else if (x > numPad3XLeft && x < numPad3XRight && y > numPad3YTop && y < numPad3YBottom) {
            return "3";
        } else if (x > numPad4XLeft && x < numPad4XRight && y > numPad4YTop && y < numPad4YBottom) {
            return "4";
        } else if (x > numPad5XLeft && x < numPad5XRight && y > numPad5YTop && y < numPad5YBottom) {
            return "5";
        } else if (x > numPad6XLeft && x < numPad6XRight && y > numPad6YTop && y < numPad6YBottom) {
            return "6";
        } else if (x > numPad7XLeft && x < numPad7XRight && y > numPad7YTop && y < numPad7YBottom) {
            return "7";
        } else if (x > numPad8XLeft && x < numPad8XRight && y > numPad8YTop && y < numPad8YBottom) {
            return "8";
        } else if (x > numPad9XLeft && x < numPad9XRight && y > numPad9YTop && y < numPad9YBottom) {
            return "9";
        } else if (x > numPad0XLeft && x < numPad0XRight && y > numPad0YTop && y < numPad0YBottom) {
            return "0";
        } else if (x > numPadBKSpaceXLeft && x < numPadBKSpaceXRight && y > numPadBKSpaceYTop && y < numPadBKSpaceYBottom) {
            return "backspace";
        } else if (x > numPadClearXLeft && x < numPadClearXRight && y > numPadClearYTop && y < numPadClearYBottom) {
            return "clear";
        }
        return null;
    }

    private void processEnterPasscode(int x, int y) {
        if (isEnterKey(x, y)) {
            if (!touchScreenPasscode.isEmpty())
                handleVerifyPasscode(Integer.parseInt(touchScreenPasscode));
            touchScreenPasscode = "";
            return;
        }

        String input = getNumPad(x, y);
        if (isRtnHome(x, y) || input == null)
            return;
        else if (input.equals("backspace"))
            touchScreenPasscode = touchScreenPasscode.length() > 0 ? touchScreenPasscode.substring(0, touchScreenPasscode.length() - 1) : "";
        else if (input.equals("clear"))
            touchScreenPasscode = "";
        else
            touchScreenPasscode += input;

        updateScreen(Screen.Enter_Passcode, touchScreenPasscode);
    }

    private void processScanBarcode(int x, int y) {
        if (isRtnHome(x, y)) {
            SimpleTimer timer = new SimpleTimer(2)
                    .wakeIf(() -> screen == Screen.Scan_Barcode)
                    .isRepeat(true)
                    .periodAction(this::callBRGoStandby)
                    .afterWake(() -> brCallTime = 0);
            timer.start();
        }
    }

    private void processAdminLogin(int x, int y) {
        // TODO
        int usernameXLeft = 190;
        int usernameXRight = 478;
        int usernameYTop = 105;
        int usernameYBottom = 143;

        int passwordXLeft = 190;
        int passwordXRight = 478;
        int passwordYTop = 165;
        int passwordYBottom = 202;

        if (isEnterKey(x, y)) {
            if (!touchScreenAdmin.isEmpty())
//                handleVerifyAdminUser(Integer.parseInt(touchScreenAdmin));
                touchScreenAdmin = "";
            return;
        }

        if (isRtnHome(x, y)) {

        }
    }

    private void handleVerifyAdminUser(int username, int password) {
        // TODO
    }

    private void handleVerifyPasscode(int passcode) {
        SmallLocker sl = findByPasscode(passcode);
        if (sl == null)
            updateScreen(Screen.Enter_Passcode, "Invalid passcode");
        else {
            int fee = sl.getPayment();
            openLocker = sl;

            // Check fee
            if (fee == 0)
                lockerMBox.send(new Msg(id, mbox, Msg.Type.L_Unlock, sl.getLockerID()));
            else {
                updateScreen(Screen.Payment, fee + "");
                SimpleTimer timer = new SimpleTimer(2)
                        .wakeIf(() -> screen != Screen.Payment)
                        .isRepeat(true)
                        .periodAction(() -> requestOCRForTransaction(fee))
                        .afterWake(() -> octCallTime = 0);
                timer.start();
            }
        }
    }

    private SmallLocker chooseEmptyLocker(LockerSize size) {
        for (SmallLocker l : smallLockers) {
            if (!l.isOccupied() && l.getSize() == size)
                return l;
        }
        return null;
    }

    private void barcodeVerified(Msg msg) {
        if (screen != Screen.Scan_Barcode || brStatus == HWStatus.Fail)
            return;

        // Choose an empty locker
        String[] tokens = msg.getDetails().split("\t");
        String barcode = tokens[0].trim();

        // Handle same barcode in same locker
        if (!barcode.equals(readBarcode)) {
            updateScreen(Screen.Scan_Barcode, "Barcode is invalid.\nPlease contact administrator.");
            return;
        }

        LockerSize size = LockerSize.valueOf(tokens[1].trim());
        SmallLocker sl = chooseEmptyLocker(size);

        // Handle locker is full
        if (sl == null) {
            updateScreen(Screen.Scan_Barcode, "Size " + size + " locker is full.\nPlease contact administrator.");
            return;
        }

        int passcode = generatePasscode();
        sl.addPackage(barcode, passcode);
        log.info("Added package #" + sl.getBarcode() + " to locker #" + sl.getLockerID());

        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_PackageArrived, barcode + "\t" + sl.getArriveTime()));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_SendPasscode, barcode + "\t" + passcode));
        lockerMBox.send(new Msg(id, mbox, Msg.Type.L_Unlock, sl.getLockerID()));
        SimpleTimer timer = new SimpleTimer(2)
                .wakeIf(() -> screen == Screen.Scan_Barcode)
                .isRepeat(true)
                .periodAction(this::callBRGoStandby)
                .afterWake(() -> brCallTime = 0);
        timer.start();

        openLocker = sl;
    }

    private void handleFailMsg(Msg msg) {
        String[] tokens = msg.getDetails().split("%");
        Msg.Type type = Msg.Type.valueOf(tokens[0]);
        String detail = tokens.length > 1 ? tokens[1] : "";
        log.warning("Fail to send message to server: [" + type + "] " + detail);
        if (type != Msg.Type.Poll)
            msgQueue.add(new Msg(id, mbox, type, detail));
    }

    private void handleLogin() {
        // TODO
        updateScreen(Screen.Admin_Login);
    }

    private void handleCheckIn() {
        if (screen != Screen.Main_Menu && screen != Screen.Show_Locker)
            return;
        else if (serverStatus == HWStatus.Disconnected || serverStatus == HWStatus.Fail || brStatus == HWStatus.Fail) {
            updateScreen(Screen.Server_Down);
            return;
        }

        lockerFunction = LockerFunction.Check_In;

        SimpleTimer timer = new SimpleTimer(2)
                .wakeIf(() -> screen != Screen.Scan_Barcode)
                .isRepeat(true)
                .periodAction(this::callBRGoActive)
                .afterWake(() -> brCallTime = 0);
        timer.start();
        updateScreen(Screen.Scan_Barcode);
    }

    //------------------------------------------------------------
    // Barcode Reader Related functions
    private void handleBRIsActive() {
        if (brStatus == HWStatus.Fail)
            return;

        brStatus = HWStatus.Active;

        if (screen != Screen.Scan_Barcode) {
            new Thread(this::callBRGoStandby).start();
        }
    }

    private void handleBRIsStandby() {
        if (brStatus == HWStatus.Fail)
            return;

        brStatus = HWStatus.Standby;

        if (screen == Screen.Scan_Barcode) {
            new Thread(this::callBRGoActive).start();
        }
    }

    private void handleBarcodeRead(Msg msg) {
        if (screen != Screen.Scan_Barcode)
            return;
        else if (brStatus == HWStatus.Fail) {
            updateScreen(Screen.Server_Down);
            return;
        }

        String barcode = msg.getDetails().trim();
        readBarcode = barcode;
        if (findPackage(barcode)) {
            updateScreen(Screen.Scan_Barcode, "Barcode is invalid.\nPlease contact administrator.");
            return;
        }
        readBarcode = barcode;
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_VerifyBarcode, barcode));

        // Handle network problem
        if (serverStatus != HWStatus.Active)
            updateScreen(Screen.Server_Down);
        else {
            SimpleTimer timer = new SimpleTimer(5)
                    .wakeIf(() -> screen != Screen.Scan_Barcode)
                    .afterWake(() -> {
                        // fixme something wrong
                        if (screen == Screen.Scan_Barcode)
                            updateScreen(Screen.Server_Down);
                    });
            timer.start();
        }
    }

    private void callBRGoActive() {
        callBRChangeStatus(new Msg(id, mbox, Msg.Type.BR_GoActive, ""));
    }

    private void callBRGoStandby() {
        callBRChangeStatus(new Msg(id, mbox, Msg.Type.BR_GoStandby, ""));
    }

    private void callBRChangeStatus(Msg msg) {
        synchronized (chgBRStatusLock) {
            if (screen == Screen.Scan_Barcode && brStatus == HWStatus.Active)
                return;

            if (screen != Screen.Scan_Barcode && brStatus == HWStatus.Standby)
                return;

            HWStatus hwStatus = msg.getType() == Msg.Type.BR_GoActive ? HWStatus.Active : HWStatus.Standby;

            if (brStatus != hwStatus) {
                log.info("Sending " + msg.getType() + " to barcode reader.");
                barcodeReaderMBox.send(msg);
                brCallTime++;
                if (brCallTime == 5) {
                    String detail = System.currentTimeMillis() + " Type: " + msg.getType() + ", Detail: " + msg.getDetails();
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_ReportFail, detail));
                    log.warning("Reported barcode go " + hwStatus.toString().toLowerCase() + " to server.");
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    // Barcode Reader Related functions
    //------------------------------------------------------------

    //------------------------------------------------------------
    // Octopus Card Reader Related functions
    private void handleWaitingTransaction() {
        ocrStatus = HWStatus.Active;

        if (screen != Screen.Payment)
            callOCRGoStandby();
    }


    private void handelOCRIsStandby() {
        ocrStatus = HWStatus.Standby;

        if (screen == Screen.Payment && openLocker != null)
            new Thread(()->requestOCRForTransaction(openLocker.getPayment())).start();
    }

    private void requestOCRForTransaction(int amount) {
        callOCRChangeStatus(new Msg(id, mbox, Msg.Type.OCR_TransactionRequest, amount + ""));
    }

    private void callOCRGoStandby() {
        callOCRChangeStatus(new Msg(id, mbox, Msg.Type.OCR_GoStandby, ""));
    }

    private void callOCRChangeStatus(Msg msg) {
        synchronized (chgOCRStatusLock) {
            if (screen == Screen.Payment && ocrStatus == HWStatus.Active)
                return;

            if (screen != Screen.Payment && ocrStatus == HWStatus.Standby)
                return;

            HWStatus hwStatus = msg.getType() == Msg.Type.OCR_TransactionRequest ? HWStatus.Active : HWStatus.Standby;

            if (ocrStatus != hwStatus) {
                log.info("Sending " + msg.getType() + " to octopus card reader.");
                octopusCardReaderMBox.send(msg);
                if (++octCallTime == 5) {
                    String detail = System.currentTimeMillis() + " Type: " + msg.getType() + ", Detail: " + msg.getDetails();
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_ReportFail, detail));
                    log.warning("Reported barcode go " + hwStatus.toString().toLowerCase() + " to server.");
                }
            }
        }
    }
    // Octopus Card Reader Related functions
    //------------------------------------------------------------

    //------------------------------------------------------------
    // Locker Related functions
    private int generatePasscode() {
        long x;
        int[] generatedPasscode = new int[1];
        do {
            generatedPasscode[0] = ThreadLocalRandom.current().nextInt(10000000, 100000000);
            x = smallLockers.stream().filter(l -> l.isOccupied() && l.passcodeIsSame(generatedPasscode[0])).count();
        } while (x != 0);

        return generatedPasscode[0];
    }

    private void handleLockerOpened(Msg msg) {
        if (lockerStatus == HWStatus.Fail)
            return;

        String lockerID = msg.getDetails().trim();
        if (screen != Screen.Enter_Passcode && screen != Screen.Payment_Succeeded && screen != Screen.Scan_Barcode){
            slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_ReportFail, "Unauthorized opening for locker #" + lockerID + "."));
            SmallLocker sl = findLocker(lockerID);
            if (sl != null)
                sl.setWorkNormal(false);
            return;
        }

        openLocker.setLocked(false);
        if (openLocker.getLockerID().equals(lockerID)) {
            SimpleTimer timer = new SimpleTimer(1).afterWake(() -> updateScreen(Screen.Show_Locker, lockerID));
            timer.start();
        }

        if (lockerFunction == LockerFunction.Pick_Up) {
            String barcode = openLocker.pickUpPackage();
            slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_PackagePicked, barcode + "\t" + System.currentTimeMillis() + ""));
        }
    }

    private void handleLockerClose(Msg msg) {
        if (lockerStatus == HWStatus.Fail)
            return;

        String lockerID = msg.getDetails().trim();
        if (openLocker.getLockerID().equals(lockerID)) {
            System.out.println("Fuck");
            if (lockerFunction == LockerFunction.Check_In) {
                handleCheckIn();
            } else if (lockerFunction == LockerFunction.Pick_Up) {
                updateScreen(Screen.Welcome_Page);
                lockerFunction = LockerFunction.Home;
            }
            openLocker.setLocked(true);
            openLocker = null;    // locker is close
        } else {
            slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_ReportFail, "Unusual closing for locker #" + lockerID + "."));
            SmallLocker sl = findLocker(lockerID);
            sl.setLocked(true);
        }
    }

    private void handleReceiveFee(Msg msg) {
        if (screen != Screen.Payment || ocrStatus == HWStatus.Fail)
            return;

        String[] tokens = msg.getDetails().split("\t");
        String cardNo = tokens[0];
        double fee = Double.parseDouble(tokens[1]);

        if (openLocker != null && openLocker.getPayment() == fee) {
            updateScreen(Screen.Payment_Succeeded);
            lockerMBox.send(new Msg(id, mbox, Msg.Type.L_Unlock, openLocker.getLockerID()));
            slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_Payment, openLocker.getBarcode() + "\t" + cardNo + "\t" + fee));

            SimpleTimer timer = new SimpleTimer(2)
                    .wakeIf(() -> screen == Screen.Payment)
                    .isRepeat(true)
                    .periodAction(this::callOCRGoStandby)
                    .afterWake(() -> octCallTime = 0);
            timer.start();
        } else {
            updateScreen(Screen.Server_Down, "Payment error found.");
            slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_ReportFail, System.currentTimeMillis() + ": Error payment, card #" + cardNo + " amount: " + fee));
        }
    }

    private void backUpLockerInfo() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(locationID + ".db"));
            out.writeLong(smallLockers.size());
            smallLockers.forEach(sl -> {
                try {
                    out.writeObject(sl);
                } catch (IOException e) {
                    log.warning("Back up locker #" + sl.getLockerID() + " information fail.");
                }
            });
        } catch (IOException e) {
            log.warning("Back up locker information fail.");
        }
    }
    // Locker Related functions
    //------------------------------------------------------------

    //------------------------------------------------------------
    // Touch Screen Related functions
    private void handlePaymentFail(Msg msg) {
        if (screen != Screen.Payment)
            return;

        updateScreen(Screen.Payment_Failed, msg.getDetails());
    }

    private void updateScreen(Screen s) {
        updateScreen(s, "");
    }

    private void updateScreen(Screen s, String msg) {
        String detail = s.toString();
        if (!msg.isEmpty())
            detail += "," + msg;
        touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, detail));
        screen = s;
        switch (s) {
            case Main_Menu:
            case Enter_Passcode:
            case Scan_Barcode:
            case Payment:
                timeOutToScreen(60, Screen.Welcome_Page);
                break;

            case Payment_Failed:
                timeOutToScreen(5, Screen.Payment, openLocker == null ? "" : (openLocker.getPayment() + ""));
                break;

            case Server_Down:
                timeOutToScreen(5, Screen.Welcome_Page);
                lockerFunction = LockerFunction.Home;
                break;
        }
    }

    private boolean findPackage(String barcode) {
        synchronized (smallLockers) {
            SmallLocker smallLocker = smallLockers.stream().filter(sl -> sl.isContainSamePackage(barcode)).findFirst().orElse(null);
            return smallLocker != null;
        }
    }

    private SmallLocker findLocker(String lockerID) {
        synchronized (smallLockers) {
            return smallLockers.stream().filter(sl -> sl.getLockerID().equals(lockerID)).findFirst().orElse(null);
        }
    }

    private SmallLocker findByPasscode(int passcode) {
        synchronized (smallLockers) {
            return smallLockers.stream().filter(sl -> sl.passcodeIsSame(passcode)).findFirst().orElse(null);
        }
    }

    private void timeOutToScreen(int second, Screen chgScreen) {
        timeOutToScreen(second, chgScreen, "");
    }

    private void timeOutToScreen(int second, Screen chgScreen, String msg) {
        Screen s = screen;
        SimpleTimer timer = new SimpleTimer(second).wakeIf(() -> screen != s)
                .afterWake(() -> {
                    if (s == screen) {
                        updateScreen(chgScreen, msg);
                        if (chgScreen == Screen.Welcome_Page) {
                            new Thread(this::callBRGoStandby).start();
                            new Thread(this::callOCRGoStandby).start();
                            lockerFunction = LockerFunction.Home;
                        }
                    }
                });
        timer.start();
        if (screenTimer != null)
            screenTimer.interrupt();
        screenTimer = timer;
    }
    // Touch Screen Related functions
    //------------------------------------------------------------

    private void shutDown() {
        backUpLockerInfo();
        System.exit(0);
    }

    private enum LockerFunction {
        Home,
        Pick_Up,
        Check_In
    }
} // SLC
