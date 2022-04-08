package SLC.TouchDisplayHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    public Group groupGroup;
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
    private static double fee;
    // Show Locker/Locker Not Close
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
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "WelcomePage"));
                        break;

                    case "Main Menu":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "MainMenu"));
                        break;

                    case "Confirmation":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "Confirmation"));
                        break;

                    case "Enter Passcode":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "EnterPasscode"));
                        break;

                    case "Payment":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "Payment"));
                        break;

                    case "Payment Succeeded":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "PaymentSucceeded"));
                        break;

                    case "Payment Failed":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "PaymentFailed"));
                        break;

                    case "Scan Barcode":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "ScanBarcode"));
                        break;

                    case "Show Locker":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "ShowLocker"));
                        break;

                    case "Locker Not Close":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "LockerNotClose"));
                        break;

                    case "Server Down":
                        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "ServerDown"));
                        break;
                }
            }
        });
        if (paymentText != null) {
            this.paymentText.setText(fee + "");
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

    // fixme
    public MBox getMbox() {
        return touchDisplayMBox;
    }


    //------------------------------------------------------------
    // td_mouseClick
    public void td_mouseClick(MouseEvent mouseEvent) {
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();

        /**
         * Welcome Page
         * Main Menu
         * Enter Passcode
         * Payment
         * Payment Succeeded
         * Payment Failed
         * Show Locker
         * Scan Barcode
         * Locker Not Close
         * Server Down
         */

        switch (selectedScreen) {
            case "Welcome Page":
                handleWelcomePage();
                break;
            case "Main Menu":
                handleMainMenu(x, y);
                break;
            case "Enter Passcode":
                handleEnterPasscode(x, y);
                break;
            case "Scan Barcode":
                handleScanBarcode(x, y);
                break;
            case "Show Locker":
                locker0Rect.setFill(Color.RED);
                break;
//            case "Payment":
//                // fixme: start counting 4s after a click
//                Thread.sleep(4000);
//                touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "ShowLocker"));
//                break;
        }
        // todo: set rectangle color: enterRect.setFill(Color.RED);
        log.fine(id + ": mouse clicked: -- (" + x + ", " + y + ")");
        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_MouseClicked, x + " " + y));
    } // td_mouseClick

    private void handleWelcomePage() {
        touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "MainMenu"));
    }

    private void handleMainMenu(int x, int y) {
        double checkInXLeft = checkInRect.getLayoutX();
        double checkInXRight = checkInXLeft + checkInRect.getWidth();
        double checkInYTop = checkInRect.getLayoutY();
        double checkInYBottom = checkInYTop + checkInRect.getHeight();

        double pickUpXLeft = pickUpRect.getLayoutX();
        double pickUpXRight = pickUpXLeft + pickUpRect.getWidth();
        double pickUpYTop = pickUpRect.getLayoutY();
        double pickUpYBottom = pickUpYTop + pickUpRect.getHeight();

        if (x > checkInXLeft && x < checkInXRight && y > checkInYTop && y < checkInYBottom) {
//            touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "ScanBarcode"));
            touchDisplayEmulator.handleCheckIn(new Msg(id, touchDisplayMBox, Msg.Type.TD_GoCheckIn, ""));
        } else if (x > pickUpXLeft && x < pickUpXRight && y > pickUpYTop && y < pickUpYBottom) {
            touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "EnterPasscode"));
        }
    }

    private void handleEnterPasscode(int x, int y) {
        double homeXLeft = homeRect.getLayoutX() + groupGroup.getLayoutX();
        double homeXRight = homeXLeft + homeRect.getWidth();
        double homeYTop = homeRect.getLayoutY() + groupGroup.getLayoutY();
        double homeYBottom = homeYTop + homeRect.getHeight();

        double numpad1XLeft = numpad1Rect.getLayoutX();
        double numpad1XRight = numpad1XLeft + numpad1Rect.getWidth();
        double numpad1YTop = numpad1Rect.getLayoutY();
        double numpad1YBottom = numpad1YTop + numpad1Rect.getHeight();

        double numpad2XLeft = numpad2Rect.getLayoutX();
        double numpad2XRight = numpad2XLeft + numpad2Rect.getWidth();
        double numpad2YTop = numpad2Rect.getLayoutY();
        double numpad2YBottom = numpad2YTop + numpad2Rect.getHeight();

        double numpad3XLeft = numpad3Rect.getLayoutX();
        double numpad3XRight = numpad3XLeft + numpad3Rect.getWidth();
        double numpad3YTop = numpad3Rect.getLayoutY();
        double numpad3YBottom = numpad3YTop + numpad3Rect.getHeight();

        double numpad4XLeft = numpad4Rect.getLayoutX();
        double numpad4XRight = numpad4XLeft + numpad4Rect.getWidth();
        double numpad4YTop = numpad4Rect.getLayoutY();
        double numpad4YBottom = numpad4YTop + numpad4Rect.getHeight();

        double numpad5XLeft = numpad5Rect.getLayoutX();
        double numpad5XRight = numpad5XLeft + numpad5Rect.getWidth();
        double numpad5YTop = numpad5Rect.getLayoutY();
        double numpad5YBottom = numpad5YTop + numpad5Rect.getHeight();

        double numpad6XLeft = numpad6Rect.getLayoutX();
        double numpad6XRight = numpad6XLeft + numpad6Rect.getWidth();
        double numpad6YTop = numpad6Rect.getLayoutY();
        double numpad6YBottom = numpad6YTop + numpad6Rect.getHeight();

        double numpad7XLeft = numpad7Rect.getLayoutX();
        double numpad7XRight = numpad7XLeft + numpad7Rect.getWidth();
        double numpad7YTop = numpad7Rect.getLayoutY();
        double numpad7YBottom = numpad7YTop + numpad7Rect.getHeight();

        double numpad8XLeft = numpad8Rect.getLayoutX();
        double numpad8XRight = numpad8XLeft + numpad8Rect.getWidth();
        double numpad8YTop = numpad8Rect.getLayoutY();
        double numpad8YBottom = numpad8YTop + numpad8Rect.getHeight();

        double numpad9XLeft = numpad9Rect.getLayoutX();
        double numpad9XRight = numpad9XLeft + numpad9Rect.getWidth();
        double numpad9YTop = numpad9Rect.getLayoutY();
        double numpad9YBottom = numpad9YTop + numpad9Rect.getHeight();

        double numpad0XLeft = numpad0Rect.getLayoutX();
        double numpad0XRight = numpad0XLeft + numpad0Rect.getWidth();
        double numpad0YTop = numpad0Rect.getLayoutY();
        double numpad0YBottom = numpad0YTop + numpad0Rect.getHeight();

        double numpadBkspaceXLeft = numpadBkspaceRect.getLayoutX();
        double numpadBkspaceXRight = numpadBkspaceXLeft + numpadBkspaceRect.getWidth();
        double numpadBkspaceYTop = numpadBkspaceRect.getLayoutY();
        double numpadBkspaceYBottom = numpadBkspaceYTop + numpadBkspaceRect.getHeight();

        double numpadClearXLeft = numpadClearRect.getLayoutX();
        double numpadClearXRight = numpadClearXLeft + numpadClearRect.getWidth();
        double numpadClearYTop = numpadClearRect.getLayoutY();
        double numpadClearYBottom = numpadClearYTop + numpadClearRect.getHeight();

        double enterXLeft = enterRect.getLayoutX();
        double enterXRight = enterXLeft + enterRect.getWidth();
        double enterYTop = enterRect.getLayoutY();
        double enterYBottom = enterYTop + enterRect.getHeight();

        if (x > homeXLeft && x < homeXRight && y > homeYTop && y < homeYBottom) {
            touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "MainMenu"));
        } else if (x > numpad1XLeft && x < numpad1XRight && y > numpad1YTop && y < numpad1YBottom) {
            passcodeTextArea.appendText("1");
        } else if (x > numpad2XLeft && x < numpad2XRight && y > numpad2YTop && y < numpad2YBottom) {
            passcodeTextArea.appendText("2");
        } else if (x > numpad3XLeft && x < numpad3XRight && y > numpad3YTop && y < numpad3YBottom) {
            passcodeTextArea.appendText("3");
        } else if (x > numpad4XLeft && x < numpad4XRight && y > numpad4YTop && y < numpad4YBottom) {
            passcodeTextArea.appendText("4");
        } else if (x > numpad5XLeft && x < numpad5XRight && y > numpad5YTop && y < numpad5YBottom) {
            passcodeTextArea.appendText("5");
        } else if (x > numpad6XLeft && x < numpad6XRight && y > numpad6YTop && y < numpad6YBottom) {
            passcodeTextArea.appendText("6");
        } else if (x > numpad7XLeft && x < numpad7XRight && y > numpad7YTop && y < numpad7YBottom) {
            passcodeTextArea.appendText("7");
        } else if (x > numpad8XLeft && x < numpad8XRight && y > numpad8YTop && y < numpad8YBottom) {
            passcodeTextArea.appendText("8");
        } else if (x > numpad9XLeft && x < numpad9XRight && y > numpad9YTop && y < numpad9YBottom) {
            passcodeTextArea.appendText("9");
        } else if (x > numpad0XLeft && x < numpad0XRight && y > numpad0YTop && y < numpad0YBottom) {
            passcodeTextArea.appendText("0");
        } else if (x > numpadBkspaceXLeft && x < numpadBkspaceXRight && y > numpadBkspaceYTop && y < numpadBkspaceYBottom) {
            // Note: need to be editable for backspace
            passcodeTextArea.setEditable(true);
            passcodeTextArea.deletePreviousChar();
            passcodeTextArea.setEditable(false);
        } else if (x > numpadClearXLeft && x < numpadClearXRight && y > numpadClearYTop && y < numpadClearYBottom) {
            passcodeTextArea.clear();
        } else if (x > enterXLeft && x < enterXRight && y > enterYTop && y < enterYBottom) {
            String sendMsg = passcodeTextArea.getText();
            touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_SendPasscode, sendMsg));


        }
    }

    private void handleScanBarcode(int x, int y) {
        double homeXLeft = homeRect.getLayoutX() + groupGroup.getLayoutX();
        double homeXRight = homeXLeft + homeRect.getWidth();
        double homeYTop = homeRect.getLayoutY() + groupGroup.getLayoutY();
        double homeYBottom = homeYTop + homeRect.getHeight();


        if (x > homeXLeft && x < homeXRight && y > homeYTop && y < homeYBottom) {
            touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_UpdateDisplay, "MainMenu"));
        }
    }

    public void setPaymentText(Text paymentText) {
        this.paymentText = paymentText;
    }


    public static void setFee(double fee) {
        TouchDisplayEmulatorController.fee = fee;
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
