package SLC.TouchDisplayHandler;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import SLC.SLC.HWStatus;
import SLC.SLC.Screen;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


//======================================================================
// TouchDisplayHandler
public class TouchDisplayHandler extends HWHandler {
    protected HWStatus tdInnerStatus =  HWStatus.Active;
    private Screen currentScreen = Screen.Welcome_Page;
    //------------------------------------------------------------
    // TouchDisplayHandler
    public TouchDisplayHandler(String id, AppKickstarter appKickstarter) throws Exception {
        super(id, appKickstarter);
    } // TouchDisplayHandler


    //------------------------------------------------------------
    // processMsg

    // only receive message
    protected void processMsg(Msg msg) {
        switch (msg.getType()) {
            case TD_MouseClicked:
                slc.send(new Msg(id, mbox, Msg.Type.TD_MouseClicked, msg.getDetails()));
                break;

            case TD_UpdateDisplay:
                handleUpdateDisplay(msg);
                break;

            case SLS_RqDiagnostic:
                sendDiagnostic();
                break;

            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
    } // processMsg


    //------------------------------------------------------------
    // handleUpdateDisplay
    protected void handleUpdateDisplay(Msg msg) {
        log.info(id + ": update display -- " + msg.getDetails());
    } // handleUpdateDisplay


    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() {
        log.info(id + ": Handle Poll");
    } // handlePoll

    // For hacking
    protected void changeScreen(Screen s) {
        currentScreen = s;
        slc.send(new Msg(id, mbox, Msg.Type.TD_ChangeScreen, s.toString()));
    }

    private void sendDiagnostic() {
        Map<String, Object> information = new LinkedHashMap<>();

        information.put("Current Screen", currentScreen);
        information.put("Retrieval time", System.currentTimeMillis());
        information.put("Version", appKickstarter.getProperty("TouchDisplay.Version"));
        information.put("Manufacturer Name", appKickstarter.getProperty("TouchDisplay.Manufacturer"));
        information.put("Name", appKickstarter.getProperty("TouchDisplay.Name"));

        String data = new JSONObject(information).toString();

        slc.send(new Msg(id, mbox, Msg.Type.TD_RpDiagnostic, data));
    }
} // TouchDisplayHandler
