package SLC.TouchDisplayHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import SLC.SLC.Screen;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.logging.Logger;


//======================================================================
// TouchDisplayEmulatorController
public class TouchDisplayEmulatorController {

    private String id;
    private AppKickstarter appKickstarter;
    private Logger log;
    private TouchDisplayEmulator touchDisplayEmulator;
    private MBox touchDisplayMBox;
    private String selectedScreen;
    private String pollResp;
    public ChoiceBox screenSwitcherCBox;
    public ChoiceBox pollRespCBox;
    public Rectangle homeRect;
    // Main Menu
    public Rectangle checkInRect;
    public Rectangle pickUpRect;
    // Enter Passcode/Admin Login
    public Rectangle numpad1Rect;
    public Rectangle numpad2Rect;
    public Rectangle numpad3Rect;
    public Rectangle numpad4Rect;
    public Rectangle numpad5Rect;
    public Rectangle numpad6Rect;
    public Rectangle numpad7Rect;
    public Rectangle numpad8Rect;
    public Rectangle numpad9Rect;
    public Rectangle numpad0Rect;
    public Rectangle numpadBkspaceRect;
    public Rectangle numpadClearRect;
    // Enter Passcode
    public TextArea passcodeTextArea;
    public Rectangle enterRect;
    public Text invalidPasscodeText;
    // Admin Login
    public TextField usernameTextField;
    public TextField passwordTextField;
    public Rectangle loginRect;
    public Text invalidMsgText;
    // Scan Barcode
    public Text invalidBarcodeText;
    public Text lockerFullText;
    // Payment
    public Text paymentText;
    // Payment Failed
    public Text insufficientAmtText;
    public Text readCardErrorText;
    // Sever Down
    public Text errorMsgText;
    // Show Locker/Locker Not Close
    public Rectangle locker0Rect;
    public Rectangle locker1Rect;
    public Rectangle locker2Rect;
    public Rectangle locker3Rect;
    public Rectangle locker4Rect;
    public Rectangle locker5Rect;
    public Rectangle locker6Rect;
    public Rectangle locker7Rect;
    public Rectangle locker8Rect;
    public Rectangle locker9Rect;
    public Rectangle locker10Rect;
    public Rectangle locker11Rect;
    public Rectangle locker12Rect;
    public Rectangle locker13Rect;
    public Rectangle locker14Rect;
    public Rectangle locker15Rect;
    public Rectangle locker16Rect;
    public Rectangle locker17Rect;
    public Rectangle locker18Rect;
    public Rectangle locker19Rect;
    public Rectangle locker20Rect;
    public Rectangle locker21Rect;
    public Rectangle locker22Rect;
    public Rectangle locker23Rect;
    public Rectangle locker24Rect;
    public Rectangle locker25Rect;
    public Rectangle locker26Rect;
    public Rectangle locker27Rect;
    public Rectangle locker28Rect;
    public Rectangle locker29Rect;
    public Rectangle locker30Rect;
    public Rectangle locker31Rect;
    public Rectangle locker32Rect;
    public Rectangle locker33Rect;
    public Rectangle locker34Rect;
    public Rectangle locker35Rect;
    public Rectangle locker36Rect;
    public Rectangle locker37Rect;
    public Rectangle locker38Rect;
    public Rectangle locker39Rect;
    public Rectangle locker40Rect;
    public Rectangle locker41Rect;
    public Rectangle locker42Rect;
    public Rectangle locker43Rect;

