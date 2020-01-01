/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles transaction tracking for ZWave.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZWaveTransaction {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveTransaction.class);

    private final static AtomicLong sequence = new AtomicLong();
    private final long transactionId = sequence.getAndIncrement();
    private boolean waitForResponse = true;

    private final int DEFAULT_TIMEOUT = 5000;

    private ZWaveMessagePayloadTransaction payload;

    // Timers

    /**
     * Serial message priority enumeration. Indicates the message priority.
     * Queue priority concept -:
     * <ul>
     * <li>RealTime: Messages that must be sent at highest priority. Generally this is reserved for battery devices so
     * we send messages while they are awake. The high priority allows their messages to jump the queue.
     * <i>RealTime</i> messages will not be queued in the wakeup queue. This is meant to be used for time critical
     * events that have no meaning if they are delayed.
     * <li>Immediate: Messages that must be sent at highest priority. Generally this is reserved for battery devices so
     * we send messages while they are awake. The high priority allows their messages to jump the queue.
     * <li>High: Other high priority messages
     * <li>Set: Messages relating to SET commands. This should only be used for SET type commands that need to be
     * responsive - eg light switches, or things that are expected to occur quickly.
     * <li>Get: Messages relating to GET commands. Most messages relating to reading data use this priority.
     * <li>Config: Messages relating to system configuration. This can be GET or SET type commands, but these are things
     * that don't need to be responsive.
     * <li>Poll: Messages relating to polling. Normally these are GET commands, but the system overrides the priority to
     * the lowest so they don't cause any impact on the system.
     * </ul>
     *
     */
    public enum TransactionPriority {
        NonceResponse,
        RealTime,
        Immediate,
        High,
        Set,
        Controller,
        Get,
        Config,
        Poll
    }

    /**
     * Transaction state tracking is handled by working through the different stages of
     * a transaction and handling the transaction stages and completion checking.
     *
     * Transaction states are as follows -:
     * UNINITIALIZED: The transaction has not yet started.
     * SENT: The message has been sent, but no acknowledgements are received.
     * RECEIVED_ACK_CONTROLLER: An ACK has been received from the controller.
     * RECEIVED_ACK_NODE: An ACK has been received from the node. This indicates that the node has received the message.
     * RECEIVED_DATA: The final data has been received.
     * CANCELLED: The transaction is cancelled due to a response error
     *
     * A transaction requires at least the first ACK from the controller. For transactions requiring additional
     * responses, once the ACK from the controller is received, additional transactions may be started if desired.
     *
     */
    public enum TransactionState {
        UNINTIALIZED,
        WAIT_RESPONSE,
        WAIT_REQUEST,
        WAIT_DATA,
        DONE,
        ABORTED,
        CANCELLED
    }

    private SerialMessage serialMessage = null;

    private TransactionPriority priority;
    private long dataTimeout;

    private TransactionState transactionStateCancelled = TransactionState.UNINTIALIZED;
    private TransactionState transactionStateTracker = TransactionState.UNINTIALIZED;

    private int attemptsRemaining = 3;

    private boolean requiresSecurity = false;
    private boolean requiresResponse = true;

    private long startTime;
    private Date timeout;

    public ZWaveTransaction(final ZWaveMessagePayloadTransaction payload) {
        this.priority = payload.getPriority();
        this.dataTimeout = payload.getTimeout();
        if (this.dataTimeout < 250) {
            this.dataTimeout = DEFAULT_TIMEOUT;
        }
        this.attemptsRemaining = payload.getMaxAttempts();
        if (this.attemptsRemaining == 0) {
            this.attemptsRemaining = 3;
        }
        if (payload instanceof ZWaveCommandClassTransactionPayload) {
            this.requiresSecurity = ((ZWaveCommandClassTransactionPayload) payload).getRequiresSecurity();
            this.requiresResponse = ((ZWaveCommandClassTransactionPayload) payload).getRequiresResponse();
        }
        this.payload = payload;
    }

    public void setPayload(ZWaveMessagePayloadTransaction payload) {
        this.payload = payload;
    }

    public void resetTransaction() {
        logger.debug("TID {}: Transaction RESET with {} retries remaining.", transactionId, attemptsRemaining);
        transactionStateTracker = TransactionState.UNINTIALIZED;
        serialMessage = null;
    }

    public void transactionStart() {
        startTime = System.currentTimeMillis();

        logger.trace("TID {}: Transaction Start type {} ", transactionId, payload.getSerialMessageClass());

        // We must have just sent the message
        if (payload.getSerialMessageClass().requiresResponse()) {
            transactionStateTracker = TransactionState.WAIT_RESPONSE;
            return;
        }
        if (payload.getSerialMessageClass().requiresRequest()) {
            transactionStateTracker = TransactionState.WAIT_REQUEST;
            return;
        }

        // If we get here, we don't require any response, so we're done!
        transactionStateTracker = TransactionState.DONE;
    }

    public void setSerialMessage(SerialMessage serialMessage) {
        this.serialMessage = serialMessage;
    }

    public SerialMessage getSerialMessage() {
        if (serialMessage == null) {
            serialMessage = payload.getSerialMessage();
        }

        return serialMessage;
    }

    public SerialMessageClass getSerialMessageClass() {
        return payload.getSerialMessageClass();
    }

    public byte[] getPayloadBuffer() {
        return payload.getPayloadBuffer();
    }

    public int getNodeId() {
        return payload.getDestinationNode();
    }

    public int getQueueId() {
        // if (payload.getSerialMessageClass() == SerialMessageClass.SendData) {
        // return 255;
        // }
        return payload.getDestinationNode();
    }

    public SerialMessageClass getExpectedReplyClass() {
        return payload.getExpectedResponseSerialMessageClass();
    }

    public CommandClass getExpectedCommandClass() {
        // logger.debug("Transaction type is {}", payload.getClass().getSimpleName());
        if (payload instanceof ZWaveCommandClassTransactionPayload) {
            // logger.debug("Transaction expected response is {}",
            // ((ZWaveCommandClassTransactionPayload) payload).getExpectedResponseCommandClass());
            return ((ZWaveCommandClassTransactionPayload) payload).getExpectedResponseCommandClass();
        }
        return null;
    }

    public Integer getExpectedCommandClassCommand() {
        if (payload instanceof ZWaveCommandClassTransactionPayload) {
            return ((ZWaveCommandClassTransactionPayload) payload).getExpectedResponseCommandClassCommand();
        }
        return null;
    }

    public TransactionState getTransactionState() {
        return transactionStateTracker;
    }

    /**
     * Gets the state the transaction was at when cancelled
     *
     * @return the {@link TransactionState} when cancelled
     */
    public TransactionState getTransactionCancelledState() {
        return transactionStateCancelled;
    }

    public void setPriority(TransactionPriority priority) {
        this.priority = priority;
    }

    public TransactionPriority getPriority() {
        return priority;
    }

    public int getCallbackId() {
        if (serialMessage == null) {
            return -1;
        }
        return serialMessage.getCallbackId();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public void setTimeout(Date timeout) {
        this.timeout = timeout;
    }

    public Date getTimeout() {
        return timeout;
    }

    public void setTransactionCanceled() {
        logger.debug("TID {}: Transaction CANCELLED", transactionId);
        transactionStateCancelled = transactionStateTracker;
        transactionStateTracker = TransactionState.CANCELLED;
    }

    public void setTransactionAborted() {
        logger.debug("TID {}: Transaction ABORTED", transactionId);
        transactionStateTracker = TransactionState.ABORTED;
    }

    public void setTransactionComplete() {
        logger.debug("TID {}: Transaction COMPLETED", transactionId);
        transactionStateTracker = TransactionState.DONE;
    }

    public void setAttemptsRemaining(int attemptsRemaining) {
        this.attemptsRemaining = attemptsRemaining;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public int decrementAttemptsRemaining() {
        return --attemptsRemaining;
    }

    public boolean requiresDataBeforeNextRelease() {
        return payload.requiresData();
    }

    public long getDataTimeout() {
        return dataTimeout;
    }

    public boolean transactionAdvance(SerialMessage incomingMessage) {
        logger.trace("TID {}: TransactionAdvance ST: {}", transactionId, transactionStateTracker);
        logger.trace("TID {}: TransactionAdvance WT: {} {}", transactionId,
                payload.getExpectedResponseSerialMessageClass());
        logger.trace("TID {}: TransactionAdvance RX: {}", transactionId, incomingMessage);

        TransactionState stateTrackerStart = transactionStateTracker;
        switch (transactionStateTracker) {
            case UNINTIALIZED:
                break;

            case WAIT_RESPONSE:
                if (incomingMessage.getMessageClass() != payload.getSerialMessageClass()
                        || incomingMessage.getMessageType() != SerialMessageType.Response) {
                    break;
                }

                // We've received our response - advance
                if (payload.getSerialMessageClass().requiresRequest()) {
                    transactionStateTracker = TransactionState.WAIT_REQUEST;
                    break;
                }

                // getExpectedReply returns null if we're not waiting for data
                if (payload.getExpectedResponseSerialMessageClass() != null
                        && payload.getExpectedResponseSerialMessageClass() != incomingMessage.getMessageClass()) {
                    transactionStateTracker = TransactionState.WAIT_DATA;
                    break;
                }

                // We don't need anything else
                transactionStateTracker = TransactionState.DONE;
                break;

            case WAIT_REQUEST:
                // Check that this message is a request, and for the message class we sent
                if (incomingMessage.getMessageClass() != payload.getSerialMessageClass()
                        || incomingMessage.getMessageType() != SerialMessageType.Request) {
                    break;
                }

                // Check the callback ID is consistent
                if (incomingMessage.getCallbackId() != getCallbackId()) {
                    break;
                }

                logger.trace("TID {}: TransactionAdvance RQ: RREQ={}, RCLS={}", transactionId, requiresResponse,
                        payload.getExpectedResponseSerialMessageClass());

                // We've received our request - advance
                // getExpectedReply returns null if we're not waiting for data
                if (requiresResponse == true && payload.getExpectedResponseSerialMessageClass() != null) {
                    transactionStateTracker = TransactionState.WAIT_DATA;
                    break;
                }
                transactionStateTracker = TransactionState.DONE;
                break;

            case WAIT_DATA:
                if (incomingMessage.getMessageClass() != payload.getExpectedResponseSerialMessageClass()
                        || incomingMessage.getMessageType() != SerialMessageType.Request) {
                    break;
                }

                // Check if the nodeId is correct
                if (incomingMessage.getMessageType() == SerialMessageType.Request && payload.getDestinationNode() != 255
                        && payload.getDestinationNode() != incomingMessage.getMessageNode()) {
                    break;
                }

                // We've received the data we wanted - we're done
                transactionStateTracker = TransactionState.DONE;
                break;

            case ABORTED:
                break;

            case CANCELLED:
                // Transaction has been aborted - just return this to the application
                break;

            case DONE:
                break;

            default:
                logger.error("TID {}: Unhandled transaction state {}", transactionId, transactionStateTracker);
                break;
        }
        logger.trace("TID {}: TransactionAdvance TO: {}", transactionId, transactionStateTracker);
        return transactionStateTracker != stateTrackerStart;
    }

    public boolean getRequiresSecurity() {
        return requiresSecurity;
    }

    public boolean getRequiresResponse() {
        return requiresResponse;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setWaitForResponse(boolean waitForResponse) {
        this.waitForResponse = false;
    }

    public boolean getWaitForResponse() {
        return waitForResponse;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null) {
            return false;
        }

        ZWaveTransaction other = (ZWaveTransaction) arg0;

        if (arg0.getClass() != this.getClass()) {
            return false;
        }

        if (getExpectedReplyClass() != other.getExpectedReplyClass()) {
            return false;
        }

        if (getNodeId() != other.getNodeId()) {
            return false;
        }

        if (payload.getSerialMessageClass() != other.getSerialMessageClass()) {
            return false;
        }

        return Arrays.equals(payload.getPayloadBuffer(), other.getPayloadBuffer());
    }

    @Override
    public String toString() {
        return "TID " + transactionId + ": [" + transactionStateTracker + "] priority=" + priority
                + ", requiresResponse=" + requiresResponse + ", callback: "
                + (serialMessage == null ? "--" : serialMessage.getCallbackId());
    }
}
