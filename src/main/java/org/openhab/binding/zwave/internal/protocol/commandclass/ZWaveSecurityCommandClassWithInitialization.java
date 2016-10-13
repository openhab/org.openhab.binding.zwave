/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStageAdvancer;
import org.openhab.binding.zwave.internal.protocol.security.SecurityEncapsulatedSerialMessage;
import org.openhab.binding.zwave.internal.protocol.security.ZWaveSecureInclusionStateTracker;
import org.openhab.binding.zwave.internal.protocol.security.ZWaveSecurityPayloadFrame;
import org.openhab.binding.zwave.internal.protocol.transaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the secure pairing portion and initialization of the Security command class.
 * See {@link #initialize(boolean)} for a lot of details about
 * how the secure pairing process is inherently different from the other initialization process
 *
 * @author Dave Badia
 * @author Chris Jackson
 *
 */

@XStreamAlias("securityCommandClassWithInit")
public class ZWaveSecurityCommandClassWithInitialization extends ZWaveSecurityCommandClass
        implements ZWaveCommandClassInitialization, ZWaveEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveSecurityCommandClassWithInitialization.class);

    /**
     * the scheme that is used prior to any keys being negotiated
     */
    private static final byte SECURITY_SCHEME_ZERO = 0x00;

    /**
     * Only non-null when we are including a new node
     */
    @XStreamOmitField
    private volatile ZWaveSecureInclusionStateTracker inclusionStateTracker = null;

    /**
     * The last {@link SerialMessage} that was given to {@link ZWaveNodeInitStageAdvancer}
     * when it called {@link ZWaveSecurityCommandClass#initialize(boolean)}. Used
     * in cases where we need to resend the last message (transmission failure, etc)
     */
    @XStreamOmitField
    private ZWaveTransaction lastRequestSecurePairTransaction = null;

    private static final String SECURE_INCLUSION_FAILED_MESSAGE = "SECURITY_INCLUSION_FAILED";

    /**
     * Timer that tracks how long we should wait for a response. {@link ZWaveNodeInitStageAdvancer}
     * already has a timer, but since the initialization of this class involves multiple security
     * messages, we cannot rely on that to re-send the last message. So, we keep our own timer
     * to know when it's time to retry a message
     */
    @XStreamOmitField
    private long waitForReplyTimeout = Long.MAX_VALUE;

    @XStreamOmitField
    private long inclusionStartedAt = Long.MIN_VALUE;

    /**
     * Flag so we understand that the secure pairing process was completed at some point in time
     */
    protected boolean securePairingComplete = false;

    public ZWaveSecurityCommandClassWithInitialization(ZWaveNode node, ZWaveController controller,
            ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        controller.addEventListener(this);
    }

    private boolean isSecureInclusionInProgress() {
        return inclusionStateTracker != null;
    }

    /**
     * There are 2 different ways we need to transmit messages:
     * 1) during inclusion mode, our {@link #initialize(boolean)} method will return the next message to send (handled
     * below)
     * 2) during normal (non-inclusion) mode, give the message to {@link ZWaveController} (handled by the superclass)
     */
    @Override
    protected void transmitMessage(ZWaveTransaction transaction) {
        SerialMessage message = transaction.getSerialMessage();
        if (isSecureInclusionInProgress() && message instanceof SecurityEncapsulatedSerialMessage
                && ((SecurityEncapsulatedSerialMessage) message).getSecurityPayload() != null) {
            ZWaveSecurityPayloadFrame securityPayload = ((SecurityEncapsulatedSerialMessage) message)
                    .getSecurityPayload();
            // if the message we just created is SECURITY_NETWORK_KEY_SET, then we need to change our Network Key
            // to use the real key, as the reply we will get back will be encrypted with the real Network key
            if (bytesAreEqual(securityPayload.getMessageBytes()[0],
                    ZWaveCommandClass.CommandClass.COMMAND_CLASS_SECURITY.getKey())
                    && bytesAreEqual(securityPayload.getMessageBytes()[1], SECURITY_NETWORK_KEY_SET)) {
                logger.info("NODE {}: Setting Network Key to real key after SECURITY_NETWORK_KEY_SET",
                        getNode().getNodeId());
                setupNetworkKey(false);
            }
            // We are in inclusion mode, so give the message to the tracker so it will be picked
            // up when ZWaveNodeStageAdvancer calls our initialize method
            inclusionStateTracker.setNextRequest(transaction);
        } else {
            // Normal (non-inclusion mode) so give the message to the controller to be transmitted
            super.transmitMessage(transaction);
        }
    }

    /**
     * During inclusion, {@link ZWaveSecurityCommandClass#ZWaveSecurityEncapsulationThread} is not running
     * so we override this logic and just have the calling thread (typically ZWaveInputThread) execute the
     * security encapsulation logic
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    protected void notifyEncapsulationThread() {
        if (isSecureInclusionInProgress()) {
            sendNextMessageUsingDeviceNonce();
        } else {
            // Normal (non-inclusion mode)
            super.notifyEncapsulationThread();
        }
    }

    @ZWaveResponseHandler(id = SECURITY_SCHEME_REPORT, name = "SECURITY_SCHEME_REPORT")
    protected void handleSecuritySchemeReport(ZWaveCommandClassPayload payload, int endpoint) {
        logger.debug("NODE {}: Received SECURITY command V{}", getNode().getNodeId(), getVersion());
        traceHex("payload bytes for incoming security message", payload.getPayloadBuffer());
        lastReceivedMessageTimestamp = System.currentTimeMillis();
        if (inclusionStateTracker != null && !inclusionStateTracker.verifyAndAdvanceState(payload.getPayloadByte(1))) {
            // bad order, abort
            return;
        }
        // Should be received during inclusion only
        if (!wasThisNodeJustIncluded() || inclusionStateTracker == null) {
            logger.error("NODE {}: SECURITY_ERROR Received SECURITY_SCHEME_REPORT but we are not in inclusion mode! {}",
                    payload);
            return;
        }

        int schemes = payload.getPayloadByte(2);
        logger.debug("NODE {}: Received Security Scheme Report: ", getNode().getNodeId(), schemes);
        if (schemes == SECURITY_SCHEME_ZERO) {
            // Since we've agreed on a scheme for which to exchange our key
            // we now send our NetworkKey to the device
            logger.debug("NODE {}: Security scheme agreed.", getNode().getNodeId());

            SerialMessage networkKeyMessage = new ZWaveSendDataMessageBuilder()
                    .withCommandClass(getCommandClass(), SECURITY_NETWORK_KEY_SET)
                    .withPayload(realNetworkKey.getEncoded()).withNodeId(getNode().getNodeId()).build();

            ZWaveTransaction networkKeyTransaction = new ZWaveTransactionBuilder(networkKeyMessage)
                    .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                    .withExpectedResponseCommandClass(getCommandClass(), SECURITY_NETWORK_KEY_VERIFY)
                    .withPriority(TransactionPriority.Config).build();

            // We can't set SECURITY_NETWORK_KEY_SET in inclusionStateTracker because we need to do a
            // NONCE_GET before sending. So put this in our encrypt send queue
            // and give inclusionStateTracker/ZWaveNodeStageAdvancer the NONCE_GET
            queueMessageForEncapsulationAndTransmission(networkKeyTransaction);
            if (!inclusionStateTracker.verifyAndAdvanceState(SECURITY_NETWORK_KEY_SET)) {
                return;
            }

            ZWaveTransaction message = nonceGeneration.buildNonceGetIfNeeded();
            // Since we are in init mode, message should always != null
            if (message != null) {
                // TODO: DB is this true?: logger.error("NODE {}: "+SECURE_INCLUSION_FAILED_MESSAGE+" In
                // inclusion mode but buildNonceGetIfNeeded returned null, this may result in a deadlock");
                // Let ZWaveNodeStageAdvancer come get the NONCE_GET
                inclusionStateTracker.setNextRequest(message);
            }

        } else {
            // No common security scheme. This really shouldn't happen
            inclusionStateTracker.setErrorState("TODO: Security scheme " + schemes + " is not supported");
            logger.error("NODE {}: " + SECURE_INCLUSION_FAILED_MESSAGE
                    + " No common security scheme. Scheme requested was {}", getNode().getNodeId(), schemes);
        }
    }

    @ZWaveResponseHandler(id = SECURITY_NETWORK_KEY_VERIFY, name = "SECURITY_NETWORK_KEY_VERIFY")
    protected void handleSecurityNetworkKeyVerify(ZWaveCommandClassPayload payload, int endpoint) {
        // Should be received during inclusion only
        if (!wasThisNodeJustIncluded() || inclusionStateTracker == null) {
            logger.error(
                    "NODE {}: SECURITY_ERROR Received SECURITY_NETWORK_KEY_VERIFY but we are not in inclusion mode! {}",
                    payload);
            return;
        }

        // Since we got here, it means we decrypted a packet using the key we sent in
        // the SECURITY_NETWORK_KEY_SET message and the new key is in use by both sides.
        // Next step is to send SECURITY_COMMANDS_SUPPORTED_GET
        if (SEND_SECURITY_COMMANDS_SUPPORTED_GET_ON_STARTUP) {
            securePairingComplete = true;
        }

        SerialMessage supportedGetMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SECURITY_COMMANDS_SUPPORTED_GET).withNodeId(getNode().getNodeId())
                .build();

        ZWaveTransaction supportedGetTransaction = new ZWaveTransactionBuilder(supportedGetMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), SECURITY_COMMANDS_SUPPORTED_REPORT)
                .withPriority(TransactionPriority.Config).build();

        inclusionStateTracker.verifyAndAdvanceState(SECURITY_COMMANDS_SUPPORTED_GET);
        ZWaveTransaction nonceGetMessage = nonceGeneration.buildNonceGetIfNeeded();
        // Since we are in init mode, message should always != null
        if (nonceGetMessage == null) {
            inclusionStateTracker.setErrorState(SECURE_INCLUSION_FAILED_MESSAGE
                    + " In inclusion mode but buildNonceGetIfNeeded returned null," + " this may result in a deadlock");
            return;
        }
        inclusionStateTracker.setNextRequest(nonceGetMessage); // Let ZWaveNodeStageAdvancer come get it

        // We can't set SECURITY_COMMANDS_SUPPORTED_GET in inclusionStateTracker because we need to do a
        // NONCE_GET before sending. So put this in our encrypt send queue
        // and give inclusionStateTracker/ZWaveNodeStageAdvancer the NONCE_GET
        queueMessageForEncapsulationAndTransmission(supportedGetTransaction);
    }

    @ZWaveResponseHandler(id = SECURITY_COMMANDS_SUPPORTED_REPORT, name = "SECURITY_COMMANDS_SUPPORTED_REPORT")
    protected void handleSecurityCommandsReportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        // processSecurityCommandsSupportedReport(payload);
        // This can be received during device inclusion or outside of it
        if (inclusionStateTracker != null) {
            // We're done with all of our NodeStage#SECURITY_REPORT stuff, set inclusionStateTracker to null
            inclusionStateTracker = null;
        }

    }

    // TODO: DB remove
    private static boolean USE_DELAY_FOR_SCHEME_GET = false;

    /**
     * {@inheritDoc}
     *
     * This code is only executed during secure inclusion.
     *
     * ZWaveNodeStageAdvancer calls us for one of the following reasons:
     * 1. It's checking for the next message to be sent
     * 2. the ZWaveNodeStageAdvancer retry timer was triggered
     * <p/>
     * During node inclusion we have to exchange many message with the device to setup
     * security encapsulation.
     * <p/>
     * Ideally, we would create all necessary messages the very first time this method
     * is called and return the collection. But that is not achievable due to the following:
     * 1. Some messages depend on the result of previous responses.
     * 2. In order to send a security encapsulated message, we need to send a {@link #SECURITY_NONCE_GET},
     * wait for the {@link #SECURITY_NONCE_REPORT} and use that data to build the message. Theoretically
     * we could send many of these at once and get the replies, but they are valid for as little as 3
     * seconds so they would expire before we the message that used the nonce would ever reach the device.
     * <p/>
     * Since we can't create all messages at once, we create a helper {@link ZWaveSecureInclusionStateTracker}
     * which keeps track of where we are at in the flow and hold the next message to be sent. For security
     * reasons, it's also critical to track that the steps are executing in the proper order.
     * <p/>
     * Adding even more complexity, this method is frequently invoked by {@link ZWaveController.ZWaveInputThread} which
     * means
     * that as long as the thread is here, we will not process any incoming messages such as
     * {@link #SECURITY_NONCE_REPORT}.
     * To avoid blocking the thread, we return an empty collection to indicate that we are still waiting for a response
     * message.
     * <p/>
     * This method is nasty but I've already spent hours trying to refactor it into readable code but have obviously
     * failed.
     * <p/>
     *
     * @return One or more {@link SerialMessage} to be sent OR a zero length collection if we are still waiting for a
     *         response OR
     *         null if the secure pairing process has completed or failed
     * @throws ZWaveSerialMessageException
     *
     * @see {@link ZWaveNodeInitStageAdvancer}
     */
    @Override
    public Collection<ZWaveTransaction> initialize(boolean firstIteration) {
        // ZWaveNodeStageAdvancer calls us for one of the following reasons:
        // 1. It's checking for the next message to be sent
        // 2. the ZWaveNodeStageAdvancer retry timer was triggered
        boolean wasThisNodeJustIncluded = wasThisNodeJustIncluded();
        checkInit();
        logger.debug(
                "NODE {}: SECURITY_INITIALIZE Initialising={}, Inclusion={}, Paired={}, "
                        + "lastRxMsg={}ms, lastTxMsg={}ms",
                this.getNode().getNodeId(), firstIteration, wasThisNodeJustIncluded, securePairingComplete,
                (System.currentTimeMillis() - lastReceivedMessageTimestamp),
                (System.currentTimeMillis() - lastSentMessageTimestamp));

        if (wasThisNodeJustIncluded) {
            List<ZWaveTransaction> inclusionMessageReturnList = null;
            if (firstIteration && !securePairingComplete) {
                inclusionStartedAt = System.currentTimeMillis();
                // if we are adding this node, then send SECURITY_SCHEME_GET which will start the Network Key Exchange
                setupNetworkKey(true);
                inclusionStateTracker = new ZWaveSecureInclusionStateTracker(getNode());
                inclusionStateTracker.resetWaitForReplyTimeout();

                // Need to start things off by sending SECURITY_SCHEME_GET
                SerialMessage message = new ZWaveSendDataMessageBuilder()
                        .withCommandClass(getCommandClass(), SECURITY_SCHEME_GET).withPayload(0)
                        .withNodeId(getNode().getNodeId()).build();

                ZWaveTransaction transaction = new ZWaveTransactionBuilder(message)
                        .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                        .withExpectedResponseCommandClass(getCommandClass(), SECURITY_SCHEME_REPORT)
                        .withPriority(TransactionPriority.Config).build();

                // retry only once
                transaction.setAttemptsRemaining(1);

                // SchemeGet is unencrypted, hand it back or set it on inclusionStateTracker
                if (USE_DELAY_FOR_SCHEME_GET) {
                    inclusionStateTracker.setNextRequest(transaction);
                    inclusionMessageReturnList = Collections.emptyList();
                } else {
                    inclusionMessageReturnList = Collections.singletonList(transaction);
                }
            } else if (receivedSecurityCommandsSupportedReport) { // We're done!
                securePairingComplete = true;
                inclusionMessageReturnList = null; // Tell ZWaveNodeStageAdvancer to advance to the next stage
            } else { // Normal inclusion flow, get the next message or wait for a response to the current one
                ZWaveTransaction nextTransaction = null;
                if (USE_DELAY_FOR_SCHEME_GET) {
                    boolean timerUp = System.currentTimeMillis() > (inclusionStartedAt + 5000);
                    logger.debug("NODE {}: USE_DELAY_FOR_SCHEME_GET active,  timerUp={}", getNode().getNodeId(),
                            timerUp);
                    if (timerUp) {
                        nextTransaction = inclusionStateTracker.getNextRequest();
                    }
                } else {
                    nextTransaction = inclusionStateTracker.getNextRequest();
                }
                logger.debug(
                        "NODE {}: Security.initialize, inclusion flow, get the next message or wait for a response to the current one, nextMessage={}",
                        getNode().getNodeId(), nextTransaction);
                if (nextTransaction == null) { // There is an outstanding request or a timeout error occurred
                    if (securePairingComplete) {
                        inclusionStateTracker = null;
                        return null; // all done
                    } else { // !securePairingComplete
                        if (inclusionStateTracker.getErrorState() != null) { // Check for errors
                            logger.error("NODE {}: " + SECURE_INCLUSION_FAILED_MESSAGE + " Failed at step {}: {}",
                                    getNode().getNodeId(), commandToString(inclusionStateTracker.getCurrentStep()),
                                    inclusionStateTracker.getErrorState());
                            inclusionStateTracker = null;

                            // We're done but are in a failure state
                            return null;
                        } else {
                            // Keep waiting for a response
                            inclusionMessageReturnList = Collections.emptyList();
                        }
                    } // END securePairingComplete
                } else { // nextMessage != null: There is no outstanding request and we have another message to send
                    // Send the next request and reset our timer
                    inclusionStateTracker.resetWaitForReplyTimeout();
                    inclusionMessageReturnList = Collections.singletonList(nextTransaction);
                } // END There is an outstanding request
            } // END else Normal inclusion flow, get the next message or wait for a response to the current one

            if (inclusionMessageReturnList != null && inclusionMessageReturnList.size() > 0) {
                lastRequestSecurePairTransaction = inclusionMessageReturnList.get(0);
            }
            logger.debug("NODE {}: Security.initialize, just included, handing back message={}", getNode().getNodeId(),
                    inclusionMessageReturnList == null ? "null" : inclusionMessageReturnList);
            return inclusionMessageReturnList;
            // END wasThisNodeJustIncluded
        } else { // Our node was NOT just included
            List<ZWaveTransaction> returnMessageList = null;
            if (!securePairingComplete) {
                logger.error(
                        "NODE {}: SECURITY_ERROR Invalid state! Secure inclusion has not completed and we are not in inclusion mode. Aborting",
                        getNode().getNodeId());
                returnMessageList = null;

            } else if (firstIteration) { // request the current list of security commands as a sanity check
                // The node was initialized previously and we are connecting to it after an openhab restart
                if (!SEND_SECURITY_COMMANDS_SUPPORTED_GET_ON_STARTUP) {
                    return null; // nothing to do
                }

                SerialMessage message = new ZWaveSendDataMessageBuilder()
                        .withCommandClass(getCommandClass(), SECURITY_COMMANDS_SUPPORTED_GET)
                        .withNodeId(getNode().getNodeId()).build();

                ZWaveTransaction transaction = new ZWaveTransactionBuilder(message)
                        .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                        .withExpectedResponseCommandClass(getCommandClass(), SECURITY_COMMANDS_SUPPORTED_REPORT)
                        .withPriority(TransactionPriority.Config).build();

                ZWaveTransaction nonceGetMessage = nonceGeneration.buildNonceGetIfNeeded();
                // We can't return SECURITY_COMMANDS_SUPPORTED_GET because we need to do a
                // NONCE_GET before sending. So put this in our encrypt send queue
                // and give ZWaveNodeStageAdvancer the NONCE_GET
                queueMessageForEncapsulationAndTransmission(transaction);
                returnMessageList = Collections.singletonList(nonceGetMessage);
            } else if (receivedSecurityCommandsSupportedReport) {
                // Normal flow, nothing else to do, tell ZWaveNodeStageAdvancer to advance to the next stage
                returnMessageList = null;
            } else if (System.currentTimeMillis() > waitForReplyTimeout) {
                logger.error(
                        "NODE {}: SECURITY_ERROR Got no response to SECURITY_COMMANDS_SUPPORTED on init, using old",
                        getNode().getNodeId());
                // Tell ZWaveNodeStageAdvancer to advance to the next stage
                returnMessageList = null;
            } else {
                // the request was already sent, wait for the nonce exchange and the reply to come
                returnMessageList = Collections.emptyList();
            }
            logger.debug("NODE {}: Security.initialize, from xml, handing back message={}", getNode().getNodeId(),
                    returnMessageList);
            if (returnMessageList != null && returnMessageList.size() > 0) {
                lastRequestSecurePairTransaction = returnMessageList.get(0);
            }
            return returnMessageList;
        } // end if wasThisNodeJustIncluded
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        if (event instanceof ZWaveTransactionCompletedEvent && event.getNodeId() == getNode().getNodeId()) {
            logger.debug("NODE {}: updating lastSentMessageTimestamp", this.getNode().getNodeId());
            lastSentMessageTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    protected void checkInit() {
        super.checkInit();
    }

    // TODO: Move this to main security class
    @Override
    boolean checkRealNetworkKeyLoaded() {
        if (realNetworkKey == null) {
            String errorMessage = "NODE " + getNode().getNodeId()
                    + ": Trying to perform secure operation but Network key is NOT set due to: ";
            if (keyException != null) {
                errorMessage += keyException.getMessage();
            }
            // logger.error(errorMessage, keyException);
            // TODO: Split this into the initialisation handler
            if (inclusionStateTracker != null) {
                inclusionStateTracker.setErrorState(errorMessage);
            }
            return false;
        }
        return true;
    }

    public boolean wasSecureInclusionSuccessful() {
        return securePairingComplete;
    }

}
