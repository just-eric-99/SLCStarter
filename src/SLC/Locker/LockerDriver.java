package SLC.Locker;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.Msg;
import SLC.HWHandler.HWHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class LockerDriver extends HWHandler {
    private final int n = Integer.parseInt(appKickstarter.getProperty("locker.Count"));

    public static HashMap<String, Boolean> lockers = new HashMap<>();

    public LockerDriver(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
        initialize();
    }

    private void initialize() {
        if (lockers.isEmpty()) {
            for (int i = 0; i < n; i++) {
                lockers.put(i + "", true);
            }
        }
    }

    @Override
    protected void processMsg(Msg msg) {
        // slc will send the message to lock or unlock
        switch (msg.getType()) {

            case TimesUp:
                break;

            case L_Unlock:
                handleUnlock(msg.getDetails());
                break;

            case L_HasClose:
                sendHasCloseMsg(msg.getDetails());

            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
    }

    public static HashMap<String, Boolean> getLockers() {
        return lockers;
    }

    @Override
    protected void handlePoll() {
        log.info(id + ": Handle Poll");
    }

    protected void handleUnlock(String locker) {
        if (lockers.get(locker) != null) {
            lockers.replace(locker, false);
            log.info("Locker #" + locker + " is opened.");
            slc.send(new Msg(id,mbox, Msg.Type.L_Opened, locker + ""));
        } else {
            log.info("Locker #" + locker + " not found.");
        }
    }

    protected void sendHasCloseMsg(String lockerId) {
        log.info(id + ": Locker #" + lockerId + " is  now close.");
    }


}
