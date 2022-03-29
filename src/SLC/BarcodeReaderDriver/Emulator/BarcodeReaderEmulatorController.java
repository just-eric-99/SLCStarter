package SLC.BarcodeReaderDriver.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


//======================================================================
// BarcodeReaderEmulatorController
public class BarcodeReaderEmulatorController {
    private String id;
    private AppKickstarter appKickstarter;
    private Logger log;
    private BarcodeReaderEmulator barcodeReaderEmulator;
    private MBox barcodeReaderMBox;
    private String activationResp;
    private String standbyResp;
    private String pollResp;
    public TextField barcodeNumField;
    public TextField barcodeReaderStatusField;
    public TextArea barcodeReaderTextArea;
    public ChoiceBox standbyRespCBox;
    public ChoiceBox activationRespCBox;
    public ChoiceBox pollRespCBox;


    //------------------------------------------------------------
    // initialize
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, BarcodeReaderEmulator barcodeReaderEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
	this.log = log;
	this.barcodeReaderEmulator = barcodeReaderEmulator;
	this.barcodeReaderMBox = appKickstarter.getThread("BarcodeReaderDriver").getMBox();
        this.activationRespCBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                activationResp = activationRespCBox.getItems().get(newValue.intValue()).toString();
                appendTextArea("Activation Response set to " + activationRespCBox.getItems().get(newValue.intValue()).toString());
            }
        });
        this.standbyRespCBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                standbyResp = standbyRespCBox.getItems().get(newValue.intValue()).toString();
                appendTextArea("Standby Response set to " + standbyRespCBox.getItems().get(newValue.intValue()).toString());
            }
        });
	this.pollRespCBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pollResp = pollRespCBox.getItems().get(newValue.intValue()).toString();
                appendTextArea("Poll Response set to " + pollRespCBox.getItems().get(newValue.intValue()).toString());
            }
        });
        this.activationResp = activationRespCBox.getValue().toString();
        this.standbyResp = standbyRespCBox.getValue().toString();
        this.pollResp = pollRespCBox.getValue().toString();
        this.goStandby();
    } // initialize


    //------------------------------------------------------------
    // buttonPressed
    public void buttonPressed(ActionEvent actionEvent) {
	Button btn = (Button) actionEvent.getSource();

	switch (btn.getText()) {
	    case "Barcode 1":
	        barcodeNumField.setText(appKickstarter.getProperty("BarcodeReader.Barcode1"));
	        break;

	    case "Barcode 2":
		barcodeNumField.setText(appKickstarter.getProperty("BarcodeReader.Barcode2"));
		break;

	    case "Barcode 3":
		barcodeNumField.setText(appKickstarter.getProperty("BarcodeReader.Barcode3"));
		break;

	    case "Reset":
		barcodeNumField.setText("");
		break;

	    case "Send Barcode":
                barcodeReaderMBox.send(new Msg(id, barcodeReaderMBox, Msg.Type.BR_BarcodeRead, barcodeNumField.getText()));
                barcodeReaderTextArea.appendText("Sending barcode " + barcodeNumField.getText()+"\n");
		break;

	    case "Activate/Standby":
                barcodeReaderMBox.send(new Msg(id, barcodeReaderMBox, Msg.Type.BR_GoActive, barcodeNumField.getText()));
                barcodeReaderTextArea.appendText("Removing card\n");
		break;

	    default:
	        log.warning(id + ": unknown button: [" + btn.getText() + "]");
		break;
	}
    } // buttonPressed


    //------------------------------------------------------------
    // getters
    public String getActivationResp() { return activationResp; }
    public String getStandbyResp()    { return standbyResp; }
    public String getPollResp()       { return pollResp; }


    //------------------------------------------------------------
    // goActive
    public void goActive() {
        updateBarcodeReaderStatus("Active");
    } // goActive


    //------------------------------------------------------------
    // goStandby
    public void goStandby() {
        updateBarcodeReaderStatus("Standby");
    } // goStandby


    //------------------------------------------------------------
    // updateBarcodeReaderStatus
    private void updateBarcodeReaderStatus(String status) {
        barcodeReaderStatusField.setText(status);
    } // updateBarcodeReaderStatus


    //------------------------------------------------------------
    // appendTextArea
    public void appendTextArea(String status) {
	barcodeReaderTextArea.appendText(status+"\n");
    } // appendTextArea
} // BarcodeReaderEmulatorController
