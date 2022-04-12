package SLSvr.SLSvr;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.Msg;
import AppKickstarter.timer.Timer;
import Common.LockerSize;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SLSvr extends AppThread {
    private ServerSocket slSvrSocket;
    private final int port;
    private int diagnosticTime;

    protected final List<Locker> lockers = new ArrayList<>();
    protected final List<Package> packages = new ArrayList<>();

    /**
     * Constructor for an appThread
     *
     * @param id             name of the appThread
     * @param appKickstarter a reference to our AppKickstarter
     */
    public SLSvr(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
        port = Integer.parseInt(appKickstarter.getProperty("Server.Port"));
        diagnosticTime = Integer.parseInt(appKickstarter.getProperty("SLSvr.DiagnosticTime"));
        loadLockerData();
        loadPackageData();
    }

    @Override
    public void run() {
        Timer.setTimer(id, mbox, diagnosticTime);
        log.info(id + ": starting...");

        try {
            slSvrSocket = new ServerSocket(port);
            Thread receiveSvr = new Thread(() -> {
                try {
                    runSvr();
                } catch (IOException e) {
                    log.info(id + " terminated.");
                }
            });
            receiveSvr.start();

            for (boolean quit = false; !quit; ) {
                Msg msg = mbox.receive();

                log.fine(id + ": message received: [" + msg + "].");

                switch (msg.getType()) {
                    case Terminate:
                        quit = true;
                        break;

                    case TimesUp:
                        Timer.setTimer(id, mbox, diagnosticTime);
                        requestDiagnostic();
                        break;

                    default:
                        try {
                            send(msg);
                        } catch (LockerException | PackageNotFoundException | IOException e) {
                            log.warning(e.getMessage());
                        }
                }
            }
            receiveSvr.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopSvr();
        appKickstarter.unregThread(this);
        shutDown();
    }

    private void send(Msg msg) throws LockerException, IOException, PackageNotFoundException {
        switch (msg.getType()) {
            case Poll:
                handlePoll(msg.getDetails());
                break;

            case SLS_BarcodeVerified:
                sendVerified(msg);
                break;

            case SLS_InvalidBarcode:
                sendInvalid(msg);
                break;

            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
    }

    private void runSvr() throws IOException {
        log.info(id + ": running...");
        while (true) {
            Socket cSocket = slSvrSocket.accept();
            log.info(cSocket.getInetAddress().getHostAddress() + " is connected.");

            Thread t = new Thread(() -> {
                Locker l = null;
                try {
                    DataInputStream in = new DataInputStream(cSocket.getInputStream());
                    String lockerID = readString(in).trim();
                    l = findLocker(lockerID);
                    log.info(cSocket.getInetAddress().getHostAddress() + " login as " + l.getID() + ".");

                    synchronized (l.getID()) {
                        l.setSocket(cSocket);
                    }

                    serve(in, lockerID);
                } catch (IOException e) {
                    log.info(cSocket.getInetAddress().getHostAddress() + " is disconnected.");
                } catch (LockerException e) {
                    log.warning(cSocket.getInetAddress().getHostAddress() + " is disconnected: Unauthorized");
                }

                if (l != null) {
                    synchronized (l.getID()) {
                        l.setSocket(null);
                    }
                }
            });
            t.start();
        }
    }

    private void serve(DataInputStream in, String lockerID) throws IOException, LockerException {
        while (true) {
            Msg.Type type = Msg.Type.values()[in.readInt()];
            try {
                switch (type) {
                    case Poll:
                        log.info("Handle Poll for locker #" + lockerID + ".");
                        mbox.send(new Msg(id, mbox, Msg.Type.Poll, lockerID));
                        break;

                    case SLS_VerifyBarcode:
                        verifyBarcode(in, lockerID);
                        break;

                    case SLS_SendPasscode:
                        setPasscode(in);
                        break;

                    case SLS_PackageArrived:
                        setArrivalTime(in, lockerID);
                        break;

                    case SLS_PackagePicked:
                        setPickUpTime(in);
                        break;

                    case SLS_Payment:
                        addPaymentRecord(in);
                        break;

                    case SLS_SendDiagnostic:
                        receiveDiagnostic(in);
                        break;

                    case SLS_ReportFail:
                        receiveReportFail(in, lockerID);
                        break;

                    default:
                        log.warning(id + ": unknown message type: [" + type + "]");
                        break;
                }
            } catch (PackageNotFoundException e) {
                log.warning(e.getMessage());
            }
        }
    }

    protected void requestDiagnostic() {
        synchronized (lockers) {
            lockers.forEach(l -> {
                Socket cSocket = l.getSocket();
                if (cSocket != null) {
                    try {
                        DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
                        out.writeInt(Msg.Type.SLS_RqDiagnostic.ordinal());
                        log.info("Request system diagnostic for locker #" + l.getID());
                    } catch (IOException e) {
                        log.warning("Request system diagnostic fail. Locker #" + l.getID() + " is disconnected.");
                    }
                } else {
                    log.warning("Request system diagnostic fail. Locker #" + l.getID() + " is disconnected.");
                }
            });
        }
    }

    protected void handlePoll(String detail) throws IOException, LockerException {
        Socket cSocket = findLocker(detail).getSocket();
        DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
        out.writeInt(Msg.Type.PollAck.ordinal());
    }

    protected void addPackage(String barcode, String lockerID, LockerSize size) throws LockerException {
        try {
            findPackage(barcode);
            throw new LockerException("Add Package fail: Package #" + barcode + " is already added.");
        } catch (PackageNotFoundException e) {
            try {
                Locker l = findLocker(lockerID);
                if (!l.reserveLocker(size))
                    throw new LockerException("Add Package fail: " + size + " is full.");
            } catch (LockerException ex) {
                throw new LockerException("Add Package fail: " + e.getMessage());
            }
            Package p = new Package(barcode, lockerID, size);
            synchronized (packages) {
                packages.add(p);
            }
            log.info("Add package #" + barcode + " to locker location #" + lockerID + " success.");
        }
    }

    private void sendPackageProperty(Msg msg, SendPackageListener sp) throws IOException, LockerException, PackageNotFoundException {
        String[] tokens = msg.getDetails().split("\t");
        Socket cSocket = findLocker(tokens[0]).getSocket();
        if (cSocket != null) {
            try {
                DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
                Package p = findPackage(tokens[1]);
                out.writeInt(msg.getType().ordinal());
                synchronized (p.getBarcode()) {
                    sp.send(out, p);
                }
                return;
            } catch (IOException ignored) {
            }
        }
        throw new IOException("Locker #" + tokens[0] + " is disconnected. Server will retry it later.");
    }

    protected void sendVerified(Msg msg) throws IOException, LockerException, PackageNotFoundException {
        try {
            sendPackageProperty(msg, (out, p) -> {
                sendString(out, p.getBarcode());
                sendString(out, p.getSize().toString());
            });
        } catch (IOException e) {
            throw new IOException("Send verified message fail: " + e.getMessage());
        } catch (LockerException e) {
            throw new LockerException("Send verified message fail: " + e.getMessage());
        }
    }

    protected void sendInvalid(Msg msg) throws LockerException, IOException {
        String[] tokens = msg.getDetails().split("\t");
        Socket cSocket = findLocker(tokens[0]).getSocket();
        if (cSocket != null) {
            try {
                DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
                out.writeInt(msg.getType().ordinal());
                sendString(out, tokens[1]);
                return;
            } catch (IOException ignored) {
            }
        }
        throw new IOException("Locker #" + tokens[0] + " is disconnected. Server will retry it later.");
    }

    protected void verifyBarcode(DataInputStream in, String lockerID) throws IOException {
        String barcode = readString(in);
        try {
            Package p = findPackage(barcode);
            mbox.send(new Msg(id, mbox, Msg.Type.SLS_BarcodeVerified, lockerID + "\t" + p.getBarcode() + "\t" + p.getSize()));
            log.info("verifyBarcode: Success. Verified barcode #" + barcode + " for locker #" + lockerID);
        } catch (PackageNotFoundException e) {
            mbox.send(new Msg(id, mbox, Msg.Type.SLS_InvalidBarcode, lockerID + "\t" + barcode));
            log.info("verifyBarcode: Fail. Cannot verify barcode #" + barcode + " for locker #" + lockerID);
        }
    }

    protected void setPasscode(DataInputStream in) throws IOException, PackageNotFoundException {
        String packageID = readString(in);
        int passcode = in.readInt();
        Package p = findPackage(packageID);
        p.setLockerPasscode(passcode);
        log.info("SetPasscode: Success. Received passcode #" + passcode + " for package #" + p.getBarcode());
    }

    protected void setArrivalTime(DataInputStream in, String lockerID) throws IOException, PackageNotFoundException {
        String packageID = readString(in);
        long date = in.readLong();
        Package p = findPackage(packageID);
        p.setArriveTime(new Date(date));
        log.info("SetArrivalTime: Success. Received arrival time for package #" + p.getBarcode());
    }

    protected void setPickUpTime(DataInputStream in) throws IOException, PackageNotFoundException {
        String packageID = readString(in);
        long date = in.readLong();
        Package p = findPackage(packageID);
        p.setPickUpTime(new Date(date));
        log.info("SetPickUpTime: Success. Received pick up time for package #" + p.getBarcode());
    }

    protected void addPaymentRecord(DataInputStream in) throws IOException, PackageNotFoundException {
        String packageID = readString(in);
        String octopusID = readString(in);
        double amount = in.readDouble();
        Package p = findPackage(packageID);
        p.addPayment(new Payment(octopusID, amount));
        log.info("AddPaymentRecord: Add payment record to package #" + p.getBarcode());
    }

    protected Package findPackage(String barcode) throws PackageNotFoundException {
        synchronized (packages) {
            Package p = packages.stream().filter(aPackage -> aPackage.equals(barcode)).findFirst().orElse(null);
            if (p != null) return p;
            throw new PackageNotFoundException("Package #" + barcode + " is not found.");
        }
    }

    protected Locker findLocker(String id) throws LockerException {
        synchronized (lockers) {
            Locker l = lockers.stream().filter(locker -> locker.equals(id)).findFirst().orElse(null);
            if (l != null) return l;
            throw new LockerException("Locker #" + id + " is not found.");
        }
    }

    private void loadPackageData() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("Server_package.db"));
            long total = in.readLong();
            for (long i = 0; i < total; i++) {
                Package p = (Package) in.readObject();
                packages.add(p);
            }
            log.info("Initialize package data complete.");
        } catch (FileNotFoundException e) {
            log.info("Initialize server without package data.");
        } catch (IOException | ClassNotFoundException e) {
            log.warning("Initialize package data error.");
        }
    }

    private void loadLockerData() {
        int total = Integer.parseInt(appKickstarter.getProperty("Locker.Total"));
        for (int i = 1; i <= total; i++) {
            String lockerID = appKickstarter.getProperty("Locker.Locker" + i + ".ID").trim();
            int largeLocker = Integer.parseInt(appKickstarter.getProperty("Locker.Locker" + i + ".Large"));
            int mediumLocker = Integer.parseInt(appKickstarter.getProperty("Locker.Locker" + i + ".Medium"));
            int smallLocker = Integer.parseInt(appKickstarter.getProperty("Locker.Locker" + i + ".Small"));
            lockers.add(new Locker(lockerID, largeLocker, mediumLocker, smallLocker));
        }
    }

    protected void receiveDiagnostic(DataInputStream in) throws IOException{
        String data = readString(in);
        JSONObject diagnostic = new JSONObject(data);
        System.out.println(diagnostic);
        JSONObject brReader = diagnostic.getJSONObject("Barcode Reader Driver");
        System.out.println(brReader);
        System.out.println(brReader.getString("Version"));
    }

    private void receiveReportFail(DataInputStream in, String lockerID) throws IOException {
        log.info("Receive fail from locker #" + lockerID + ": " + readString(in));
    }

    protected boolean removePackage(String barcode) throws PackageNotFoundException {
        Package p = findPackage(barcode);
        if (p.isArrive() && !p.isPickUp())
            return false;
        packages.remove(p);
        return true;
    }

    protected String readString(DataInputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        String s = "";
        while (len > 0) {
            int readLength = Math.min(len, 1024);
            in.read(buffer, 0, readLength);
            s += new String(buffer, 0, readLength);
            len -= readLength;
        }
        return s;
    }

    protected void sendString(DataOutputStream out, String str) throws IOException {
        int len = str.length();
        out.writeInt(len);
        out.write(str.getBytes(StandardCharsets.UTF_8), 0, len);
    }

    private void stopSvr() {
        log.info(id + ": terminating...");
        try {
            slSvrSocket.close();
        } catch (IOException e) {
            log.warning("Server socket is closed.");
        }
    }

    private void shutDown() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("server_package.db"));
            out.writeLong(packages.size());
            packages.forEach(p -> {
                try {
                    out.writeObject(p);
                } catch (IOException e) {
                    log.warning("Package " + p.getBarcode() + " backup fail.");
                }
            });
        } catch (IOException e) {
            log.warning("Package Info backup fail.");
        }
        System.exit(0);
    }

    public static class PackageNotFoundException extends Exception {
        public PackageNotFoundException() {
        }

        public PackageNotFoundException(String message) {
            super(message);
        }
    }

    public static class LockerException extends Exception {
        public LockerException() {
        }

        public LockerException(String message) {
            super(message);
        }
    }
}