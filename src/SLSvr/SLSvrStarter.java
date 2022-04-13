package SLSvr;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.Msg;
import AppKickstarter.timer.Timer;
import SLSvr.SLSvr.SLSvr;
import javafx.application.Platform;

public class SLSvrStarter extends AppKickstarter {
    protected Timer timer;
    protected SLSvr slSvr;

    public static void main(String[] args) {
        new SLSvrStarter().startApp();
    }

    public SLSvrStarter() {
        super("SLSvrStarter", "etc/SLSvr.cfg");
    }

    @Override
    protected void startApp() {
        // start our application
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Starting...");

        startSvr();
    }

    protected void startSvr() {
        try {
            timer = new Timer("timer", this);
            slSvr = new SLSvr("SLSvr", this);
        } catch (Exception e) {
            System.out.println("AppKickstarter: startApp failed");
            e.printStackTrace();
            Platform.exit();
        }

        // start threads
        new Thread(timer).start();
        new Thread(slSvr).start();
    }

    @Override
    public void stopApp() {
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Stopping...");
        slSvr.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
    }
}
