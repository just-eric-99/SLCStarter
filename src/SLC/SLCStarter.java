package SLC;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.Msg;
import AppKickstarter.timer.Timer;

import SLC.SLC.SLC;
import SLC.BarcodeReaderDriver.BarcodeReaderDriver;
import SLC.TouchDisplayHandler.TouchDisplayHandler;

import javafx.application.Platform;


//======================================================================
// SLCStarter
public class SLCStarter extends AppKickstarter {
    protected Timer timer;
    protected SLC slc;
    protected BarcodeReaderDriver barcodeReaderDriver;
    protected TouchDisplayHandler touchDisplayHandler;


    //------------------------------------------------------------
    // main
    public static void main(String [] args) {
        new SLCStarter().startApp();
    } // main


    //------------------------------------------------------------
    // SLCStart
    public SLCStarter() {
	super("SLCStarter", "etc/SLC.cfg");
    } // SLCStart


    //------------------------------------------------------------
    // startApp
    protected void startApp() {
	// start our application
	log.info("");
	log.info("");
	log.info("============================================================");
	log.info(id + ": Application Starting...");

	startHandlers();
    } // startApp


    //------------------------------------------------------------
    // startHandlers
    protected void startHandlers() {
	// create handlers
	try {
	    timer = new Timer("timer", this);
	    slc = new SLC("SLC", this);
	    barcodeReaderDriver = new BarcodeReaderDriver("BarcodeReaderDriver", this);
	    touchDisplayHandler = new TouchDisplayHandler("TouchDisplayHandler", this);
	} catch (Exception e) {
	    System.out.println("AppKickstarter: startApp failed");
	    e.printStackTrace();
	    Platform.exit();
	}

	// start threads
	new Thread(timer).start();
	new Thread(slc).start();
	new Thread(barcodeReaderDriver).start();
	new Thread(touchDisplayHandler).start();
    } // startHandlers


    //------------------------------------------------------------
    // stopApp
    public void stopApp() {
	log.info("");
	log.info("");
	log.info("============================================================");
	log.info(id + ": Application Stopping...");
	slc.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
	barcodeReaderDriver.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
	touchDisplayHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
	timer.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
    } // stopApp
} // SLCStarter
