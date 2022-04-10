package SLC.TouchDisplayHandler;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import SLC.SLC.Screen;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


//======================================================================
// TouchDisplayHandler
public class TouchDisplayHandler extends HWHandler {
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
        slc.send(new Msg(id, mbox, Msg.Type.TD_ChangeScreen, s.toString()));
    }
} // TouchDisplayHandler
