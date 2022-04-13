package SLC;
import AppKickstarter.timer.Timer;

import SLC.Locker.Emulator.LockerEmulator;
import SLC.Locker.LockerDriver;
import SLC.SLC.SLC;
import SLC.BarcodeReaderDriver.BarcodeReaderDriver;
import SLC.BarcodeReaderDriver.Emulator.BarcodeReaderEmulator;
import SLC.SLSvrHandler.SLSvrHandler;
import SLC.TouchDisplayHandler.Emulator.TouchDisplayEmulator;
import SLC.TouchDisplayHandler.TouchDisplayHandler;
import SLC.OctopusCardReaderDriver.OctopusCardReaderDriver;
import SLC.OctopusCardReaderDriver.Emulator.OctopusCardReaderEmulator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


//======================================================================
// SLCEmulatorStarter
public class SLCEmulatorStarter extends SLCStarter {
    //------------------------------------------------------------
    // main
    public static void main(String[] args) {
        new SLCEmulatorStarter().startApp();
    } // main


    //------------------------------------------------------------
    // startHandlers
    @Override
    protected void startHandlers() {
        Emulators.slcEmulatorStarter = this;
        new Emulators().start();
    } // startHandlers


    //------------------------------------------------------------
    // Emulators
    public static class Emulators extends Application {
        private static SLCEmulatorStarter slcEmulatorStarter;

        //----------------------------------------
        // start
        public void start() {
            launch();
        } // start

        //----------------------------------------
        // start
        public void start(Stage primaryStage) {
            Timer timer = null;
            SLC slc = null;
            BarcodeReaderEmulator barcodeReaderEmulator = null;
            TouchDisplayEmulator touchDisplayEmulator = null;
            OctopusCardReaderEmulator octopusCardReaderEmulator = null;
            SLSvrHandler slSvrHandler = null;
            LockerEmulator lockerEmulator = null;

            // create emulators
            try {
                timer = new Timer("timer", slcEmulatorStarter);
                slc = new SLC("SLC", slcEmulatorStarter);
                barcodeReaderEmulator = new BarcodeReaderEmulator("BarcodeReaderDriver", slcEmulatorStarter);
                touchDisplayEmulator = new TouchDisplayEmulator("TouchDisplayHandler", slcEmulatorStarter);
                octopusCardReaderEmulator = new OctopusCardReaderEmulator("OctopusCardReaderDriver", slcEmulatorStarter);
                slSvrHandler = new SLSvrHandler("SLSvrHandler", slcEmulatorStarter);
                lockerEmulator = new LockerEmulator("LockerDriver", slcEmulatorStarter);

                // start emulator GUIs
                barcodeReaderEmulator.start();
                touchDisplayEmulator.start();
                lockerEmulator.start();
                octopusCardReaderEmulator.start();
            } catch (Exception e) {
                System.out.println("Emulators: start failed");
                e.printStackTrace();
                Platform.exit();
            }
            slcEmulatorStarter.setTimer(timer);
            slcEmulatorStarter.setSLC(slc);
            slcEmulatorStarter.setBarcodeReaderDriver(barcodeReaderEmulator);
            slcEmulatorStarter.setTouchDisplayHandler(touchDisplayEmulator);
            slcEmulatorStarter.setOctopusCardReaderDriver(octopusCardReaderEmulator);
            slcEmulatorStarter.setSLSvrHandler(slSvrHandler);
            slcEmulatorStarter.setLockerDriver(lockerEmulator);

            // start threads
            new Thread(timer).start();
            new Thread(slc).start();
            new Thread(barcodeReaderEmulator).start();
            new Thread(touchDisplayEmulator).start();
            new Thread(slSvrHandler).start();
            new Thread(lockerEmulator).start();
            new Thread(octopusCardReaderEmulator).start();
        } // start
    } // Emulators


    //------------------------------------------------------------
    //  setters
    private void setTimer(Timer timer) {
        this.timer = timer;
    }

    private void setSLC(SLC slc) {
        this.slc = slc;
    }

    private void setBarcodeReaderDriver(BarcodeReaderDriver barcodeReaderDriver) {
        this.barcodeReaderDriver = barcodeReaderDriver;
    }

    private void setTouchDisplayHandler(TouchDisplayHandler touchDisplayHandler) {
        this.touchDisplayHandler = touchDisplayHandler;
    }

    private void setSLSvrHandler(SLSvrHandler slSvrHandler) {
        this.slSvrHandler = slSvrHandler;
    }

    private void setLockerDriver(LockerDriver lockerDriver) {
        this.lockerDriver = lockerDriver;
    }

    private void setOctopusCardReaderDriver(OctopusCardReaderDriver octopusCardReaderDriver) {
        this.octopusCardReaderDriver = octopusCardReaderDriver;
    }

} // SLCEmulatorStarter
