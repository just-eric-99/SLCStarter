package SLC.Locker;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.Msg;
import SLC.HWHandler.HWHandler;

import java.util.*;


public class LockerDriver extends HWHandler {
    private final int n = Integer.parseInt(appKickstarter.getProperty("Locker.Count"));

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
        System.out.println("Locker: " + msg);
        switch (msg.getType()) {

            case TimesUp:
                break;

            case L_Unlock:
                handleUnlock(msg.getDetails());
                break;

            case L_HasClose:
                sendHasCloseMsg(msg.getDetails());
                break;

            case SLS_RqDiagnostic:
                sendLockerDiagnostic();
                break;

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

    protected void sendLockerDiagnostic() {
        Map<String, Object> information = new LinkedHashMap<>();
        ArrayList<String> openedLocker = new ArrayList<>();
        ArrayList<String> closedLocker = new ArrayList<>();

        lockers.entrySet()
                .parallelStream()
                .forEach(entry -> {
                    if (entry.getValue()) {
                        closedLocker.add(entry.getKey());
                    } else {
                        openedLocker.add(entry.getKey());
                    }
                });

        information.put("Name", appKickstarter.getProperty("Locker.Name"));
        information.put("Manufacturer Name", appKickstarter.getProperty("Locker.Manufacturer"));
        information.put("Version", appKickstarter.getProperty("Locker.Version"));
        information.put("Opened lockers", openedLocker.toArray());
        information.put("Closed lockers", closedLocker.toArray());
        information.put("Retrieval time", System.currentTimeMillis());

        Map<String, Object> map = new HashMap<>();
        map.put("Locker", information);

        String data = new JSONObject(information).toString();
//        System.out.println("================================================");
//        System.out.println("Inside Locker Driver...");
//        System.out.println(data);
//        System.out.println("================================================");


        slc.send(new Msg(id, mbox, Msg.Type.L_RpDiagnostic, data));
    }

}
