package SLC.BarcodeReaderDriver;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import SLC.SLC.HWStatus;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


//======================================================================
// BarcodeReaderDriver
public class BarcodeReaderDriver extends HWHandler {
    protected HWStatus brInnerStatus = HWStatus.Standby;
    //------------------------------------------------------------
    // BarcodeReaderDriver
    public BarcodeReaderDriver(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
    } // BarcodeReaderDriver


    //------------------------------------------------------------
    // processMsg
    protected void processMsg(Msg msg) {
        switch (msg.getType()) {
            case BR_BarcodeRead:
                slc.send(new Msg(id, mbox, Msg.Type.BR_BarcodeRead, msg.getDetails()));
                break;

            case BR_GoActive:
                handleGoActive();
                break;

            case BR_GoStandby:
                handleGoStandby();
                break;

            case SLS_RqDiagnostic:
                sendBarcodeReaderDiagnostic();
                break;


            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
    } // processMsg


    //------------------------------------------------------------
    // handleGoActive
    protected void handleGoActive() {
        log.info(id + ": Go Active");
    } // handleGoActive


    //------------------------------------------------------------
    // handleGoStandby
    protected void handleGoStandby() {
        log.info(id + ": Go Standby");
    } // handleGoStandby


    //------------------------------------------------------------
    // handlePoll
    protected void handlePoll() {
        log.info(id + ": Handle Poll");
    } // handlePoll

    //------------------------------------------------------------
    // sendOctopusCardReaderDiagnostic
    protected void sendBarcodeReaderDiagnostic() {
        Map<String, Object> information = new LinkedHashMap<>();

        // put it in decending order
        information.put("Hardware status", brInnerStatus);
        information.put("Retrieval time", System.currentTimeMillis());
        information.put("Version", appKickstarter.getProperty("BarcodeReader.Version"));
        information.put("Manufactured by", appKickstarter.getProperty("BarcodeReader.Manufacturer"));
        information.put("Name", appKickstarter.getProperty("BarcodeReader.Name"));

        String data = new JSONObject(information).toString();

        slc.send(new Msg(id, mbox, Msg.Type.BR_RpDiagnostic, data));
    }
} // BarcodeReaderDriver