    //------------------------------------------------------------
    // initialize
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, TouchDisplayEmulator touchDisplayEmulator, String pollRespParam, String showMsg) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.touchDisplayEmulator = touchDisplayEmulator;
        this.touchDisplayMBox = appKickstarter.getThread("TouchDisplayHandler").getMBox();
        this.pollResp = pollRespParam;
        this.pollRespCBox.setValue(this.pollResp);
        this.pollRespCBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pollResp = pollRespCBox.getItems().get(newValue.intValue()).toString();
            }
        });

        this.screenSwitcherCBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                selectedScreen = screenSwitcherCBox.getItems().get(newValue.intValue()).toString();
                switch (selectedScreen) {
                    // case == selection list
                    case "Welcome Page":
                        touchDisplayEmulator.changeScreen(Screen.Welcome_Page);
                        break;

                    case "Main Menu":
                        touchDisplayEmulator.changeScreen(Screen.Main_Menu);
                        break;

                    case "Confirmation":
                        touchDisplayEmulator.changeScreen(Screen.Confirmation);
                        break;

                    case "Enter Passcode":
                        touchDisplayEmulator.changeScreen(Screen.Enter_Passcode);
                        break;

                    case "Payment":
                        touchDisplayEmulator.changeScreen(Screen.Payment);
                        break;

                    case "Payment Succeeded":
                        touchDisplayEmulator.changeScreen(Screen.Payment_Succeeded);
                        break;

                    case "Payment Failed":
                        touchDisplayEmulator.changeScreen(Screen.Payment_Failed);
                        break;

                    case "Scan Barcode":
                        touchDisplayEmulator.changeScreen(Screen.Scan_Barcode);
                        break;

                    case "Show Locker":
                        touchDisplayEmulator.changeScreen(Screen.Show_Locker);
                        break;

                    case "Locker Not Close":
                        touchDisplayEmulator.changeScreen(Screen.Locker_Not_Close);
                        break;

                    case "Server Down":
                        touchDisplayEmulator.changeScreen(Screen.Server_Down);
                        break;
                }
            }
        });

        // Enter Passcode
        if (passcodeTextArea != null) {
            if (showMsg.contains("Invalid passcode")) {
                invalidPasscodeText.setVisible(true);
            } else {
                passcodeTextArea.setText(showMsg);
            }
        }
        // Scan Barcode
        if (lockerFullText != null && invalidBarcodeText != null) {
            if (showMsg.contains("invalid")) {
                invalidBarcodeText.setVisible(true);
            } else if (showMsg.contains("full")) {
                lockerFullText.setVisible(true);
            }
        }
        // Payment
        if (paymentText != null)
            this.paymentText.setText(showMsg);
        // Payment Failed
        if (insufficientAmtText != null && readCardErrorText != null) {
            if (showMsg.contains("Insufficient Amount")) {
                insufficientAmtText.setVisible(true);
            } else if (showMsg.contains("Read Card Error")) {
                readCardErrorText.setVisible(true);
            }
        }
        // Show Locker/Locker Not Close
        if (locker0Rect != null) {
            System.out.println("showMsg in locker0Rect: " + showMsg);
            setDisplayLocker(showMsg);
        }
        // Sever Down
        if (errorMsgText != null) {
            errorMsgText.setText(showMsg);
        }
        this.selectedScreen = screenSwitcherCBox.getValue().toString();
    } // initialize


    //------------------------------------------------------------
    // getSelectedScreen
    public String getSelectedScreen() {
        return selectedScreen;
    } // getSelectedScreen


    //------------------------------------------------------------
    // getPollResp
    public String getPollResp() {
        return pollResp;
    } // getPollResp

    //------------------------------------------------------------
    // getMbox
    public MBox getMbox() {
        return touchDisplayMBox;
    }// getMbox


    //------------------------------------------------------------
    // td_mouseClick
    public void td_mouseClick(MouseEvent mouseEvent) {
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();

        log.fine(id + ": mouse clicked: -- (" + x + ", " + y + ")");
        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_MouseClicked, x + " " + y));
    } // td_mouseClick


    //------------------------------------------------------------
    // setDisplayLocker
    public void setDisplayLocker(String lockerId) {
        switch (lockerId) {
            case "0":
                locker0Rect.setFill(Color.RED);
                break;

            case "1":
                locker1Rect.setFill(Color.RED);
                break;

            case "2":
                locker2Rect.setFill(Color.RED);
                break;

            case "3":
                locker3Rect.setFill(Color.RED);
                break;

            case "4":
                locker4Rect.setFill(Color.RED);
                break;

            case "5":
                locker5Rect.setFill(Color.RED);
                break;

            case "6":
                locker6Rect.setFill(Color.RED);
                break;

            case "7":
                locker7Rect.setFill(Color.RED);
                break;

            case "8":
                locker8Rect.setFill(Color.RED);
                break;

            case "9":
                locker9Rect.setFill(Color.RED);
                break;

            case "10":
                locker10Rect.setFill(Color.RED);
                break;

            case "11":
                locker11Rect.setFill(Color.RED);
                break;

            case "12":
                locker12Rect.setFill(Color.RED);
                break;

            case "13":
                locker13Rect.setFill(Color.RED);
                break;

            case "14":
                locker14Rect.setFill(Color.RED);
                break;

            case "15":
                locker15Rect.setFill(Color.RED);
                break;

            case "16":
                locker16Rect.setFill(Color.RED);
                break;

            case "17":
                locker17Rect.setFill(Color.RED);
                break;

            case "18":
                locker18Rect.setFill(Color.RED);
                break;

            case "19":
                locker19Rect.setFill(Color.RED);
                break;

            case "20":
                locker20Rect.setFill(Color.RED);
                break;

            case "21":
                locker21Rect.setFill(Color.RED);
                break;

            case "22":
                locker22Rect.setFill(Color.RED);
                break;

            case "23":
                locker23Rect.setFill(Color.RED);
                break;

            case "24":
                locker24Rect.setFill(Color.RED);
                break;

            case "25":
                locker25Rect.setFill(Color.RED);
                break;

            case "26":
                locker26Rect.setFill(Color.RED);
                break;

            case "27":
                locker27Rect.setFill(Color.RED);
                break;

            case "28":
                locker28Rect.setFill(Color.RED);
                break;

            case "29":
                locker29Rect.setFill(Color.RED);
                break;

            case "30":
                locker30Rect.setFill(Color.RED);
                break;

            case "31":
                locker31Rect.setFill(Color.RED);
                break;

            case "32":
                locker32Rect.setFill(Color.RED);
                break;

            case "33":
                locker33Rect.setFill(Color.RED);
                break;

            case "34":
                locker34Rect.setFill(Color.RED);
                break;

            case "35":
                locker35Rect.setFill(Color.RED);
                break;

            case "36":
                locker36Rect.setFill(Color.RED);
                break;

            case "37":
                locker37Rect.setFill(Color.RED);
                break;

            case "38":
                locker38Rect.setFill(Color.RED);
                break;

            case "39":
                locker39Rect.setFill(Color.RED);
                break;

            case "40":
                locker40Rect.setFill(Color.RED);
                break;

            case "41":
                locker41Rect.setFill(Color.RED);
                break;

            case "42":
                locker42Rect.setFill(Color.RED);
                break;

            case "43":
                locker43Rect.setFill(Color.RED);
                break;

        }
    } // setDisplayLocker
} // TouchDisplayEmulatorController
