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
    // Enter Passcode
    public TextArea passcodeTextArea;
    public Rectangle enterRect;
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
    public Text invalidPasscodeText;
    // Payment
    public Text paymentText;
    private static String showMsg;
    // Show Locker/Locker Not Close
    public Text checkInText;
    public Text pickUpText;
    public static Rectangle locker0Rect;
    public static Rectangle locker1Rect;
    public static Rectangle locker2Rect;
    public static Rectangle locker3Rect;
    public static Rectangle locker4Rect;
    public static Rectangle locker5Rect;
    public static Rectangle locker6Rect;
    public static Rectangle locker7Rect;
    public static Rectangle locker8Rect;
    public static Rectangle locker9Rect;
    public static Rectangle locker10Rect;
    public static Rectangle locker11Rect;
    public static Rectangle locker12Rect;
    public static Rectangle locker13Rect;
    public static Rectangle locker14Rect;
    public static Rectangle locker15Rect;
    public static Rectangle locker16Rect;
    public static Rectangle locker17Rect;
    public static Rectangle locker18Rect;
    public static Rectangle locker19Rect;
    public static Rectangle locker20Rect;
    public static Rectangle locker21Rect;
    public static Rectangle locker22Rect;
    public static Rectangle locker23Rect;
    public static Rectangle locker24Rect;
    public static Rectangle locker25Rect;
    public static Rectangle locker26Rect;
    public static Rectangle locker27Rect;
    public static Rectangle locker28Rect;
    public static Rectangle locker29Rect;
    public static Rectangle locker30Rect;
    public static Rectangle locker31Rect;
    public static Rectangle locker32Rect;
    public static Rectangle locker33Rect;
    public static Rectangle locker34Rect;
    public static Rectangle locker35Rect;
    public static Rectangle locker36Rect;
    public static Rectangle locker37Rect;
    public static Rectangle locker38Rect;
    public static Rectangle locker39Rect;
    public static Rectangle locker40Rect;
    public static Rectangle locker41Rect;
    public static Rectangle locker42Rect;
    public static Rectangle locker43Rect;

//    public static ArrayList<Rectangle> lockers = null;

//    private TouchDisplayEmulatorController() {
//        if (lockers == null) {
//            lockers = new ArrayList<>();
//            for (int i = 0; i < 44; i++) {
//                lockers.add(new Rectangle());
//            }
//        }
//
//        // lockers.get(i).setColour = red;
//    }


    //------------------------------------------------------------
    // initialize
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, TouchDisplayEmulator touchDisplayEmulator, String pollRespParam) {
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

//        new TouchDisplayEmulatorController();

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
        if (paymentText != null)
            this.paymentText.setText(showMsg);
        if (passcodeTextArea != null)
            passcodeTextArea.setText(showMsg);
        this.selectedScreen = screenSwitcherCBox.getValue().toString();
        System.out.println("In: " + this);
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

    // fixme
    public MBox getMbox() {
        return touchDisplayMBox;
    }


    //------------------------------------------------------------
    // td_mouseClick
    public void td_mouseClick(MouseEvent mouseEvent) {
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();

        log.fine(id + ": mouse clicked: -- (" + x + ", " + y + ")");
        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_MouseClicked, x + " " + y));
    } // td_mouseClick

    public static void setShowMsg(String showMsg) {
        TouchDisplayEmulatorController.showMsg = showMsg;
    }

    public void setPaymentText(String amount) {
        paymentText.setText(amount);
    }

    public static void setDisplayLocker(String lockerId) {
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
    }
} // TouchDisplayEmulatorController
