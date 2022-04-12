package SLC.OctopusCardReaderDriver;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


//======================================================================
// octopusCardReaderDriver
public class OctopusCardReaderDriver extends HWHandler {

    //------------------------------------------------------------
    // OctopusCardReaderDriver
    public OctopusCardReaderDriver(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
    } // OctopusCardReaderDriver


    @Override
    protected void processMsg(Msg msg) {
        switch (msg.getType()) {
            case OCR_TransactionRequest:
                System.out.println("Got type, "+msg.getDetails());
                //expected details should be the amount in int
                double amount = Double.parseDouble(msg.getDetails());

                if (amount <= 0) {
                    slc.send(new Msg(id, mbox, Msg.Type.OCR_CardOK, msg.getDetails()));
                    handleCardOK();
                    break;
                }

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
    protected void handleCardFailed() {
        log.info(id + ": Card Failed");
    } // handleCardFailed

    //------------------------------------------------------------
    // handleCardOK
    protected void handleCardOK() {
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

        information.put("Name", appKickstarter.getProperty("OctopusCardReader.Name"));
        information.put("Version", appKickstarter.getProperty("OctopusCardReader.Version"));
        information.put("Device Type", appKickstarter.getProperty("OctopusCardReader.Device.Type"));

        String data = new JSONObject(information).toString();

        slc.send(new Msg(id, mbox, Msg.Type.OCR_RpDiagnostic, data));
    }
}
