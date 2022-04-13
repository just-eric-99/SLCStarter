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
    public TextField octopusCardAmountField1;
    public TextField octopusCardAmountField2;
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
        this.octopusCardNumField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*"))
                    octopusCardNumField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        this.octopusCardAmountField1.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*"))
                    octopusCardAmountField1.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        this.octopusCardAmountField2.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*"))
                    octopusCardAmountField2.setText(newValue.replaceAll("[^\\d]", ""));
                else if (octopusCardAmountField2.getText().length() > 2)
                    octopusCardAmountField2.setText(octopusCardAmountField2.getText(0, 2));
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
            case "Octopus 1":
                octopusCardNumField.setText(appKickstarter.getProperty("OctopusCardReader.Number1"));
                break;

            case "Octopus 2":
                octopusCardNumField.setText(appKickstarter.getProperty("OctopusCardReader.Number2"));
                break;

            case "Octopus 3":
                octopusCardNumField.setText(appKickstarter.getProperty("OctopusCardReader.Number3"));
                break;

            case "Reset":
                octopusCardNumField.setText("");
                octopusCardAmountField1.setText("");
                octopusCardAmountField2.setText("");
                break;

            case "Send Octopus":
                sendCard();
                break;

            case "Fail: Insufficient Amount":
                octopusCardReaderEmulator.handleCardFailed("Insufficient Amount");
                appendTextArea("Send card fail: Insufficient Amount");
                break;

            case "Fail: Read Card Error":
                octopusCardReaderEmulator.handleCardFailed("Read Card Error");
                appendTextArea("Send card fail: Read Card Error");
                break;

            case "Standby":
                octopusCardReaderMBox.send(new Msg(id, octopusCardReaderMBox, Msg.Type.OCR_GoStandby, "OCR is standby"));
                octopusCardReaderTextArea.appendText("Removing card\n");
                break;

            default:
                log.warning(id + ": unknown button: [" + btn.getText() + "]");
                break;
        }
    } // buttonPressed


    private void sendCard() {
        String cardID = octopusCardNumField.getText();
        String amount = octopusCardAmountField1.getText().isEmpty() ? "0" : octopusCardAmountField1.getText();
        amount += octopusCardAmountField2.getText().isEmpty() ? "" : ("." + octopusCardAmountField2.getText());

        if (cardID.isEmpty() || amount.isEmpty())
            return;

        appendTextArea("Send Octopus Card #" + octopusCardNumField.getText() + ", amount: " + amount);
        octopusCardReaderEmulator.handleCardOK(cardID, amount);
    }


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
    public void setSendAmountField(String amount) {
        amount = Double.parseDouble(amount) + "";
        String[] tokens = amount.split("\\.");
        octopusCardAmountField1.setText(tokens[0]);
        octopusCardAmountField2.setText(tokens[1]);
    } // setRequestAmount

    //------------------------------------------------------------
    // setRequestAmount
    public void setOctopusCardRequestAmountField(String amount) {
        octopusCardRequestAmountField.setText(amount);
    } // setRequestAmount
} // OctopusCardReaderEmulatorController