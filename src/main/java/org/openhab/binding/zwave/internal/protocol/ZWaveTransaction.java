package org.openhab.binding.zwave.internal.protocol;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
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

    private int DEFAULT_TIMEOUT = 2500;

    private final ZWaveMessagePayloadTransaction payload;

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
        RealTime,
        Immediate,
        High,
        Set,
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
        CANCELLED
    }

    // private int nodeId;

    private SerialMessage serialMessageDebug; // Delete - just here for debugging right now

    private int callbackId = 0;

    private TransactionPriority priority;
    // private SerialMessageClass serialMessageClass;
    // private SerialMessageClass expectedReplyClass;
    private CommandClass expectedReplyCommandClass;
    private Integer expectedReplyCommandClassCommand;
    private long dataTimeout;

    private TransactionState transactionStateCancelled = TransactionState.UNINTIALIZED;
    private TransactionState transactionStateTracker = TransactionState.UNINTIALIZED;

    private int attemptsRemaining = 3;

    private long startTime;
    private Date timeout;

    // public ZWaveTransaction(int nodeId, SerialMessageClass serialMessage, SerialMessageClass expectedReplyClass,
    // CommandClass expectedReplyCommandClass, int expectedReplyCommandClassCommand, TransactionPriority priority,
    // int attempts, boolean requiresData, int dataTimeout) {
    // this.nodeId = nodeId;
    // this.serialMessage = serialMessage;
    // this.serialMessageClass = serialMessage.getMessageClass();
    // this.expectedReplyClass = expectedReplyClass;
    // this.expectedReplyCommandClass = expectedReplyCommandClass;
    // this.expectedReplyCommandClassCommand = expectedReplyCommandClassCommand;
    // this.priority = priority;
    // this.attemptsRemaining = 3;
    // this.requiresData = requiresData;
    // this.dataTimeout = dataTimeout;
    // }

    public ZWaveTransaction(final ZWaveMessagePayloadTransaction payload) {
        // this.serialMessageClass = payload.getSerialMessageClass();
        // this.expectedReplyClass = payload.getExpectedResponseSerialMessageClass();
        this.priority = payload.getPriority();
        this.dataTimeout = payload.getTimeout();
        if (this.dataTimeout < 250) {
            this.dataTimeout = DEFAULT_TIMEOUT;
        }
        this.attemptsRemaining = payload.getMaxAttempts();
        if (this.attemptsRemaining == 0) {
            this.attemptsRemaining = 3;
        }
        this.payload = payload;
    }

    public void resetTransaction() {
        logger.debug("Transaction RESET with {} retries remaining.", attemptsRemaining);
        transactionStateTracker = TransactionState.UNINTIALIZED;
    }

    public void transactionStart() {
        startTime = System.currentTimeMillis();

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

    // public void setSerialMessage(SerialMessage serialMessage) {
    // this.serialMessage = serialMessage;
    // }

    public SerialMessage getSerialMessage() {
        SerialMessage serialMessage = payload.getSerialMessage();

        // Debug
        serialMessageDebug = serialMessage;

        // We need to remember the callback Id
        callbackId = serialMessage.getCallbackId();

        return serialMessage;
    }

    public SerialMessageClass getSerialMessageClass() {
        return payload.getSerialMessageClass();
    }

    public int getNodeId() {
        return payload.getDestinationNode();
    }

    public int getQueueId() {
        if (payload.getSerialMessageClass() == SerialMessageClass.SendData) {
            return 255;
        }
        return payload.getDestinationNode();
    }

    public SerialMessageClass getExpectedReplyClass() {
        return payload.getExpectedResponseSerialMessageClass();
    }

    public CommandClass getExpectedCommandClass() {
        return expectedReplyCommandClass;
    }

    public Integer getExpectedCommandClassCommand() {
        return expectedReplyCommandClassCommand;
    }

    public TransactionState getTransactionState() {
        return transactionStateTracker;
    }

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
        transactionStateCancelled = transactionStateTracker;
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

    public boolean requiresDataBeforeNextRelease() {
        return payload.requiresData();
    }

    public long getDataTimeout() {
        return dataTimeout;
    }

    public boolean transactionAdvance(SerialMessage incomingMessage) {
        logger.debug("TransactionAdvance ST: {}", transactionStateTracker);
        logger.debug("TransactionAdvance TX: {}", serialMessageDebug);
        logger.debug("TransactionAdvance WT: {}", payload.getExpectedResponseSerialMessageClass());
        logger.debug("TransactionAdvance RX: {}", incomingMessage);

        System.out.println("TransactionAdvance ST: " + transactionStateTracker);
        System.out.println("TransactionAdvance TX: " + serialMessageDebug);
        System.out.println("TransactionAdvance WT: " + payload.getExpectedResponseSerialMessageClass());
        System.out.println("TransactionAdvance RX: " + incomingMessage);

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

                // TODO: Ultimately, getExpectedReply should return null if we're not waiting for data
                if (payload.getExpectedResponseSerialMessageClass() != null
                        && payload.getExpectedResponseSerialMessageClass() != incomingMessage.getMessageClass()) {
                    transactionStateTracker = TransactionState.WAIT_DATA;
                    break;
                }

                // We don't need anything else
                transactionStateTracker = TransactionState.DONE;
                break;

            case WAIT_REQUEST:
                if (incomingMessage.getMessageClass() != payload.getSerialMessageClass()
                        || incomingMessage.getMessageType() != SerialMessageType.Request) {
                    break;
                }

                if (incomingMessage.getCallbackId() != getCallbackId()) {
                    break;
                }

                // We've received our request - advance
                // TODO: Ultimately, getExpectedReply should return null if we're not waiting for data
                if (payload.getExpectedResponseSerialMessageClass() != null) { // && expectedReplyClass !=
                                                                               // incomingMessage.getMessageClass()) {
                    transactionStateTracker = TransactionState.WAIT_DATA;
                    break;
                }
                transactionStateTracker = TransactionState.DONE;
                break;

            case WAIT_DATA:
                System.out.println("WAIT_DATA -- 1");

                if (incomingMessage.getMessageClass() != payload.getExpectedResponseSerialMessageClass()
                        || incomingMessage.getMessageType() != SerialMessageType.Request) {
                    System.out.println("WAIT_DATA -- 2");
                    break;
                }

                // Check if the nodeId is correct
                if (incomingMessage.getMessageType() == SerialMessageType.Request && payload.getDestinationNode() != 255
                        && payload.getDestinationNode() != incomingMessage.getMessageNode()) {
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

    @Override
    public String toString() {
        return transactionStateTracker + ": callback: " + callbackId;
    }

    public long getTransactionId() {
        return transactionId;
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

        if (getSerialMessage().equals(other.getSerialMessage())) {
            return true;
        }

        return false;
    }
}
