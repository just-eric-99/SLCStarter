package SLC.HWHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;


//======================================================================
// HWHandler
public abstract class HWHandler extends AppThread {
    protected MBox slc = null;

    //------------------------------------------------------------
    // HWHandler
    public HWHandler(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
    } // HWHandler


    //------------------------------------------------------------
    // run
    public void run() {
        slc = appKickstarter.getThread("SLC").getMBox();
        log.info(id + ": starting...");

        for (boolean quit = false; !quit;) {
            Msg msg = mbox.receive();

            log.fine(id + ": message received: [" + msg + "].");

            switch (msg.getType()) {
                case Poll:
                    handlePoll();
                    break;

                case Terminate:
                    quit = true;
                    break;

                default:
                    processMsg(msg);
            }
        }

        // declaring our departure
        appKickstarter.unregThread(this);
        log.info(id + ": terminating...");
    } // run


    //------------------------------------------------------------
    // abstract methods
    protected abstract void processMsg(Msg msg);
    protected abstract void handlePoll();
} // HWHandler
