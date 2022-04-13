package SLSvr.Emulator;

import AppKickstarter.misc.Msg;
import Common.LockerSize;
import SLSvr.SLSvr.Package;
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
    protected void handlePoll(String detail) throws IOException, LockerException {
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
    protected void addPackage(String barcode, String lockerID, LockerSize size) throws LockerException {
        super.addPackage(barcode, lockerID, size);
    }

    @Override
    public void requestDiagnostic() {
        super.requestDiagnostic();
    }

    @Override
    public Package findPackage(String barcode) throws PackageNotFoundException {
        return super.findPackage(barcode);
    }

    @Override
    public boolean removePackage(String barcode) throws PackageNotFoundException {
        return super.removePackage(barcode);
    }
} // SLSvrEmulator
