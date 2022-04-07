package SLSvr.Emulator;

import AppKickstarter.misc.Msg;
import SLSvr.SLSvrStarter;

import SLSvr.SLSvr.SLSvr;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


//======================================================================
// SLSvrEmulator
public class SLSvrEmulator extends SLSvr {
    private SLSvrStarter slSvrStarter;
    private String id;
    private Stage myStage;
    private SLSvrEmulatorController slSvrEmulatorController;

    //------------------------------------------------------------
    // SLSvrEmulator
    public SLSvrEmulator(String id, SLSvrStarter slSvrStarter) {
        super(id, slSvrStarter);
        this.slSvrStarter = slSvrStarter;
        this.id = id;
    } // SLSvrEmulator


    //------------------------------------------------------------
    // start
    public void start() throws Exception {
        Parent root;
        myStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        String fxmlName = "SLSvrEmulator.fxml";
        loader.setLocation(SLSvrEmulator.class.getResource(fxmlName));
        root = loader.load();
        slSvrEmulatorController = (SLSvrEmulatorController) loader.getController();
        slSvrEmulatorController.initialize(id, slSvrStarter, log, this);
        myStage.initStyle(StageStyle.DECORATED);
        myStage.setScene(new Scene(root, 530, 560));
        myStage.setTitle("Smart Locker Server");
        myStage.setResizable(false);
        myStage.setOnCloseRequest((WindowEvent event) -> {
            slSvrStarter.stopApp();
            Platform.exit();
        });
        myStage.show();
    } // BarcodeReaderEmulator

    //------------------------------------------------------------
    // handlePoll
    @Override
    protected void handlePoll(String detail) throws IOException, LockerNotFoundException {
        Socket cSocket = findLocker(detail).getSocket();
        DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
        switch (slSvrEmulatorController.getPollResp()) {
            case "ACK":
                out.writeInt(Msg.Type.PollAck.ordinal());
                break;

            case "NAK":
                out.writeInt(Msg.Type.PollNak.ordinal());
                break;

            case "Ignore":
                // Just ignore.  do nothing!!
                break;
        }
    } // handlePoll

    @Override
    protected void addPackage(Msg msg) throws LockerNotFoundException {
        slSvrEmulatorController.appendTextArea("Adding package to server...");
        try {
            super.addPackage(msg);
            slSvrEmulatorController.appendTextArea("Add package success.");
        } catch (LockerNotFoundException e){
            slSvrEmulatorController.appendTextArea(e.getMessage());
            throw e;
        }
    }

    @Override
    protected void sendPackageFee(Msg msg) throws IOException, LockerNotFoundException, PackageNotFoundException {
        slSvrEmulatorController.appendTextArea("Sending fee to client...");
        try {
            super.sendPackageFee(msg);
            slSvrEmulatorController.appendTextArea("Send fee success.");
        } catch (IOException | LockerNotFoundException | PackageNotFoundException e) {
            slSvrEmulatorController.appendTextArea(e.getMessage());
            throw e;
        }
    }

    @Override
    protected void sendTimeInterval(Msg msg) throws IOException, LockerNotFoundException, PackageNotFoundException {
        slSvrEmulatorController.appendTextArea("Sending time interval to client...");
        try {
            super.sendTimeInterval(msg);
            slSvrEmulatorController.appendTextArea("Send time interval success.");
        } catch (IOException | LockerNotFoundException | PackageNotFoundException e) {
            slSvrEmulatorController.appendTextArea(e.getMessage());
            throw e;
        }
    }
} // BarcodeReaderEmulator
