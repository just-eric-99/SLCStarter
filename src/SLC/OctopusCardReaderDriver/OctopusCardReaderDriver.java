package SLC.OctopusCardReaderDriver;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import SLC.SLC.HWStatus;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


//======================================================================
// octopusCardReaderDriver
public class OctopusCardReaderDriver extends HWHandler {
    protected HWStatus ocrInnerStatus = HWStatus.Standby;
    //------------------------------------------------------------
    // OctopusCardReaderDriver
    public OctopusCardReaderDriver(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
    } // OctopusCardReaderDriver


    @Override
    protected void processMsg(Msg msg) {
        System.out.println("Got type, "+ msg);
        switch (msg.getType()) {
            case OCR_TransactionRequest:
                // expected details should be the amount in int
                double amount = Double.parseDouble(msg.getDetails());
                handleTransactionRequest(amount);
                break;

            case OCR_GoStandby:
                handleGoStandby();
                break;

            case SLS_RqDiagnostic:
                sendOctopusCardReaderDiagnostic();
                break;

            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
    }

    @Override
    protected void handlePoll() {
        log.info(id + ": Handle Poll");
    }

    //------------------------------------------------------------
    // handleGoStandby
    protected void handleGoStandby() {
        log.info(id + ": Go Standby");
    } // handleGoStandby

    //------------------------------------------------------------
    // handleCardFailed
    protected void handleCardFailed(String failMsg) {
        log.info(id + ": Card Failed");
    } // handleCardFailed

    //------------------------------------------------------------
    // handleCardOK
    protected void handleCardOK(String cardID, String amount) {
        log.info(id + ": Card OK");
    } // handleCardOk

    //------------------------------------------------------------
    // handleTransactionRequest
    protected void handleTransactionRequest(double amount) {
        log.info(id + ": Transaction Request");
    } // handleCardRead

    //------------------------------------------------------------
    // sendOctopusCardReaderDiagnostic
    protected void sendOctopusCardReaderDiagnostic() {
        Map<String, Object> information = new LinkedHashMap<>();

        information.put("Hardware status", ocrInnerStatus);
        information.put("Name", appKickstarter.getProperty("OctopusCardReader.Name"));
        information.put("Version", appKickstarter.getProperty("OctopusCardReader.Version"));
        information.put("Device Type", appKickstarter.getProperty("OctopusCardReader.Device.Type"));

        String data = new JSONObject(information).toString();

        slc.send(new Msg(id, mbox, Msg.Type.OCR_RpDiagnostic, data));
    }
}
