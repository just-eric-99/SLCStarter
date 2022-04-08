package SLC.OctopusCardReaderDriver;

import SLC.HWHandler.HWHandler;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;


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
                //expected details should be the amount in the format dollar+\t+cents
                String[] amount = msg.getDetails().split("\t");

                if (Integer.parseInt(amount[0]) <= 0 && Integer.parseInt(amount[1]) <= 0) {
                    slc.send(new Msg(id, mbox, Msg.Type.OCR_CardOK, msg.getDetails()));
                    handleCardOK();
                    break;
                }

                handleTransactionRequest(Integer.parseInt(amount[0]), Integer.parseInt(amount[1]));
                break;

            case OCR_GoActive:
                handleGoActive();
                break;

            case OCR_GoStandby:
                handleGoStandby();
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
    protected void handleTransactionRequest(int dollar, int cent) {
        log.info(id + ": Transaction Request");
    } // handleCardRead
}
