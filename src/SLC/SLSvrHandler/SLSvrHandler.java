package SLC.SLSvrHandler;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


//======================================================================
// SLSvrHandler
public class SLSvrHandler extends HWHandler {
    private Socket slSvr;
    private DataInputStream in;
    private DataOutputStream out;

    //------------------------------------------------------------
    // SLSvrHandler
    public SLSvrHandler(String id, AppKickstarter appKickstarter) throws IOException {
        super(id, appKickstarter);
        String serverIP = appKickstarter.getProperty("Server.IP");
        int serverPort = Integer.parseInt(appKickstarter.getProperty("Server.Port"));
        slSvr = new Socket(serverIP, serverPort);
        in = new DataInputStream(slSvr.getInputStream());
        out = new DataOutputStream(slSvr.getOutputStream());
        sendString("HKGLK01");  // TODO change to get the id from cfg
        new Thread(() -> {
            try {
                receive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    } // SLSvrHandler

    @Override
    public void run() {
        super.run();
        stopClient();
    }

    //------------------------------------------------------------
    // processMsg
    protected void processMsg(Msg msg) {
        try {
            switch (msg.getType()) {
                case SLS_VerifyBarcode:
                    verifyBarcode(msg);
                    break;

                case SLS_SendPasscode:
                    sendPasscode(msg);
                    break;

                case SLS_Fee:
                    requestFee(msg);
                    break;

                case SLS_PackageArrived:
                case SLS_PackagePicked:
                    sendTimeStr(msg);
                    break;

                case SLS_Payment:
                    sendPayment(msg);
                    break;

                default:
                    log.warning(id + ": unknown message type: [" + msg + "]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // processMsg

    private void receive() throws IOException {
        while (true) {
            Msg.Type type = Msg.Type.values()[in.readInt()];
            System.out.println(type);
            switch (type) {
                case PollAck:
                    slc.send(new Msg(id, mbox, type, id + " is up!"));
                    break;

                case PollNak:
                    slc.send(new Msg(id, mbox, type, id + " is down!"));
                    break;

                case SLS_BarcodeVerified:
                case SLS_InvalidBarcode:
                    slc.send(new Msg(id, mbox, type, readString()));
                    break;

                case SLS_Fee:
                    slc.send(new Msg(id, mbox, type, in.readDouble() + ""));
                    break;

                case SLS_TimeInterval:
                    slc.send(new Msg(id, mbox, type, in.readInt() + ""));
                    break;

                default:
                    log.warning(id + ": unknown message type: [" + type + "]");
            }
        }
    }


    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() {
        try {
            out.writeInt(Msg.Type.Poll.ordinal());
        } catch (IOException e) {
            slc.send(new Msg(id, mbox, Msg.Type.PollNak, id + " is down!"));
        }
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

    private void requestFee(Msg msg) throws IOException {
        out.writeInt(msg.getType().ordinal());
        sendString(msg.getDetails());
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

    private String readString() throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        in.read(buffer, 0, len);
        return new String(buffer, 0, len);
    }

    private void sendString(String str) throws IOException {
        int len = str.length();
        out.writeInt(len);
        out.write(str.getBytes(StandardCharsets.UTF_8), 0, len);
    }

    private void stopClient() {
        log.info(id + ": terminating...");
        try {
            slSvr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} // SLSvrHandler
