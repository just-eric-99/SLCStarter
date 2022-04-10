package SLSvr.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;

import java.util.logging.Logger;

import Common.LockerSize;
import SLSvr.SLSvr.SLSvr;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


//======================================================================
// BarcodeReaderEmulatorController
public class SLSvrEmulatorController {
    private String id;
    private AppKickstarter appKickstarter;
    private Logger log;
    private SLSvrEmulator slSvrEmulator;
    private MBox slSvrMBox;
    private String pollResp;
    public TextField packageIDField;
    public TextField lockerIDField;
    public TextArea slSvrTextArea;
    public ChoiceBox<String> pollRespCBox;
    public ChoiceBox<String> lockerSizeCBox;


    //------------------------------------------------------------
    // initialize
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, SLSvrEmulator sLSvrEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.slSvrEmulator = sLSvrEmulator;
        this.slSvrMBox = appKickstarter.getThread("SLSvr").getMBox();
        this.pollRespCBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pollResp = pollRespCBox.getItems().get(newValue.intValue()).toString();
                appendTextArea("Poll Response set to " + pollRespCBox.getItems().get(newValue.intValue()).toString());
            }
        });
        this.pollResp = pollRespCBox.getValue().toString();
    } // initialize


    //------------------------------------------------------------
    // buttonPressed
    public void buttonPressed(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();

        switch (btn.getText()) {
            case "Package 1":
                loadSamplePackageField(1);
                break;

            case "Package 2":
                loadSamplePackageField(2);
                break;

            case "Package 3":
                loadSamplePackageField(3);
                break;

            case "Package 4":
                loadSamplePackageField(4);
                break;

            case "Reset":
                packageIDField.setText("");
                lockerIDField.setText("");
                lockerSizeCBox.setValue(LockerSize.Large.toString());
                break;

            case "Add Package":
                addPackage();
                break;

            case "Activate/Standby":
                slSvrMBox.send(new Msg(id, slSvrMBox, Msg.Type.BR_GoActive, packageIDField.getText()));
                slSvrTextArea.appendText("Removing card\n");
                break;

            case "Diagnostic":
                slSvrEmulator.requestDiagnostic();
                break;

            default:
                log.warning(id + ": unknown button: [" + btn.getText() + "]");
                break;
        }
    } // buttonPressed


    //------------------------------------------------------------
    // getters
    public String getPollResp() {
        return pollResp;
    }

    private void loadSamplePackageField(int num) {
        String propertyName = "Package.Package" + num + ".";
        packageIDField.setText(appKickstarter.getProperty(propertyName + "ID"));
        lockerIDField.setText(appKickstarter.getProperty(propertyName + "LockerID"));
        lockerSizeCBox.setValue(appKickstarter.getProperty(propertyName + "Size"));
    }

    private void addPackage() {
        appendTextArea("Adding package #" + packageIDField.getText() + " information...");
        String barcode = packageIDField.getText().trim();
        String lockerID = lockerIDField.getText().trim();
        LockerSize size = LockerSize.valueOf(lockerSizeCBox.getValue());
        try {
            slSvrEmulator.addPackage(barcode, lockerID, size);
            appendTextArea("Add package #" + barcode + " to locker #" + lockerID + ".");
        } catch (SLSvr.LockerException e) {
            appendTextArea(e.getMessage());
        }
    }

    private void removePackage(){

    }

    //------------------------------------------------------------
    // appendTextArea
    public void appendTextArea(String status) {
        slSvrTextArea.appendText(status + "\n");
    } // appendTextArea
} // BarcodeReaderEmulatorController
