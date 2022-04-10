package SLC.SLC;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;
import Common.LockerSize;
import org.json.JSONObject;

import java.util.*;
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

    private final List<SmallLocker> smallLockers = new ArrayList<>();

    private Screen screen = Screen.Welcome_Page;
    private LockerFunction lockerFunction = LockerFunction.Home;

    // fixme Ask HW Status when start?
    private HWStatus brStatus = HWStatus.Initialize;
    private HWStatus octStatus = HWStatus.Initialize;
    private HWStatus tdStatus = HWStatus.Initialize;
    private HWStatus serverStatus = HWStatus.Initialize;

    private String touchScreenMsg = "";
    private String readBarcode = "";
    private SmallLocker openLocker = null;
    private int fee = -1;

    private final List<Thread> threadList = new ArrayList<>();
    private final List<Msg> msgQueue = new ArrayList<>();
    private final HashMap<String, JSONObject> diagnostic = new HashMap<>();


    //------------------------------------------------------------
    // SLC
    public SLC(String id, AppKickstarter appKickstarter) throws Exception {
        super(id, appKickstarter);
        pollingTime = Integer.parseInt(appKickstarter.getProperty("SLC.PollingTime"));
        loadLockerInfo();
        loadDiagnosticInfo();
    } // SLC

    private void loadLockerInfo() {
        int total = Integer.parseInt(appKickstarter.getProperty("Locker.Count"));
        for (int i = 0; i < total; i++) {
            LockerSize size = LockerSize.valueOf(appKickstarter.getProperty("Locker.LockerId" + i + ".Size"));
            smallLockers.add(new SmallLocker(i + "", size));
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
                    checkThread();
                    break;

                case PollAck:
                    handlePollAck(msg);
                    break;

                case PollNak:
                    handlePollNak(msg);
                    break;

                case TD_GoPickUp:
                    handlePickUp();
                    break;

                case TD_SendPasscode:
                    sendPasscode(msg);
                    break;

                // For hacking purpose
                case TD_ChangeScreen:
                    updateScreen(Screen.valueOf(msg.getDetails()));
                    break;

                case BR_IsActive:
                    brStatus = HWStatus.Active;
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
                    updateScreen(Screen.Scan_Barcode, "Barcode is not valid.");
                    break;

                case L_Opened:
                    handleLockerOpened(msg);
                    break;

                case L_HasClose:
                    // receive locker close message with lockerId
                    handleLockerClose(msg);
                    break;

                case OCR_WaitingTransaction:
                    handleWaitingTransaction(msg);
                    break;

                case OCR_CardOK:
                    handleReceiveFee();
                    break;

                case OCR_CardFailed:
                    handlePaymentFail(msg);
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

        killThread();
        // declaring our departure
        appKickstarter.unregThread(this);
        log.info(id + ": terminating...");
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
                brStatus = HWStatus.Active;
                break;

            case "OctopusCardReaderDriver":
                octStatus = HWStatus.Active;
                break;

            case "TouchDisplayHandler":
                tdStatus = HWStatus.Active;
                break;

            case "SLSvrHandler":
                serverStatus = HWStatus.Active;
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
                octStatus = HWStatus.Fail;
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

        Thread t = new Thread(() -> {
            try {
                for (int i = 0; i < 240; i++) {
                    Thread.sleep(500);
                    // if all 5 hardware received, send ->
                    int count = (int) diagnostic.entrySet().parallelStream().filter(data -> data.getValue().equals("")).count();
                    if (count == 0)
                        break;
                }
                generateDiagnostic();
            } catch (InterruptedException ignored) {}
        });
        addAndStartThread(t);
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
        // diagnostic to string, generate diagnostic
//        Map<Object, Object> map = new HashMap<>();
//        map.put("Information", diagnostic);
//
//        String data = new GsonBuilder().setPrettyPrinting().create().toJson(map).replace("\\n", "\n").replace("\\", "");
//        System.out.println("Inside slc...");
//        System.out.println(data);
//        System.out.println("end...");
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
        Thread t = new Thread(() -> {
            try {
                while (serverStatus == HWStatus.Disconnected) {
                    log.info("Trying to reconnect server...");
                    slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_Reconnect, ""));
                    Thread.sleep(10000);
                }
            } catch (InterruptedException ignored) {}
        });
        addAndStartThread(t);
    }

    private void checkThread() {
        synchronized (threadList) {
            log.info("Checking for thread list (length: " + threadList.size() + ")...");
            threadList.removeIf(t -> !t.isAlive());
        }
    }

    private void addAndStartThread(Thread t) {
        t.start();
        synchronized (threadList) {
            threadList.add(t);
        }
    }

    private void killThread() {
        synchronized (threadList) {
            threadList.forEach(Thread::interrupt);
        }
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

        if (x > checkInXLeft && x < checkInXRight && y > checkInYTop && y < checkInYBottom)
            handleCheckIn();
        else if (x > pickUpXLeft && x < pickUpXRight && y > pickUpYTop && y < pickUpYBottom) {
            touchScreenMsg = "";
            updateScreen(Screen.Enter_Passcode);
            lockerFunction = LockerFunction.Pick_Up;
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

    private void processEnterPasscode(int x, int y) {
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

        int enterXLeft = 492;
        int enterXRight = 580;
        int enterYTop = 125;
        int enterYBottom = 184;

        if (x > enterXLeft && x < enterXRight && y > enterYTop && y < enterYBottom) {
            // fixme empty string problem
            handleVerifyPasscode(Integer.parseInt(touchScreenMsg));
            touchScreenMsg = "";
            return;
        }

        if (isRtnHome(x, y) || y < numPad1YTop) {
            return;
        } else if (x > numPad1XLeft && x < numPad1XRight && y > numPad1YTop && y < numPad1YBottom) {
            touchScreenMsg += "1";
        } else if (x > numPad2XLeft && x < numPad2XRight && y > numPad2YTop && y < numPad2YBottom) {
            touchScreenMsg += "2";
        } else if (x > numPad3XLeft && x < numPad3XRight && y > numPad3YTop && y < numPad3YBottom) {
            touchScreenMsg += "3";
        } else if (x > numPad4XLeft && x < numPad4XRight && y > numPad4YTop && y < numPad4YBottom) {
            touchScreenMsg += "4";
        } else if (x > numPad5XLeft && x < numPad5XRight && y > numPad5YTop && y < numPad5YBottom) {
            touchScreenMsg += "5";
        } else if (x > numPad6XLeft && x < numPad6XRight && y > numPad6YTop && y < numPad6YBottom) {
            touchScreenMsg += "6";
        } else if (x > numPad7XLeft && x < numPad7XRight && y > numPad7YTop && y < numPad7YBottom) {
            touchScreenMsg += "7";
        } else if (x > numPad8XLeft && x < numPad8XRight && y > numPad8YTop && y < numPad8YBottom) {
            touchScreenMsg += "8";
        } else if (x > numPad9XLeft && x < numPad9XRight && y > numPad9YTop && y < numPad9YBottom) {
            touchScreenMsg += "9";
        } else if (x > numPad0XLeft && x < numPad0XRight && y > numPad0YTop && y < numPad0YBottom) {
            touchScreenMsg += "0";
            System.out.println(touchScreenMsg);
        } else if (x > numPadBKSpaceXLeft && x < numPadBKSpaceXRight && y > numPadBKSpaceYTop && y < numPadBKSpaceYBottom) {
            touchScreenMsg = touchScreenMsg.length() > 0 ? touchScreenMsg.substring(0, touchScreenMsg.length() - 1) : "";
        } else if (x > numPadClearXLeft && x < numPadClearXRight && y > numPadClearYTop && y < numPadClearYBottom) {
            touchScreenMsg = "";
        }
        updateScreen(Screen.Enter_Passcode, touchScreenMsg);
    }

    private void processScanBarcode(int x, int y) {
        if (isRtnHome(x, y)) {
            barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.BR_GoStandby, ""));
        }
    }

    private void handleVerifyPasscode(int passcode) {
        SmallLocker sl = findByPasscode(passcode);
        if (sl == null)
            updateScreen(Screen.Enter_Passcode, "Invalid passcode");
        else {
            int fee = sl.getPayment();
            openLocker = sl;
            System.out.println("Fee: " + fee);
            if (fee == 0) {
                lockerMBox.send(new Msg(id, mbox, Msg.Type.L_Unlock, sl.getLockerID()));
            } else {
                this.fee = fee;
                octopusCardReaderMBox.send(new Msg(id, mbox, Msg.Type.OCR_TransactionRequest, fee + ""));
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
        if (screen != Screen.Scan_Barcode)
            return;
        // randomly choose an empty locker
        String[] tokens = msg.getDetails().split("\t");
        String barcode = tokens[0].trim();
        // TODO Screen to show error
        if (!barcode.equals(readBarcode)) {
            updateScreen(Screen.Scan_Barcode, "Barcode is invalid.\nPlease contact administrator.");
            return;
        }

        LockerSize size = LockerSize.valueOf(tokens[1].trim());
        SmallLocker sl = chooseEmptyLocker(size);
        if (sl == null) {
            // TODO send change screen to TD
            updateScreen(Screen.Scan_Barcode, "Size " + size + " locker is full.\nPlease contact administrator.");
            return;
        }

        int passcode = generatePasscode();
        sl.addPackage(barcode, passcode);
        log.info("Added package #" + sl.getBarcode() + " to locker #" + sl.getLockerID());

        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_PackageArrived, barcode + "\t" + sl.getArriveTime()));
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_SendPasscode, barcode + "\t" + passcode));
        barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.BR_GoStandby, ""));
        lockerMBox.send(new Msg(id, mbox, Msg.Type.L_Unlock, sl.getLockerID()));
        openLocker = sl;
    }

    private void handleCheckIn() {
        if (screen != Screen.Main_Menu && screen != Screen.Show_Locker)
            return;
        else if (serverStatus == HWStatus.Disconnected || serverStatus == HWStatus.Fail) {
            handleServerDownScreen();
            return;
        }

        lockerFunction = LockerFunction.Check_In;
        barcodeReaderMBox.send(new Msg(id, mbox, Msg.Type.BR_GoActive, ""));
        updateScreen(Screen.Scan_Barcode);
    }

    private void handlePickUp() {
        if (screen != Screen.Main_Menu)
            return;
        updateScreen(Screen.Enter_Passcode);
    }

    private void handleServerDownScreen() {
        updateScreen(Screen.Server_Down);
        timeOutToWelcomePage(5);
    }

    private void sendPasscode(Msg msg) {
        if (screen != Screen.Enter_Passcode)
            return;
        SmallLocker sl = findByPasscode(Integer.parseInt(msg.getDetails()));
        // null -> passcode wrong, not null -> passcode correct
        if (sl == null) {
            // fixme Passcode wrong page
            touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, ""));
            // TODO Time out -> Display passcode page
        } else {
            // TODO
        }
    }

    private void handleBarcodeRead(Msg msg) {
        if (screen != Screen.Scan_Barcode || brStatus != HWStatus.Active)
            return;

        String barcode = msg.getDetails().trim();
        readBarcode = barcode;
        if (findPackage(barcode)) {
            // fixme already exist?
            return;
        }
        readBarcode = barcode;
        slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_VerifyBarcode, barcode));
    }

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
        String lockerID = msg.getDetails().trim();
        if (openLocker.getLockerID().equals(lockerID)) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    updateScreen(Screen.Show_Locker, lockerID);
                } catch (InterruptedException ignored) {
                }
            });
            addAndStartThread(t);
        }

        if (lockerFunction == LockerFunction.Pick_Up)
            openLocker.pickUpPackage();
    }

    private void handleLockerClose(Msg msg) {
        System.out.println("Locker close: " + msg);
        String lockerID = msg.getDetails().trim();
        if (openLocker.getLockerID().equals(lockerID)) {
            if (lockerFunction == LockerFunction.Check_In) {
                handleCheckIn();
            } else if (lockerFunction == LockerFunction.Pick_Up) {
                String barcode = openLocker.pickUpPackage();
                slSvrHandlerMBox.send(new Msg(id, mbox, Msg.Type.SLS_PackagePicked, barcode + "\t" + System.currentTimeMillis() + ""));
                updateScreen(Screen.Main_Menu);
                lockerFunction = LockerFunction.Home;
            }
            openLocker = null;    // locker is close
        }
    }

    private void handleWaitingTransaction(Msg msg) {
        double fee = Double.parseDouble(msg.getDetails());
        if (this.fee == fee) {
            updateScreen(Screen.Payment, this.fee + "");
        }
    }

    private void handleReceiveFee() {
        updateScreen(Screen.Payment_Succeeded);
        lockerMBox.send(new Msg(id, mbox, Msg.Type.L_Unlock, openLocker.getLockerID()));
    }

    private void handlePaymentFail(Msg msg) {
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
    }

    private boolean findPackage(String barcode) {
        synchronized (smallLockers) {
            SmallLocker smallLocker = smallLockers.stream().filter(sl -> sl.isContainSamePackage(barcode)).findFirst().orElse(null);
            return smallLocker != null;
        }
    }

    private SmallLocker findByPasscode(int passcode) {
        synchronized (smallLockers) {
            return smallLockers.stream().filter(sl -> sl.passcodeIsSame(passcode)).findFirst().orElse(null);
        }
    }

    private void timeOutToWelcomePage(int second) {
        Screen s = screen;
        Thread t = new Thread(() -> {
            try {
                for (int i = 0; i < second * 2; i++) {
                    Thread.sleep(500);
                    if (screen != s)
                        return;
                }
                updateScreen(Screen.Welcome_Page);
            } catch (InterruptedException ignored) {}
        });
        addAndStartThread(t);
    }

    protected void shutDown() throws InterruptedException {

        // send a message to touch display to display "Shutting down ..." with a loading icon?
        Thread.sleep(5000);
        System.exit(0);
    }

    private enum LockerFunction {
        Home,
        Pick_Up,
        Check_In
    }
} // SLC
