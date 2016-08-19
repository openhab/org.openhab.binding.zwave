package org.openhab.binding.zwave.internal.protocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles transactions for the Z-Wave controller. A transaction contains a set of messages required
 * to transfer data to a device, and potentially receive data back.
 * <p>
 * There can be a number of different transaction types -:
 * <ul>
 * <li><i>REQ</i>uest from openHAB, no response other than the low level <i>ACK</i></li>
 * <li><i>REQ</i>uest from openHAB, <i>RES</i>ponse from controller</li>
 * <li><i>REQ</i>uest from openHAB, <i>REQ</i>uest from controller</li>
 * <li><i>REQ</i>uest from openHAB, <i>RES</i>ponse from controller, <i>REQ</i>uest from controller</li>
 * <li><i>REQ</i>uest from openHAB, <i>RES</i>ponse from controller, <i>REQ</i>uest from controller,
 * <i>DATA</i> from node</li>
 * </ul>
 * The responses required for each transaction is defined in the {@link SerialMessage} class and these are
 * tracked in this class.
 * </p>
 * <p>
 * Most transactions are SendData messages which request data from a device. These are the last type of transaction.
 * The meaning of each step is as follows -:
 * <ul>
 * <li><i>REQ</i>uest sent from openHAB sends the command class data.</li>
 * <li><i>RES</i>ponse from the controller advises that the controller accepts the message and it will be sent to the
 * device.
 * At this time, transactions to other nodes <u>could</u> be sent. Transactions are tracked using a callback ID,</li>
 * <li><i>REQ</i>uest from the controller advises that the device has received the message (or the message has timed
 * out).</li>
 * <li><i>DATA</i> message from the device is received.</li>
 * </ul>
 * This definition allows multiple messages to be outstanding at once. From a Z-Wave API perspective, the <i>DATA</i>
 * response
 * is outside of a controllers transaction, so we are allowed to have multiple transactions. However, we don't want to
 * overload a device, so we need to restrict requests to any node to ensure only a single transaction is outstanding at
 * once.
 * </p>
 * <p>
 * The following transaction management 'rules' are applied -:
 * <ul>
 * <li>Only a single transaction still awaiting a <i>RES</i>ponse can be outstanding to ANY node.</li>
 * <li>Only a single transaction still awaiting a <i>REQ</i>uest can be outstanding to a specific node.</li>
 * <li>Only a single transaction requiring a <i>DATA</i> response can be released at once to a specific node.</li>
 * <li>A total of MAX_OUTSTANDING_TRANSACTIONS can be outstanding at once.</li>
 * </ul>
 * </p>
 * <h2>Transaction Flow</h2>
 * <p>
 * Transactions are processed as follows -:
 * <ul>
 * <li>{@link ZWaveTransactions} containing the {@link SerialMessage} are added to the transmit queue with
 * {@link #queueTransactionForSend} method.</li>
 * <li>If a transaction can be sent, then it is transmitted. The sent transaction is added to the sent
 * transaction list so that received messages can be correlated with the transaction and timeouts can be managed.</li>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 * </p>
 * <h2>TX Message Queue</h2>
 * <p>
 * Messages are queued in this class. This allows transmit messages to be released with knowledge of
 * other transactions that are outstanding. Specifically, we only want to release a single transaction
 * per node at once (with the exception below).
 * </p>
 * <p>
 * This class provides a semaphore to block the send thread (or maybe this should directly call the send method?)
 * </p>
 * <p>
 * Only a single transaction requiring a response can be released at once to any single node. This
 * specifically allows NONCE responses to be sent in the middle of a secure transaction.
 * <p>
 * <h2>Timeouts</h2>
 * <p>
 * A timer thread manages timeouts - different times are used for the different stages of a transaction.
 * Defaults for each timer are as follows -:
 * <ul>
 * <li><i>RES</i>ponse - should be received within <b>250ms</b> of the <i>REQ</i>uest</li>
 * <li><i>REQ</i>uest - should be received within <b>2500ms</b> of the <i>REQ</i>uest or the controllers <i>RES</i>ponse
 * </li>
 * <li><i>DATA</i> - should be received within <b>2500ms</b> of the <i>REQ</i>uest.</li>
 * </ul>
 * When a timeout occurs - what????
 * </p>
 * <h2>Cancelled Transactions</h2>
 * <p>
 * Transactions can be cancelled by a message handler - for example if there's a timeout in a transaction we can
 * cancel the transaction without waiting for the DATA response.
 * </p>
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZWaveTransactionManager {
    private Logger logger = LoggerFactory.getLogger(ZWaveTransactionManager.class);

    // private static final int ZWAVE_RESPONSE_TIMEOUT = 5000;
    private static final int INITIAL_TX_QUEUE_SIZE = 128;
    private static final int MAX_OUTSTANDING_TRANSACTIONS = 2;

    private ZWaveController controller;

    private final long timer1 = 500;
    private final long timer2 = 2500;
    private final long timer3 = 2500;

    private final Timer timer = new Timer();
    private TimerTask timerTask = null;
    private final PriorityBlockingQueue<ZWaveTransaction> sendQueue = new PriorityBlockingQueue<ZWaveTransaction>(
            INITIAL_TX_QUEUE_SIZE, new ZWaveTransactionComparator());

    private final List<ZWaveTransaction> outstandingTransactions = new ArrayList<ZWaveTransaction>();

    private ZWaveTransaction lastTransaction = null;

    private final Object transactionSync = new Object();

    public ZWaveTransactionManager(ZWaveController controller) {
        this.controller = controller;
    }

    /**
     * Add a transaction to the send queue
     *
     * @param transaction
     */
    public void queueTransactionForSend(ZWaveTransaction transaction) {
        if (sendQueue.contains(transaction)) {
            logger.debug("NODE {}: Transaction already on the send queue. Removing original.",
                    transaction.getMessageNode());
            sendQueue.remove(transaction);
        }

        // Add the message to the queue
        // We always add the most recent version, even though they are supposedly the same,
        // in case things like priority have changed
        sendQueue.add(transaction);

        sendNextMessage();
    }

    /**
     * Gets the number of messages currently in the transmit queue
     *
     * @return
     */
    public int getSendQueueLength() {
        return sendQueue.size();
    }

    public void clearSendQueue() {
        sendQueue.clear();
    }

    /**
     * Gets the next transaction to be sent.
     *
     * This returns the transaction being sent. All processing of the transaction, including retries etc
     * is handled within the transaction handler.
     *
     * @return
     */
    public ZWaveTransaction getTransactionToSend() {
        return sendQueue.poll();
    }

    /**
     * Processes an incoming {@link SerialMessage}
     * This is called by the receive processing queue.
     *
     * @param incomingMessage
     */
    public void processReceiveMessage(SerialMessage incomingMessage) {
        ZWaveTransaction currentTransaction = null;
        System.out.println("Received msg " + incomingMessage.toString());
        System.out.println("lastTransaction = " + lastTransaction);

        if (incomingMessage.getMessageClass() == SerialMessageClass.SendData
                && incomingMessage.getMessageType() == SerialMessageType.Request) {
            System.out.println("Message is SendData");
        }

        synchronized (transactionSync) {
            System.out.println("Checking outstanding transactions: " + outstandingTransactions.size());
            System.out.println("Last transaction: " + lastTransaction);

            // If we are waiting for the RESponse, then check for this first
            // There can only be a single outstanding RESponse
            if (incomingMessage.getMessageType() == SerialMessageType.Response) {
                if (lastTransaction == null) {
                    System.out.println("RESponse received when we weren't expecting one!");
                    return;
                }

                System.out.println("Message is RESponse " + incomingMessage.getMessageClass() + "   "
                        + lastTransaction.getSerialMessageClass());
                if (incomingMessage.getMessageClass() != lastTransaction.getSerialMessageClass()) {
                    System.out.println("Message class RESponse error");
                    return;
                }
                currentTransaction = lastTransaction;

                System.out.println("Transaction " + currentTransaction.getCallbackId() + " is last transaction");

                // No need to track this transaction now
                // lastTransaction = null;
            } else {
                // Try and correlate this incoming REQuest with a transaction
                for (ZWaveTransaction transaction : outstandingTransactions) {
                    System.out.println("checking transaction " + transaction.getCallbackId() + "......");

                    // If we are waiting for the RESponse, then check for this first
                    // There can only be a single outstanding RESponse
                    if (incomingMessage.getMessageType() == SerialMessageType.Response) {

                        continue;
                    }

                    ZWaveCommandProcessor msgClass = ZWaveCommandProcessor
                            .getMessageDispatcher(incomingMessage.getMessageClass());
                    if (msgClass != null && msgClass.correlateTransactionResponse(transaction, incomingMessage)) {
                        System.out.println("Correlated to transaction " + transaction.getCallbackId() + "......");
                        currentTransaction = transaction;
                        break;
                    }
                }
            }
        }

        controller.handleIncomingMessage(currentTransaction, incomingMessage);

        // Handle transaction processing
        if (currentTransaction == null) {
            System.out.println("Not correlated with transaction");
            logger.debug("****************** Transaction not correlated");
            return;
        }

        // Handle the transaction state machine
        boolean transactionCompleted = false;
        if (currentTransaction.transactionAdvance(incomingMessage) == true) {
            System.out.println("Transaction " + currentTransaction.getCallbackId() + " advanced to "
                    + currentTransaction.getTransactionState());
            // Transaction has advanced - update the timer.
            currentTransaction.setTimeout(getNextTimer(currentTransaction));
            startTransactionTimer();
        }

        switch (currentTransaction.getTransactionState()) {
            case WAIT_DATA:
                // No need to track this transaction now
                if (currentTransaction == lastTransaction) {
                    lastTransaction = null;
                }
                break;

            case CANCELLED:
                // Transaction was cancelled
                controller.handleTransactionComplete(currentTransaction, incomingMessage);

                // Handle retries
                if (currentTransaction.decrementAttemptsRemaining() >= 0) {
                    logger.error("NODE {}: ABORT while sending message. Requeueing - {} attempts left!",
                            currentTransaction.getMessageNode(), currentTransaction.getAttemptsRemaining());
                    // if (lastSentMessage.getMessageClass() == SerialMessageClass.SendData) {
                    // handleFailedSendDataRequest(lastSentMessage);
                    // } else {

                    // Reset the transaction
                    currentTransaction.resetTransaction();

                    // Lower the priority since it's a retry!
                    // lastSentMessage.setPriority(p);
                    // enqueue(currentTransaction); TODO: Handle retries...
                    // }
                } else {
                    logger.warn("NODE {}: Too many retries. Discarding message: {}",
                            currentTransaction.getMessageNode(), currentTransaction.toString());
                    transactionCompleted = true;
                }

                break;

            case DONE:
                controller.handleTransactionComplete(currentTransaction, incomingMessage);

                // if (responseTime > longestResponseTime) {
                // longestResponseTime = responseTime;
                // }
                logger.debug("NODE {}: Response processed after {}ms", currentTransaction.getMessageNode(),
                        currentTransaction.getElapsedTime());

                transactionCompleted = true;
                break;

            default:
                // logger.error("Unhandled transaction state {}", lastSentMessage.getTransactionState());
                break;
        }

        if (transactionCompleted == true) {
            resetTransactionTimer();
            System.out.println("Transaction " + currentTransaction.getCallbackId() + " completed");
            logger.debug("NODE {}: **** Transaction completed", currentTransaction.getMessageNode());

            // Remove the transaction from the
            synchronized (transactionSync) {
                if (currentTransaction == lastTransaction) {
                    lastTransaction = null;
                }

                outstandingTransactions.remove(currentTransaction);
            }
        } else {
            System.out.println("Transaction " + currentTransaction.getCallbackId() + " NOT completed");
            logger.debug("NODE {}: **** Transaction not completed", currentTransaction.getMessageNode());
        }

        // See if we need to send another message
        sendNextMessage();
    }

    private Date getNextTimer(ZWaveTransaction transaction) {
        long nextTimer = 0;
        switch (transaction.getTransactionState()) {
            case CANCELLED:
            case DONE:
            case UNINTIALIZED:
                break;
            case WAIT_RESPONSE:
                System.out.println("Timer type WAIT_RESPONSE");
                nextTimer = System.currentTimeMillis() + timer1;
                break;
            case WAIT_REQUEST:
                System.out.println("Timer type WAIT_REQUEST");
                nextTimer = System.currentTimeMillis() + timer2;
                break;
            case WAIT_DATA:
                System.out.println("Timer type WAIT_DATA");
                nextTimer = System.currentTimeMillis() + timer3;
                break;
        }

        if (nextTimer == 0) {
            return null;
        }

        return new Date(nextTimer);
    }

    private void sendNextMessage() {
        // If we're currently processing the core of a transaction, or there are too many outstanding transactions, then
        // don't start another right now.
        synchronized (transactionSync) {
            if (lastTransaction != null || outstandingTransactions.size() >= MAX_OUTSTANDING_TRANSACTIONS) {
                return;
            }

            final ZWaveTransaction transaction = getTransactionToSend();
            if (transaction == null) {
                // Nothing to send!
                return;
            }

            // Add this message to the outstandingTransactions list

            controller.sendPacket(transaction.getSerialMessage());
            transaction.transactionStart();
            outstandingTransactions.add(transaction);
            System.out.println("Sending message " + transaction.getSerialMessage().toString());
            System.out.println("Transactions outstanding: " + outstandingTransactions.size());
            transaction.setTimeout(getNextTimer(transaction));
            startTransactionTimer();
            lastTransaction = transaction;
        }
    }

    private synchronized void startTransactionTimer() {
        // Stop any existing timer
        resetTransactionTimer();

        // Find the time till the next timer
        Date nextTimer = null;
        for (ZWaveTransaction transaction : outstandingTransactions) {
            if (nextTimer == null) {
                nextTimer = transaction.getTimeout();
                continue;
            }

            Date time = transaction.getTimeout();
            if (time != null && time.before(nextTimer)) {
                nextTimer = time;
            }
        }

        // Create the timer task
        timerTask = new ZWaveTransactionTimer();

        // Start the timer if required
        if (nextTimer != null) {
            logger.debug("Start transaction timer to {}", nextTimer);
            System.out.println("Start transaction timer to " + (nextTimer.getTime() - System.currentTimeMillis())
                    + "     " + nextTimer.getTime());
            timer.schedule(timerTask, nextTimer);
        }
    }

    private synchronized void resetTransactionTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            System.out.println("STOP transaction timer");
            logger.debug("STOP transaction timer");

            timerTask.cancel();
            timerTask = null;
        }
    }

    private class ZWaveTransactionTimer extends TimerTask {
        private final Logger logger = LoggerFactory.getLogger(ZWaveTransactionTimer.class);

        @Override
        public void run() {
            logger.debug("Timeout.......... {} outstanding transactions", outstandingTransactions.size());
            System.out.println("Timer run..... " + System.currentTimeMillis());
            synchronized (transactionSync) {
                logger.debug("Timeout.......... {} outstanding transactions", outstandingTransactions.size());
                Date now = new Date();
                List<ZWaveTransaction> retries = new ArrayList<ZWaveTransaction>();

                Iterator<ZWaveTransaction> iterator = outstandingTransactions.iterator();
                while (iterator.hasNext()) {
                    ZWaveTransaction transaction = iterator.next();
                    Date timer = transaction.getTimeout();
                    if (timer != null && timer.after(now) == false) {
                        // Timeout
                        logger.debug("NODE {}: Timeout at state {}. {} retries remaining.",
                                transaction.getSerialMessage().getMessageNode(), transaction.getTransactionState(),
                                transaction.getAttemptsRemaining());

                        // If this is the current transaction, then reset it.
                        if (lastTransaction == transaction) {
                            lastTransaction = null;
                        }

                        // Remove this transaction from the outstanding transactions list
                        iterator.remove();

                        // Resend if there are still attempts remaining
                        if (transaction.decrementAttemptsRemaining() == 0) {
                            transaction.setTransactionCanceled();
                            controller.handleTransactionComplete(transaction, null);
                        } else {
                            // Resend
                            transaction.resetTransaction();
                            retries.add(transaction);
                        }
                    }
                }

                // If there's no outstanding transaction, try and send one
                for (ZWaveTransaction transaction : retries) {
                    System.out.println("Resending... " + transaction.getSerialMessage().getCallbackId());
                    queueTransactionForSend(transaction);
                }

                sendNextMessage();
            }
        }
    }
}
