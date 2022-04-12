package SLC.SLSvrHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;


//======================================================================
// SLSvrHandler
public class SLSvrHandler extends AppThread {
    private MBox slc = null;
    private String serverIP;
    private int serverPort;
    private String locationID;

    private Socket slSvr;
    private DataInputStream in;
    private DataOutputStream out;

    private Thread receiveThread;

    private boolean connection = false;
    private boolean quit = false;

    //------------------------------------------------------------
    // SLSvrHandler
    public SLSvrHandler(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
        serverIP = appKickstarter.getProperty("Server.IP");
        serverPort = Integer.parseInt(appKickstarter.getProperty("Server.Port"));
        locationID = appKickstarter.getProperty("Locker.Location");
        receiveThread = new Thread(this::receive);
    } // SLSvrHandler

    @Override
    public void run() {
        slc = appKickstarter.getThread("SLC").getMBox();
        startClient();
        receiveThread.start();

        while (!quit) {
            Msg msg = mbox.receive();

            log.fine(id + ": message received: [" + msg + "].");

            switch (msg.getType()) {
                case SLS_Reconnect:
                    startClient();
                    break;

                case SLS_RqDiagnostic:
                    sendDiagnosticToSLC();
                    break;

                case Terminate:
                    quit = true;
                    break;

                default:
                    if (slSvr != null && slSvr.isConnected())
                        processMsg(msg);
            }
        }
        stopClient();
    }

    //------------------------------------------------------------
    // processMsg
    protected void processMsg(Msg msg) {
        try {
            System.out.println("SLSvrHandler: Process Msg, " + msg.getType());
            switch (msg.getType()) {
                case Poll:
                    handlePoll();
                    break;

                case SLS_VerifyBarcode:
                    verifyBarcode(msg);
                    break;

                case SLS_SendPasscode:
                    sendPasscode(msg);
                    break;

                case SLS_PackageArrived:
                case SLS_PackagePicked:
                    sendTimeStr(msg);
                    break;

                case SLS_Payment:
                    sendPayment(msg);
                    break;

                case SLS_SendDiagnostic:
                    sendDiagnosticToServer(msg);
                    break;

                case SLS_ReportFail:
                    reportFail(msg);
                    break;

                default:
                    log.warning(id + ": unknown message type: [" + msg + "]");
            }
        } catch (IOException e) {
            sendDisconnectedMsg();
            slc.send(new Msg(id, mbox, Msg.Type.SLS_FailMsg, msg.getType() + "%" + msg.getDetails()));
        }
    } // processMsg

    private void receive() {
        while (!quit) {
            try {
                if (slSvr == null || !slSvr.isConnected())
                    throw new IOException();

                Msg.Type type = Msg.Type.values()[in.readInt()];
                switch (type) {
                    case PollAck:
                        slc.send(new Msg(id, mbox, type, id + " is up!"));
                        break;

                    case PollNak:
                        slc.send(new Msg(id, mbox, type, id + " is down!"));
                        break;

                    case SLS_RqDiagnostic:
                        slc.send(new Msg(id, mbox, type, ""));
                        break;

                    case SLS_BarcodeVerified:
                        slc.send(new Msg(id, mbox, type, readString() + "\t" + readString()));
                        break;

                    case SLS_InvalidBarcode:
                        slc.send(new Msg(id, mbox, type, readString()));
                        break;

                    default:
                        log.warning(id + ": unknown message type: [" + type + "]");
                }
            } catch (IOException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() throws IOException {
        out.writeInt(Msg.Type.Poll.ordinal());
    } // handlePoll

    private void verifyBarcode(Msg msg) throws IOException {
        out.writeInt(msg.getType().ordinal());
        sendString(msg.getDetails().trim());
    }

    private void sendPasscode(Msg msg) throws IOException {
        String[] tokens = msg.getDetails().split("\t");
        out.writeInt(msg.getType().ordinal());
        sendString(tokens[0]);
        out.writeInt(Integer.parseInt(tokens[1]));
    }

    private void sendTimeStr(Msg msg) throws IOException {
        String[] tokens = msg.getDetails().split("\t");
        out.writeInt(msg.getType().ordinal());
        sendString(tokens[0]);
        out.writeLong(Long.parseLong(tokens[1]));
    }

    private void sendPayment(Msg msg) throws IOException {
        String[] tokens = msg.getDetails().split("\t");
        out.writeInt(msg.getType().ordinal());
        sendString(tokens[0]);
        sendString(tokens[1]);
        out.writeDouble(Double.parseDouble(tokens[2]));
    }

    private void sendDiagnosticToSLC() {
        Map<String, Object> information = new LinkedHashMap<>();

        information.put("Name", appKickstarter.getProperty("SLSvr.Handler.Name"));
        information.put("Version", appKickstarter.getProperty("SLSvr.Handler.Version"));
        information.put("IP Address", slSvr.getLocalAddress().getHostAddress());
        information.put("Port", slSvr.getLocalPort() + "");
        information.put("Connection", connection? "Stable" : "Disconnected");
        information.put("Retrieval time", System.currentTimeMillis() + "");

        String data = new JSONObject(information).toString();
        slc.send(new Msg(id, mbox, Msg.Type.SH_RpDiagnostic, data));
    }

    private void sendDiagnosticToServer(Msg msg) throws IOException {
        out.writeInt(msg.getType().ordinal());
        sendString(msg.getDetails());
    }

    private void reportFail(Msg msg) throws IOException {
        out.writeInt(msg.getType().ordinal());
        sendString(msg.getDetails().trim());
    }

    private String readString() throws IOException {
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

    private void sendString(String str) throws IOException {
        int len = str.length();
        out.writeInt(len);
        out.write(str.getBytes(StandardCharsets.UTF_8), 0, len);
    }

    private void sendDisconnectedMsg() {
        connection = false;
        slc.send(new Msg(id, mbox, Msg.Type.SLS_ConnectionFail, id + " is disconnected!"));
    }

    private synchronized void startClient() {
        try {
            slSvr = new Socket(serverIP, serverPort);
            in = new DataInputStream(slSvr.getInputStream());
            out = new DataOutputStream(slSvr.getOutputStream());
            sendString(locationID);
            slc.send(new Msg(id, mbox, Msg.Type.SLS_Connected, ""));
            connection = true;
        } catch (IOException e) {
            sendDisconnectedMsg();
        }
    }

    private void stopClient() {
        log.info(id + ": terminating...");
        try {
            if (slSvr != null)
                slSvr.close();
        } catch (IOException ignored) {}
    }
} // SLSvrHandler
