package SLC;

import AppKickstarter.timer.Timer;

import SLC.SLC.SLC;
import SLC.BarcodeReaderDriver.BarcodeReaderDriver;
import SLC.BarcodeReaderDriver.Emulator.BarcodeReaderEmulator;
import SLC.TouchDisplayHandler.Emulator.TouchDisplayEmulator;
import SLC.TouchDisplayHandler.TouchDisplayHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


//======================================================================
// SLCEmulatorStarter
public class SLCEmulatorStarter extends SLCStarter {
    //------------------------------------------------------------
    // main
    public static void main(String [] args) {
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

	    // create emulators
	    try {
	        timer = new Timer("timer", slcEmulatorStarter);
	        slc = new SLC("SLC", slcEmulatorStarter);
	        barcodeReaderEmulator = new BarcodeReaderEmulator("BarcodeReaderDriver", slcEmulatorStarter);
	        touchDisplayEmulator = new TouchDisplayEmulator("TouchDisplayHandler", slcEmulatorStarter);

		// start emulator GUIs
                barcodeReaderEmulator.start();
		touchDisplayEmulator.start();
	    } catch (Exception e) {
		System.out.println("Emulators: start failed");
		e.printStackTrace();
		Platform.exit();
	    }
	    slcEmulatorStarter.setTimer(timer);
	    slcEmulatorStarter.setSLC(slc);
	    slcEmulatorStarter.setBarcodeReaderDriver(barcodeReaderEmulator);
	    slcEmulatorStarter.setTouchDisplayHandler(touchDisplayEmulator);

	    // start threads
	    new Thread(timer).start();
	    new Thread(slc).start();
            new Thread(barcodeReaderEmulator).start();
	    new Thread(touchDisplayEmulator).start();
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
} // SLCEmulatorStarter
