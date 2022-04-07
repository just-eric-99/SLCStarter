package SLSvr.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;

import java.util.logging.Logger;

import SLSvr.SLSvr.Package;
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
    public TextField feeField1;
    public TextField feeField2;
    public TextField durationField;
    public TextArea slSvrTextArea;
    public ChoiceBox pollRespCBox;


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
        this.feeField1.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*"))
                    feeField1.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        this.feeField2.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*"))
                    feeField2.setText(newValue.replaceAll("[^\\d]", ""));
                else if (feeField2.getText().length() > 2)
                    feeField2.setText(feeField2.getText(0, 2));
            }
        });
        this.durationField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*"))
                    durationField.setText(newValue.replaceAll("[^\\d]", ""));
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
                feeField1.setText("");
                feeField2.setText("");
                durationField.setText("");
                break;

            case "Add Package":
                addPackage();
                break;

            case "Activate/Standby":
                slSvrMBox.send(new Msg(id, slSvrMBox, Msg.Type.BR_GoActive, packageIDField.getText()));
                slSvrTextArea.appendText("Removing card\n");
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
        String[] feeTokens = appKickstarter.getProperty(propertyName + "Fee").split("\\.");
        feeField1.setText(feeTokens[0]);
        feeField2.setText(feeTokens[1]);
        durationField.setText(appKickstarter.getProperty(propertyName + "Duration"));
    }

    private void addPackage() {
        String detail = packageIDField.getText() + "\t" + lockerIDField.getText() + "\t";
        detail += (feeField1.getText().isEmpty() ? 0 : feeField1.getText()) + ".";
        detail += (feeField2.getText().isEmpty() ? 0 : feeField2.getText()) + "\t";
        detail += durationField.getText().isEmpty() ? Package.defaultDuration : durationField.getText();
        appendTextArea("Sending package #" + packageIDField.getText() + " information...");
        slSvrMBox.send(new Msg(id, slSvrMBox, Msg.Type.SLS_AddPackage, detail));
    }

    //------------------------------------------------------------
    // appendTextArea
    public void appendTextArea(String status) {
        slSvrTextArea.appendText(status + "\n");
    } // appendTextArea
} // BarcodeReaderEmulatorController
