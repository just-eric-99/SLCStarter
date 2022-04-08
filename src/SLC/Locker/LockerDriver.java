package SLC.Locker;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.Msg;
import SLC.HWHandler.HWHandler;

import java.util.ArrayList;

public class LockerDriver extends HWHandler {
    private final int n = Integer.parseInt(appKickstarter.getProperty("locker.Count"));

    protected final static ArrayList<Locker> lockers = new ArrayList<>();

    public LockerDriver(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
        initialize();
    }

    private void initialize() {
        if (lockers.size() == 0) {
            loadData();
        }
    }

    private void loadData() {
        for (int i = 0; i < n; i++) {
            String nameInProperty = "Locker.LockerId"+ i;
            String occupiedInProperty = "Locker.LockerId"+ i +".Occupied";

            String name = appKickstarter.getProperty(nameInProperty);
            boolean occupied = (Integer.parseInt(appKickstarter.getProperty(occupiedInProperty))) == 1;

            lockers.add(new Locker(name, occupied));
        }
    }

    @Override
    protected void processMsg(Msg msg) {
        // slc will send the message to lock or unlock
        switch (msg.getType()) {

            case L_Opened:
                handleOpened();
                break;

            case TimesUp:
                break;

            case L_Unlock:

                handleUnlock(msg.getDetails());
                break;

            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
    }

    public static ArrayList<Locker> getLockers() {
        return lockers;
    }

    @Override
    protected void handlePoll() {
        log.info(id + ": Handle Poll");
    }

    protected void handleUnlock(String locker) {
        int lockerId = Integer.parseInt(locker);

        if (lockers.get(lockerId).isOpened()) {
            log.info("Locker #" + lockers.get(lockerId).getLockerId() + " is already opened.");
        } else {
            lockers.get(lockerId).setOpened(true);
            log.info("Locker #" + lockers.get(lockerId).getLockerId() + " is opened.");
            log.info("Locker #" + lockers.get(lockerId).getLockerId() + " is available now.");
        }
    }

    protected void handleOpened() {
        String lockerIds = "";
        if (!lockers.isEmpty()) {
            for (Locker locker : lockers) {
                if (locker.isOpened()) {
                    lockerIds += locker.getLockerId() + ",";
                }
            }
            if (lockerIds.length() != 0) {
                lockerIds = lockerIds.substring(0, lockerIds.length() - 1);
            }
        }
        slc.send(new Msg(id, mbox, Msg.Type.L_Opened, lockerIds));
    }
}
