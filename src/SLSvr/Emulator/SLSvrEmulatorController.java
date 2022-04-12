package SLSvr.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;

import java.util.logging.Logger;

import Common.LockerSize;
import SLSvr.SLSvr.Package;
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
    public TextField barcodeField;
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
                barcodeField.setText("");
                lockerIDField.setText("");
                lockerSizeCBox.setValue(LockerSize.Large.toString());
                break;

            case "Add/Edit Package":
                addEditPackage();
                break;

            case "Find Package":
                findPackageAction();
                break;

            case "Remove Package":
                removePackage();
                break;

            case "Request Diagnostic":
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
        barcodeField.setText(appKickstarter.getProperty(propertyName + "ID"));
        lockerIDField.setText(appKickstarter.getProperty(propertyName + "LockerID"));
        lockerSizeCBox.setValue(appKickstarter.getProperty(propertyName + "Size"));
    }

    private Package findPackage(String barcode) {
        try {
            return slSvrEmulator.findPackage(barcode);
        } catch (SLSvr.PackageNotFoundException e) {
            return null;
        }
    }

    private void findPackageAction() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty())
            return;
        Package p = findPackage(barcode);
        if (p != null) {
            barcodeField.setText(p.getBarcode());
            lockerIDField.setText(p.getLockerID());
            lockerSizeCBox.setValue(p.getSize().toString());
            appendTextArea(p.toString());
        } else {
            appendTextArea("Package #" + barcode + " is not found.");
        }
    }

    private void addEditPackage() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty())
            return;
        String lockerID = lockerIDField.getText().trim();
        LockerSize size = LockerSize.valueOf(lockerSizeCBox.getValue());
        Package p = findPackage(barcode);
        if (p != null) {
            appendTextArea("Editing package #" + barcode + " information...");
            p.setLockerID(lockerID);
            p.setSize(size);
            appendTextArea("Edit package #" + barcode + " successfully.");
        } else {
            appendTextArea("Adding package #" + barcode + " information...");
            try {
                slSvrEmulator.addPackage(barcode, lockerID, size);
                appendTextArea("Added package #" + barcode + " to locker #" + lockerID + ".");
            } catch (SLSvr.LockerException e) {
                appendTextArea(e.getMessage());
            }
        }
    }

    private void removePackage(){
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty())
            return;
        try {
            if (slSvrEmulator.removePackage(barcode))
                appendTextArea("Package #" + barcode + " is removed.");
            else
                appendTextArea("Package #" + barcode + " cannot remove now.");
        } catch (SLSvr.PackageNotFoundException e) {
            appendTextArea(e.getMessage());
        }
    }

    //------------------------------------------------------------
    // appendTextArea
    public void appendTextArea(String status) {
        slSvrTextArea.appendText(status + "\n");
    } // appendTextArea
} // BarcodeReaderEmulatorController
