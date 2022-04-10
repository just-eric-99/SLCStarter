package SLSvr;

import AppKickstarter.timer.Timer;
import SLSvr.Emulator.SLSvrEmulator;
import SLSvr.SLSvr.SLSvr;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


//======================================================================
// SLCEmulatorStarter
public class SLSvrEmulatorStarter extends SLSvrStarter {
    //------------------------------------------------------------
    // main
    public static void main(String [] args) {
	new SLSvrEmulatorStarter().startApp();
    } // main


    //------------------------------------------------------------
    // startHandlers
    @Override
    protected void startSvr() {
        Emulators.slSvrEmulatorStarter = this;
        new Emulators().start();
    } // startHandlers


    //------------------------------------------------------------
    // Emulators
    public static class Emulators extends Application {
        private static SLSvrEmulatorStarter slSvrEmulatorStarter;

        //----------------------------------------
        // start
        public void start() {
                launch();
        } // start

        //----------------------------------------
        // start
        public void start(Stage primaryStage) {
            Timer timer = null;
            SLSvrEmulator slSvrEmulator = null;

            // create emulators
            try {
                timer = new Timer("timer", slSvrEmulatorStarter);
                slSvrEmulator = new SLSvrEmulator("SLSvr", slSvrEmulatorStarter);

                // start emulator GUIs
                slSvrEmulator.start();
            } catch (Exception e) {
                System.out.println("Emulators: start failed");
                e.printStackTrace();
                Platform.exit();
            }
            slSvrEmulatorStarter.setTimer(timer);
            slSvrEmulatorStarter.setSLSvr(slSvrEmulator);

            // start threads
            new Thread(timer).start();
            new Thread(slSvrEmulator).start();
        } // start
    } // Emulators


    //------------------------------------------------------------
    //  setters
    private void setTimer(Timer timer) {
        this.timer = timer;
    }

    private void setSLSvr(SLSvr slSvr){
        this.slSvr = slSvr;
    }
} // SLCEmulatorStarter
