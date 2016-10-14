package org.openhab.binding.zwave.internal.protocol.transaction;

import java.util.Comparator;
import java.util.Date;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles transaction tracking for ZWave.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public abstract class ZWaveTransaction {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveTransaction.class);

    // Timers

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
        CANCELLED
    }

    private TransactionPriority priority;
    private SerialMessageClass serialMessageClass;
    private SerialMessageClass expectedReplyClass;

    private TransactionState transactionStateTracker = TransactionState.UNINTIALIZED;

    private int attemptsRemaining;
    private int callbackId;

    private long startTime;
    private Date timeout;

    public ZWaveTransaction(SerialMessageClass expectedReplyClass, TransactionPriority priority, int attempts) {
        this.expectedReplyClass = expectedReplyClass;
        this.priority = priority;
        this.attemptsRemaining = 3;
    }

    public void resetTransaction() {
        logger.debug("Transaction RESET with {} retries remaining.", attemptsRemaining);
        transactionStateTracker = TransactionState.UNINTIALIZED;
    }

    public void transactionStart() {
        startTime = System.currentTimeMillis();

        // We must have just sent the message
        if (serialMessageClass.requiresResponse()) {
            transactionStateTracker = TransactionState.WAIT_RESPONSE;
            return;
        }
        if (serialMessageClass.requiresRequest()) {
            transactionStateTracker = TransactionState.WAIT_REQUEST;
            return;
        }

        // If we get here, we don't require any response, so we're done!
        transactionStateTracker = TransactionState.DONE;
    }

    public SerialMessageClass getSerialMessageClass() {
        return serialMessageClass;
    }

    public SerialMessageClass getExpectedReplyClass() {
        return expectedReplyClass;
    }

    public TransactionState getTransactionState() {
        return transactionStateTracker;
    }

    public void setPriority(TransactionPriority priority) {
        this.priority = priority;
    }

    public TransactionPriority getPriority() {
        return priority;
    }

    public int getCallbackId() {
        return callbackId;
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
        transactionStateTracker = TransactionState.CANCELLED;
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

    public boolean transactionAdvance(SerialMessage incomingMessage) {
        logger.debug("TransactionAdvance ST: {}", transactionStateTracker);
        // logger.debug("TransactionAdvance TX: {}", serialMessage);
        logger.debug("TransactionAdvance WT: {}", expectedReplyClass);
        logger.debug("TransactionAdvance RX: {}", incomingMessage);

        System.out.println("TransactionAdvance ST: " + transactionStateTracker);
        // System.out.println("TransactionAdvance TX: " + serialMessage);
        System.out.println("TransactionAdvance WT: " + expectedReplyClass);
        System.out.println("TransactionAdvance RX: " + incomingMessage);

        TransactionState stateTrackerStart = transactionStateTracker;
        switch (transactionStateTracker) {
            case UNINTIALIZED:
                break;

            case WAIT_RESPONSE:
                if (incomingMessage.getMessageClass() != serialMessageClass
                        || incomingMessage.getMessageType() != SerialMessageType.Response) {
                    break;
                }

                // We've received our response - advance
                if (serialMessageClass.requiresRequest()) {
                    transactionStateTracker = TransactionState.WAIT_REQUEST;
                    break;
                }

                // TODO: Ultimately, getExpectedReply should return null if we're not waiting for data
                if (expectedReplyClass != null && expectedReplyClass != incomingMessage.getMessageClass()) {
                    transactionStateTracker = TransactionState.WAIT_DATA;
                    break;
                }

                // We don't need anything else
                transactionStateTracker = TransactionState.DONE;
                break;

            case WAIT_REQUEST:
                if (incomingMessage.getMessageClass() != serialMessageClass
                        || incomingMessage.getMessageType() != SerialMessageType.Request) {
                    break;
                }

                if (incomingMessage.getCallbackId() != getCallbackId()) {
                    break;
                }

                // We've received our request - advance
                // TODO: Ultimately, getExpectedReply should return null if we're not waiting for data
                if (expectedReplyClass != null) { // && expectedReplyClass != incomingMessage.getMessageClass()) {
                    transactionStateTracker = TransactionState.WAIT_DATA;
                    break;
                }
                transactionStateTracker = TransactionState.DONE;
                break;

            case WAIT_DATA:
                System.out.println("WAIT_DATA -- 1");

                if (incomingMessage.getMessageClass() != expectedReplyClass
                        || incomingMessage.getMessageType() != SerialMessageType.Request) {
                    System.out.println("WAIT_DATA -- 2");
                    break;
                }

                // Check if the nodeId is correct
                if (incomingMessage.getMessageType() == SerialMessageType.Request
                        && serialMessage.getMessageNode() != 255
                        && serialMessage.getMessageNode() != incomingMessage.getMessageNode()) {
                    System.out.println("WAIT_DATA -- 3");
                    break;
                }

                // We've received the data we wanted - we're done
                transactionStateTracker = TransactionState.DONE;
                break;

            case CANCELLED:
                // Transaction has been aborted - just return this to the application
                break;

            default:
                logger.error("Unhandled transaction state {}", transactionStateTracker);
                break;
        }
        logger.debug("TransactionAdvance TO: {}", transactionStateTracker);
        return transactionStateTracker != stateTrackerStart;
    }

    /**
     * Comparator Class. Compares two serial messages with each other based on node status (awake / sleep), priority and
     * sequence number.
     */
    public static class TransactionComparator implements Comparator<ZWaveTransaction> {
        /**
         * Compares a serial message to another serial message.
         * Used by the priority queue to order messages.
         *
         * @param arg0 the first serial message to compare the other to.
         * @param arg1 the other serial message to compare the first one to.
         */
        @Override
        public int compare(ZWaveTransaction arg0, ZWaveTransaction arg1) {
            // int res = arg0.priority.compareTo(arg1.priority);

            // if (res == 0 && arg0 != arg1) {
            // res = (arg0.sequenceNumber < arg1.sequenceNumber ? -1 : 1);
            // }

            return 0;
        }
    }

    /*
     * @Override
     * public boolean equals(Object arg0) {
     * if (arg0 == null) {
     * return false;
     * }
     *
     * ZWaveTransaction other = (ZWaveTransaction) arg0;
     *
     * if (arg0.getClass() != this.getClass()) {
     * return false;
     * }
     *
     * if (getExpectedReplyClass() != other.getExpectedReplyClass()) {
     * return false;
     * }
     *
     * if (getSerialMessage().equals(other.getSerialMessage())) {
     * return true;
     * }
     *
     * return false;
     * }
     */
}
