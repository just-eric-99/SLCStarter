package SLC.OctopusCardReaderDriver.Emulator;

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

public class OctopusCardReaderEmulatorController {
    private String id;
    private AppKickstarter appKickstarter;
    private Logger log;
    private OctopusCardReaderEmulator octopusCardReaderEmulator;
    private MBox octopusCardReaderMBox;
    private String activationResp;
    private String standbyResp;
    private String pollResp;
    public TextField octopusCardNumField;
    public static TextField octopusCardAmountField;
    public TextField octopusCardRequestAmountField;
    public TextField octopusCardReaderStatusField;
    public TextArea octopusCardReaderTextArea;
    public ChoiceBox standbyRespCBox;
    public ChoiceBox activationRespCBox;
    public ChoiceBox pollRespCBox;


    //------------------------------------------------------------
    // initialize
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, OctopusCardReaderEmulator octopusCardReaderEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.octopusCardReaderEmulator = octopusCardReaderEmulator;
        this.octopusCardReaderMBox = appKickstarter.getThread("OctopusCardReaderDriver").getMBox();
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

        String octopusDetails = octopusCardNumField.getText() + "\t" + octopusCardAmountField.getText();

        switch (btn.getText()) {
            case "Octopus 1":
                octopusCardNumField.setText(appKickstarter.getProperty("OctopusCardReader.Number1"));
                octopusCardAmountField.setText(appKickstarter.getProperty("OctopusCardReader.Number1.amount"));
                break;

            case "Octopus 2":
                octopusCardNumField.setText(appKickstarter.getProperty("OctopusCardReader.Number2"));
                octopusCardAmountField.setText(appKickstarter.getProperty("OctopusCardReader.Number2.amount"));
                break;

            case "Octopus 3":
                octopusCardNumField.setText(appKickstarter.getProperty("OctopusCardReader.Number3"));
                octopusCardAmountField.setText(appKickstarter.getProperty("OctopusCardReader.Number3.amount"));
                break;

            case "Reset":
                octopusCardNumField.setText("");
                octopusCardAmountField.setText("");
                break;

            case "Select Octopus":
                OctopusCardReaderEmulator.isCardChosen = true;
                octopusCardReaderTextArea.appendText("Selected Octopus Card " + octopusDetails + "\n");
                break;

            case "Activate/Standby":
                octopusCardReaderMBox.send(new Msg(id, octopusCardReaderMBox, Msg.Type.OCR_GoActive, octopusDetails));
                octopusCardReaderTextArea.appendText("Removing card\n");
                break;

            default:
                log.warning(id + ": unknown button: [" + btn.getText() + "]");
                break;
        }
    } // buttonPressed


    //------------------------------------------------------------
    // getters
    public String getActivationResp() {
        return activationResp;
    }

    public String getStandbyResp() {
        return standbyResp;
    }

    public String getPollResp() {
        return pollResp;
    }

    public static float getCardAmount(){return Float.parseFloat(String.valueOf(octopusCardAmountField)); }

    public static void setCardAmount(float amount){ octopusCardAmountField = new TextField(String.valueOf(amount));}


    //------------------------------------------------------------
    // goActive
    public void goActive() {
        updateOctopusCardReaderStatus("Active");
    } // goActive


    //------------------------------------------------------------
    // goStandby
    public void goStandby() {
        updateOctopusCardReaderStatus("Standby");
    } // goStandby

    //------------------------------------------------------------
    // cardFailed
    public void cardFailed() {
        updateOctopusCardReaderStatus("Card Failed");
    } // cardFailed

    //------------------------------------------------------------
    // cardOK
    public void cardOK() {
        updateOctopusCardReaderStatus("Card OK");
    } // cardOK


    //------------------------------------------------------------
    // updateOctopusCardReaderStatus
    private void updateOctopusCardReaderStatus(String status) {
        octopusCardReaderStatusField.setText(status);
    } // updateOctopusReaderStatus


    //------------------------------------------------------------
    // appendTextArea
    public void appendTextArea(String status) {
        octopusCardReaderTextArea.appendText(status + "\n");
    } // appendTextArea

    //------------------------------------------------------------
    // setRequestAmount
    public void setOctopusCardRequestAmountField(String amount) {
        octopusCardRequestAmountField.setText(amount);
    } // setRequestAmount
} // OctopusCardReaderEmulatorController