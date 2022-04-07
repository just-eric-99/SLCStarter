package SLSvr.SLSvr;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.Msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SLSvr extends AppThread {
    private ServerSocket slSvrSocket;
    private final int port;

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
        loadLockerData();
    }

    @Override
    public void run() {
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
                System.out.println(id + ": message received: [" + msg + "].");
                log.fine(id + ": message received: [" + msg + "].");

                switch (msg.getType()) {
                    case Terminate:
                        quit = true;
                        break;

                    default:
                        new Thread(() -> {
                            try {
                                send(msg);
                            } catch (LockerNotFoundException e) {
                                log.warning(e.getMessage());
                            } catch (IOException e) {
                                log.warning(e.getMessage());
                            } catch (PackageNotFoundException e) {
                                log.warning(e.getMessage());
                            }
                        }).start();
                }
            }
            receiveSvr.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopSvr();
        appKickstarter.unregThread(this);
    }

    private void send(Msg msg) throws LockerNotFoundException, IOException, PackageNotFoundException {
        switch (msg.getType()) {
            case Poll:
                handlePoll(msg.getDetails());
                break;

            case SLS_AddPackage:
                addPackage(msg);
                break;

            case SLS_BarcodeVerified:
                sendVerified(msg);
                break;

            case SLS_InvalidBarcode:
                sendInvalid(msg);
                break;

            case SLS_Fee:
                sendPackageFee(msg);
                break;

            case SLS_TimeInterval:
                sendTimeInterval(msg);
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
                    synchronized (l.getID()) {
                        l.setSocket(cSocket);
                    }
                    serve(in, lockerID);
                } catch (IOException e) {
                    log.info(cSocket.getInetAddress().getHostAddress() + " is disconnected.");
                } catch (LockerNotFoundException e) {
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

    private void serve(DataInputStream in, String lockerID) throws IOException, LockerNotFoundException {
        while (true) {
            Msg.Type type = Msg.Type.values()[in.readInt()];
            System.out.println("Serve: " + type);
            try {
                switch (type) {
                    case Poll:
                        mbox.send(new Msg(id, mbox, Msg.Type.Poll, lockerID));
                        break;

                    case SLS_Fee:
                    case SLS_TimeInterval:
                        mbox.send(new Msg(id, mbox, type, lockerID + "\t" + readString(in)));
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

                    default:
                        log.warning(id + ": unknown message type: [" + type + "]");
                        break;
                }
            } catch (PackageNotFoundException e) {
                log.warning("SetPasscode: Fail. Package ID not found.");
                e.printStackTrace();
            }
        }
    }

    protected void handlePoll(String detail) throws IOException, LockerNotFoundException {
        Socket cSocket = findLocker(detail).getSocket();
        DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
        out.writeInt(Msg.Type.PollAck.ordinal());
    }

    protected void addPackage(Msg msg) throws LockerNotFoundException {
        String[] tokens = msg.getDetails().split("\t");
        try {
            findLocker(tokens[1]);
        } catch (LockerNotFoundException e) {
            throw new LockerNotFoundException("Add Package fail: " + e.getMessage());
        }
        Package p = new Package(tokens[0], tokens[1], Double.parseDouble(tokens[2]), Integer.parseInt(tokens[3]));
        packages.add(p);
        log.info("Add package success.");
    }

    private void sendPackageProperty(Msg msg, SendPackageListener sp) throws IOException, LockerNotFoundException, PackageNotFoundException {
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

    protected void sendVerified(Msg msg) throws IOException, LockerNotFoundException, PackageNotFoundException {
        try {
            sendPackageProperty(msg, (out, p) -> sendString(out, p.getBarcode()));
        } catch (IOException e) {
            throw new IOException("Send verified message fail: " + e.getMessage());
        } catch (LockerNotFoundException e) {
            throw new LockerNotFoundException("Send verified message fail: " + e.getMessage());
        }
    }

    protected void sendInvalid(Msg msg) throws LockerNotFoundException, IOException {
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

    protected void sendPackageFee(Msg msg) throws IOException, LockerNotFoundException, PackageNotFoundException {
        try {
            sendPackageProperty(msg, (out, p) -> out.writeDouble(p.getFee()));
        } catch (IOException e) {
            throw new IOException("Send barcode fail: " + e.getMessage());
        } catch (LockerNotFoundException e) {
            throw new LockerNotFoundException("Send barcode fail: " + e.getMessage());
        } catch (PackageNotFoundException e) {
            throw new PackageNotFoundException("Send barcode fail: " + e.getMessage());
        }
    }

    protected void sendTimeInterval(Msg msg) throws IOException, LockerNotFoundException, PackageNotFoundException {
        try {
            sendPackageProperty(msg, (out, p) -> out.writeInt(p.getDuration()));
        } catch (IOException e) {
            throw new IOException("Send barcode fail: " + e.getMessage());
        } catch (LockerNotFoundException e) {
            throw new LockerNotFoundException("Send barcode fail: " + e.getMessage());
        } catch (PackageNotFoundException e) {
            throw new PackageNotFoundException("Send barcode fail: " + e.getMessage());
        }
    }

    protected void verifyBarcode(DataInputStream in, String lockerID) throws IOException {
        String barcode = readString(in);
        try {
            Package p = findPackage(barcode);
            mbox.send(new Msg(id, mbox, Msg.Type.SLS_BarcodeVerified, lockerID + "\t" + p.getBarcode()));
        } catch (PackageNotFoundException e) {
            mbox.send(new Msg(id, mbox, Msg.Type.SLS_InvalidBarcode, lockerID + "\t" + barcode));
        }
    }

    protected void setPasscode(DataInputStream in) throws IOException, PackageNotFoundException {
        String packageID = readString(in);
        int passcode = in.readInt();
        Package p = findPackage(packageID);
        p.setLockerPasscode(passcode);
        log.info("SetPasscode: Success. Received passcode for package #" + p.getBarcode());
    }

    protected void setArrivalTime(DataInputStream in, String lockerID) throws IOException, PackageNotFoundException {
        String packageID = readString(in);
        long date = in.readLong();
        Package p = findPackage(packageID);
        p.setArriveTime(new Date(date));
        log.info("SetArrivalTime: Success. Received arrival time for package #" + p.getBarcode());
        mbox.send(new Msg(id, mbox, Msg.Type.SLS_TimeInterval, lockerID + "\t" + p.getLockerID()));
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
            for (Package p : packages) {
                if (p.equals(barcode))
                    return p;
            }
        }
        throw new PackageNotFoundException("Package #" + barcode + " is not found.");
    }

    protected Locker findLocker(String id) throws LockerNotFoundException {
        synchronized (lockers) {
            for (Locker l : lockers) {
                if (l.equals(id))
                    return l;
            }
        }
        throw new LockerNotFoundException("Locker #" + id + " is not found.");
    }

    private void loadLockerData() {
        int total = Integer.parseInt(appKickstarter.getProperty("Locker.Total"));
        for (int i = 1; i <= total; i++) {
            String lockerID = appKickstarter.getProperty("Locker.Locker" + i + ".ID").trim();
            lockers.add(new Locker(lockerID));
        }
    }

    protected String readString(DataInputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        in.read(buffer, 0, len);
        return new String(buffer, 0, len);
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
            e.printStackTrace();
        }
    }

    protected static class PackageNotFoundException extends Exception {
        public PackageNotFoundException() {
        }

        public PackageNotFoundException(String message) {
            super(message);
        }
    }

    protected static class LockerNotFoundException extends Exception {
        public LockerNotFoundException() {
        }

        public LockerNotFoundException(String message) {
            super(message);
        }
    }
}