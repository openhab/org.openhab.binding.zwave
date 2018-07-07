/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionState;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionResponse.State;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveCommandProcessor;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
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

    private final int INITIAL_TX_QUEUE_SIZE = 128;
    // private final int MAX_OUTSTANDING_TRANSACTIONS = 3;

    private final int TRANSMIT_OPTION_ACK = 0x01;
    private final int TRANSMIT_OPTION_AUTO_ROUTE = 0x04;
    private final int TRANSMIT_OPTION_EXPLORE = 0x20;

    /**
     * Holdoff delay in milliseconds. This is used to delay the next transaction if the controller sends an error while
     * waiting for a response.
     */
    private final int HOLDOFF_DELAY = 250;

    private Boolean holdoffActive = Boolean.FALSE;

    private Calendar holdoffDelay = Calendar.getInstance();

    /**
     * The controller used by this transaction manager
     */
    private ZWaveController controller;

    /**
     * Timeout in which we expect the initial response from the controller
     */
    private final long timer1 = 2000;

    /**
     * Timeout in which we expect the request from the controller
     */
    private final long timer2 = 5000;

    /**
     * Timeout waiting to cancel a transaction once we've aborted it
     */
    private final long timerAbort = 12000;

    private final Timer timer = new Timer();
    private TimerTask timerTask = null;

    private final BlockingQueue<SerialMessage> recvQueue;

    private final PriorityBlockingQueue<ZWaveTransaction> sendQueue = new PriorityBlockingQueue<ZWaveTransaction>(
            INITIAL_TX_QUEUE_SIZE, new ZWaveTransactionComparator());
    private final PriorityBlockingQueue<ZWaveTransaction> secureQueue = new PriorityBlockingQueue<ZWaveTransaction>(
            INITIAL_TX_QUEUE_SIZE, new ZWaveTransactionComparator());
    private final PriorityBlockingQueue<ZWaveTransaction> controllerQueue = new PriorityBlockingQueue<ZWaveTransaction>(
            INITIAL_TX_QUEUE_SIZE, new ZWaveTransactionComparator());
    private final PriorityBlockingQueue<ZWaveTransaction> priorityControllerQueue = new PriorityBlockingQueue<ZWaveTransaction>(
            INITIAL_TX_QUEUE_SIZE, new ZWaveTransactionComparator());

    private ZWaveReceiveThread receiveThread;

    ExecutorService executor = Executors.newCachedThreadPool();
    final List<TransactionListener> transactionListeners = new ArrayList<TransactionListener>();

    private final List<ZWaveTransaction> outstandingTransactions = new ArrayList<ZWaveTransaction>();

    private ZWaveTransaction lastTransaction = null;

    public ZWaveTransactionManager(ZWaveController controller) {
        this.controller = controller;

        recvQueue = new ArrayBlockingQueue<SerialMessage>(INITIAL_TX_QUEUE_SIZE);

        receiveThread = new ZWaveReceiveThread();
        receiveThread.start();
    }

    private void AddTransactionListener(TransactionListener listener) {
        synchronized (transactionListeners) {
            if (transactionListeners.contains(listener)) {
                return;
            }

            transactionListeners.add(listener);
        }
    }

    private void RemoveTransactionListener(TransactionListener listener) {
        synchronized (transactionListeners) {
            transactionListeners.remove(listener);
        }
    }

    private void notifyTransactionComplete(final ZWaveTransaction transaction) {
        logger.debug("NODE {}: notifyTransactionResponse TID:{} {}", transaction.getNodeId(),
                transaction.getTransactionId(), transaction.getTransactionState());
        new Thread() {
            @Override
            public void run() {
                synchronized (transactionListeners) {
                    for (TransactionListener listener : transactionListeners) {
                        listener.transactionEvent(transaction);
                    }
                }
            }
        }.start();

        // If this transaction isn't complete, check if it's a secure transaction as we need to
        // abort the original request.
        if (transaction.getTransactionState() != TransactionState.DONE
                && transaction instanceof ZWaveSecureTransaction) {
            ZWaveSecureTransaction secureTransaction = (ZWaveSecureTransaction) transaction;
            logger.debug("NODE {}: processing secure transaction -- TID:{}", transaction.getNodeId(),
                    secureTransaction.getLinkedTransaction().getTransactionId());

            synchronized (transactionListeners) {
                for (TransactionListener listener : transactionListeners) {
                    listener.transactionEvent(secureTransaction.getLinkedTransaction());
                }
            }

            synchronized (sendQueue) {
                sendQueue.remove(secureTransaction.getLinkedTransaction());
            }
        }
    }

    /**
     * This method takes a {@link ZWaveMessagePayloadTransaction} and creates a {@link ZWaveTransaction} for a Security
     * SendData message.
     *
     * @param payload {@link ZWaveMessagePayloadTransaction}
     * @return the transaction id
     */
    public long queueNonceReportForSend(ZWaveMessagePayloadTransaction payload) {
        // Create a transaction from our payload data
        ZWaveTransaction transaction = new ZWaveTransaction(payload);
        if (payload.getMaxAttempts() != 0) {
            transaction.setAttemptsRemaining(payload.getMaxAttempts());
        }
        transaction.getSerialMessageClass();

        // Add the transaction to the queue
        secureQueue.add(transaction);
        logger.debug("NODE {}: Added to secure queue - size {}", transaction.getNodeId(), secureQueue.size());

        return transaction.getTransactionId();
    }

    /**
     * Queues a transaction for sending.
     *
     * @param payload the {@link ZWaveMessagePayloadTransaction}
     * @return the transaction ID
     */
    public long queueTransactionForSend(ZWaveMessagePayloadTransaction payload) {
        // Create a transaction from our payload data
        ZWaveTransaction transaction = new ZWaveTransaction(payload);
        if (payload.getMaxAttempts() != 0) {
            transaction.setAttemptsRemaining(payload.getMaxAttempts());
        }

        if (transaction.getNodeId() != 255) {
            ZWaveNode node = controller.getNode(transaction.getNodeId());
            if (node != null && !node.isListening()) {
                logger.debug("NODE {}: Bump transaction {} priority from {} to Immediate", transaction.getNodeId(),
                        transaction.getTransactionId(), transaction.getPriority());
                transaction.setPriority(TransactionPriority.Immediate);
            }
        }

        // Add the transaction to the queue
        addTransactionToQueue(transaction);

        return transaction.getTransactionId();
    }

    void addTransactionToQueue(ZWaveTransaction transaction) {
        synchronized (sendQueue) {
            PriorityBlockingQueue<ZWaveTransaction> queue;

            // Either put this into the controller queue, or the device queue
            // The controller queue is used for commands solely handled by the controller
            // and we do not check if nodes are awake.
            if (transaction.getSerialMessageClass().requiresNode()) {
                queue = sendQueue;
                logger.debug("NODE {}: Adding to device queue", transaction.getNodeId());
            } else {
                transaction.getSerialMessageClass();
                if (transaction.getSerialMessageClass() == SerialMessageClass.AddNodeToNetwork
                        || transaction.getSerialMessageClass() == SerialMessageClass.RemoveNodeFromNetwork
                        || transaction.getSerialMessageClass() == SerialMessageClass.IdentifyNode) {
                    queue = priorityControllerQueue;
                    logger.trace("NODE {}: Adding to priority controller queue", transaction.getNodeId());
                } else {
                    queue = controllerQueue;
                    logger.trace("NODE {}: Adding to controller queue", transaction.getNodeId());
                }
            }

            if (queue.contains(transaction)) {
                logger.debug("NODE {}: Transaction already in queue - removing original", transaction.getNodeId());
                queue.remove(transaction);
            }
            queue.add(transaction);
            logger.trace("NODE {}: Added to queue - size {}", transaction.getNodeId(), queue.size());
        }

        sendNextMessage();
        startTransactionTimer();
    }

    /**
     * Gets the number of messages currently in the transmit queue for a specific node. This includes transactions that
     * are in the outstanding queue.
     *
     * @return number of messages in queue
     */
    public int getSendQueueLength(int nodeId) {
        if (nodeId == 255) {
            return controllerQueue.size();
        }

        synchronized (sendQueue) {
            int outstandingCount = 0;
            for (ZWaveTransaction transaction : secureQueue) {
                if (transaction.getNodeId() == nodeId) {
                    outstandingCount++;
                }
            }
            for (ZWaveTransaction transaction : sendQueue) {
                if (transaction.getNodeId() == nodeId) {
                    outstandingCount++;
                }
            }
            for (ZWaveTransaction transaction : outstandingTransactions) {
                if (transaction.getNodeId() == nodeId) {
                    outstandingCount++;
                }
            }

            return outstandingCount;
        }
    }

    /**
     * Clear the send queue
     */
    public void clearSendQueue() {
        synchronized (sendQueue) {
            sendQueue.clear();
            secureQueue.clear();
            controllerQueue.clear();
            priorityControllerQueue.clear();
        }
    }

    /**
     * Processes an incoming {@link SerialMessage}
     * This is called by the receive processing queue.
     *
     * @param incomingMessage
     */
    public void processReceiveMessage(SerialMessage incomingMessage) {
        logger.debug("processReceiveMessage input {}<>{} : {}", recvQueue.size(), recvQueue.remainingCapacity(),
                incomingMessage.toString());

        synchronized (recvQueue) {
            logger.debug("processReceiveMessage past lock {}", incomingMessage.toString());
            recvQueue.add(incomingMessage);
            recvQueue.notify();
        }
    }

    private class ZWaveReceiveThread extends Thread {
        @Override
        public void run() {
            SerialMessage incomingMessage;
            while (!interrupted()) {
                if (recvQueue.isEmpty()) {
                    logger.debug("ZWaveReceiveThread queue empty");

                    // See if we need to send another message
                    sendNextMessage();
                    startTransactionTimer();
                }

                try {
                    incomingMessage = recvQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }

                ZWaveTransaction currentTransaction = null;
                logger.debug("Received msg ({}): {}", recvQueue.size(), incomingMessage.toString());
                logger.debug("lastTransaction {}", lastTransaction);

                // Check for NAK/CAN
                switch (incomingMessage.getMessageType()) {
                    case ACK:
                        logger.debug("Received msg: ACK");
                        continue;
                    case NAK:
                        // NAK means the controller didn't receive the message - probably because of a Checksum error
                    case CAN:
                        if (lastTransaction == null) {
                            continue;
                        }

                        // Let's hold off sending anything for a short time to let things settle
                        startHoldoffTimer();

                        // CAN means out of flow message was received by the controller
                        // It probably means we sent a message while the controller was processing the previous message.
                        logger.debug("TID {}: Resetting transaction", lastTransaction.getTransactionId());

                        notifyTransactionComplete(lastTransaction);

                        // Remove the transaction
                        synchronized (sendQueue) {
                            outstandingTransactions.remove(lastTransaction);
                        }

                        // Requeue...
                        addTransactionToQueue(lastTransaction);
                        lastTransaction = null;
                        continue;
                    default:
                        break;
                }

                // Manage incoming command class messages separately so we can manage transaction responses
                if (incomingMessage.getMessageClass() == SerialMessageClass.ApplicationCommandHandler) {
                    try {
                        int nodeId = incomingMessage.getMessagePayloadByte(1);
                        ZWaveNode node = controller.getNode(nodeId);

                        if (node == null) {
                            logger.warn("NODE {}: Not initialized (ie node unknown), ignoring message.", nodeId);
                        } else {
                            logger.debug("NODE {}: Application Command Request ({}:{})", nodeId,
                                    node.getNodeState().toString(), node.getNodeInitStage().toString());

                            List<ZWaveCommandClassPayload> commands;
                            try {
                                commands = node.processCommand(new ZWaveCommandClassPayload(incomingMessage));
                            } catch (Exception e) {
                                commands = null;
                                logger.debug("Exception processing frame: {}: ", incomingMessage, e);
                            }
                            if (commands != null) {
                                logger.debug("NODE {}: Commands processed {}.", nodeId, commands.size());

                                for (ZWaveCommandClassPayload command : commands) {
                                    logger.debug("NODE {}: Checking command {}.", nodeId, command);
                                    // Correlate transactions
                                    List<ZWaveTransaction> completed = new ArrayList<ZWaveTransaction>();

                                    synchronized (sendQueue) {
                                        for (ZWaveTransaction transaction : outstandingTransactions) {
                                            logger.trace("NODE {}: Checking transaction {}  {}.", nodeId,
                                                    transaction.getTransactionId(),
                                                    transaction.getExpectedReplyClass());
                                            logger.trace("NODE {}: Checking transaction : state >> {}", nodeId,
                                                    transaction.getTransactionState());
                                            logger.trace("NODE {}: Checking transaction : node  >> {}", nodeId,
                                                    transaction.getNodeId());
                                            logger.trace("NODE {}: Checking transaction : class >> {} == {}.", nodeId,
                                                    command.getCommandClassId(),
                                                    transaction.getExpectedCommandClass() == null ? null
                                                            : transaction.getExpectedCommandClass().getKey());
                                            logger.trace("NODE {}: Checking transaction : commd >> {} == {}.", nodeId,
                                                    command.getCommandClassCommand(),
                                                    transaction.getExpectedCommandClassCommand());

                                            if (transaction.getTransactionState() != TransactionState.WAIT_DATA) {
                                                logger.trace(
                                                        "NODE {}: Ignoring transaction since not waiting for data.",
                                                        nodeId);
                                                continue;
                                            }

                                            if (transaction
                                                    .getExpectedReplyClass() == SerialMessageClass.ApplicationCommandHandler
                                                    && transaction.getExpectedCommandClass() != null
                                                    && command.getCommandClassId() == transaction
                                                            .getExpectedCommandClass().getKey()
                                                    && nodeId == transaction.getNodeId()
                                                    && command.getCommandClassCommand() == transaction
                                                            .getExpectedCommandClassCommand()) {
                                                logger.debug("NODE {}: Command verified {}.", nodeId, command);

                                                transaction.transactionAdvance(incomingMessage);

                                                // Notify the sender
                                                notifyTransactionComplete(transaction);

                                                // Notify the controller
                                                controller.handleTransactionComplete(transaction, incomingMessage);

                                                // Remove the transaction from the outstanding transaction list
                                                if (transaction == lastTransaction) {
                                                    lastTransaction = null;
                                                }
                                                completed.add(transaction);

                                                // Handle secure transactions - these are ones where we have
                                                // requested a NONCE which we've just received, and we now need
                                                // to encrypt and send the original message
                                                if (transaction instanceof ZWaveSecureTransaction) {
                                                    secureQueue.add(((ZWaveSecureTransaction) transaction)
                                                            .getLinkedTransaction());
                                                }
                                            } else {
                                                logger.debug("NODE {}: Command NOT verified {}.", nodeId, command);
                                            }
                                        }

                                        logger.debug("Transaction completed - outstandingTransactions {}",
                                                outstandingTransactions.size());
                                        outstandingTransactions.removeAll(completed);
                                        logger.debug("Transaction completed - outstandingTransactions {}",
                                                outstandingTransactions.size());
                                    }
                                }
                            }
                        }
                    } catch (ZWaveSerialMessageException e) {
                        logger.error("Error processing frame: {} >> {}", incomingMessage.toString(), e.getMessage());
                    }
                    continue;
                }

                synchronized (sendQueue) {
                    logger.debug("Checking outstanding transactions: {}", outstandingTransactions.size());
                    logger.debug("Last transaction: {}", lastTransaction);

                    // If we are waiting for the RESponse, then check for this first
                    // There can only be a single outstanding RESponse
                    if (incomingMessage.getMessageType() == SerialMessageType.Response) {
                        if (lastTransaction == null) {
                            continue;
                        }

                        if (incomingMessage.getMessageClass() != lastTransaction.getSerialMessageClass()) {
                            continue;
                        }
                        currentTransaction = lastTransaction;
                    } else {
                        // Try and correlate this incoming REQuest with a transaction
                        for (ZWaveTransaction transaction : outstandingTransactions) {
                            logger.debug("Checking TID {}: (Callback {})", transaction.getTransactionId(),
                                    transaction.getCallbackId());

                            ZWaveCommandProcessor msgClass = ZWaveCommandProcessor
                                    .getMessageDispatcher(incomingMessage.getMessageClass());
                            if (msgClass != null
                                    && msgClass.correlateTransactionResponse(transaction, incomingMessage)) {
                                logger.debug("Correlated to TID {}: callback {}", transaction.getTransactionId(),
                                        transaction.getCallbackId());
                                currentTransaction = transaction;
                                break;
                            }
                        }
                    }
                }

                // Process low level messages
                // TODO: Maybe this should be moved?
                controller.handleIncomingMessage(currentTransaction, incomingMessage);

                // Handle transaction processing
                if (currentTransaction != null) {
                    // Handle the transaction state machine
                    boolean transactionCompleted = false;
                    if (currentTransaction.transactionAdvance(incomingMessage) == true) {
                        logger.debug("TID {}: Advanced to {}", currentTransaction.getTransactionId(),
                                currentTransaction.getTransactionState());
                        // Transaction has advanced - update the timer.
                        currentTransaction.setTimeout(getNextTimer(currentTransaction));
                    }

                    switch (currentTransaction.getTransactionState()) {
                        case WAIT_DATA:
                            // No need to track this transaction now
                            if (currentTransaction == lastTransaction
                                    && currentTransaction.requiresDataBeforeNextRelease() == false) {
                                lastTransaction = null;
                            } else if (currentTransaction.getWaitForResponse()) {

                            }
                            break;

                        case DONE:
                            // Remove the transaction from the outstanding transaction list
                            synchronized (sendQueue) {
                                if (currentTransaction == lastTransaction) {
                                    lastTransaction = null;
                                }

                                outstandingTransactions.remove(currentTransaction);
                            }

                            logger.debug("NODE {}: Response processed after {}ms", currentTransaction.getNodeId(),
                                    currentTransaction.getElapsedTime());

                            // Notify our users...
                            transactionCompleted = true;
                            break;

                        case CANCELLED:
                            // If the transaction failed getting the response from the controller, then add a delay
                            // before sending more traffic to let the controller sort its self out.
                            // We do this first to avoid any possible retransmissions.
                            if (currentTransaction.getTransactionCancelledState() == TransactionState.WAIT_RESPONSE) {
                                startHoldoffTimer();
                            }

                            // Transaction was cancelled
                            controller.handleTransactionComplete(currentTransaction, incomingMessage);

                            // Remove the transaction from the outstanding transaction list
                            synchronized (sendQueue) {
                                if (currentTransaction == lastTransaction) {
                                    lastTransaction = null;
                                }

                                outstandingTransactions.remove(currentTransaction);
                            }

                            ZWaveNode node = controller.getNode(currentTransaction.getNodeId());
                            if (node != null && !node.isListening() && currentTransaction
                                    .getTransactionCancelledState() == TransactionState.WAIT_REQUEST) {
                                // Node is not listening, and we failed waiting for a REQUEST
                                // which means the device didn't respond. Treat as ASLEEP.
                                logger.debug("NODE {}: Transaction failed waiting for REQUEST, assume sleeping device.",
                                        currentTransaction.getNodeId());
                                node.setAwake(false);
                            }

                            // Handle retries
                            if (currentTransaction.decrementAttemptsRemaining() > 0) {
                                logger.debug("NODE {}: CANCEL while sending message. Requeueing - {} attempts left!",
                                        currentTransaction.getNodeId(), currentTransaction.getAttemptsRemaining());

                                // Reset the transaction
                                currentTransaction.resetTransaction();

                                // TODO: Lower the priority since it's a retry!
                                // currentTransaction.setPriority(priority);
                                addTransactionToQueue(currentTransaction);
                            } else {
                                logger.debug("NODE {}: Retry count exceeded. Discarding message: {}",
                                        currentTransaction.getNodeId(), currentTransaction.toString());

                                // We don't consider the failure of controller transactions to indicate the
                                // device is dead.
                                // Transactions that fail in the WAIT_RESPONSE are not used as this means the
                                // failure was in communicating with the controller, not the device.
                                if (currentTransaction.getSerialMessageClass().requiresNode() && currentTransaction
                                        .getTransactionCancelledState() != TransactionState.WAIT_RESPONSE) {
                                    if (node != null && node.isAwake()) {
                                        // Node is awake, it should always respond!
                                        node.setNodeState(ZWaveNodeState.DEAD);
                                    }
                                }

                                // Notify our users...
                                transactionCompleted = true;
                            }
                            break;

                        default:
                            break;
                    }

                    // If the transaction is complete, then notify the higher layer
                    // Note that if retries are still outstanding, then a transaction is not considered complete
                    // if it has been CANCELLED or ABORTED.
                    if (transactionCompleted == true) {
                        logger.debug("NODE {}: TID {}: Transaction completed", currentTransaction.getNodeId(),
                                currentTransaction.getTransactionId());

                        // Notify the async threads
                        // Note that this is really here to complete transactions that don't return a command class
                        notifyTransactionComplete(currentTransaction);

                        // Notify the controller
                        controller.handleTransactionComplete(currentTransaction, incomingMessage);
                    } else {
                        logger.debug("NODE {}: TID {}: Transaction not completed", currentTransaction.getNodeId(),
                                currentTransaction.getTransactionId());
                    }

                }
            }
            logger.debug("Exiting ZWave Receive Thread");
        }

    }

    private Date getNextTimer(ZWaveTransaction transaction) {
        long nextTimer = 0;
        switch (transaction.getTransactionState()) {
            case CANCELLED:
            case DONE:
            case UNINTIALIZED:
                break;
            case WAIT_RESPONSE:
                nextTimer = System.currentTimeMillis() + timer1;
                break;
            case WAIT_REQUEST:
                nextTimer = System.currentTimeMillis() + timer2;
                break;
            case WAIT_DATA:
                nextTimer = System.currentTimeMillis() + transaction.getDataTimeout();
                break;
            case ABORTED:
                nextTimer = System.currentTimeMillis() + timerAbort;
                break;
        }

        if (nextTimer == 0) {
            return null;
        }

        return new Date(nextTimer);
    }

    private ZWaveTransaction getMessageFromQueue(PriorityBlockingQueue<ZWaveTransaction> queue) {
        for (ZWaveTransaction tmp : queue) {
            ZWaveNode node = controller.getNode(tmp.getNodeId());
            if (node == null) {
                logger.debug("NODE {}: Node not found - has this node been removed?!? Dropping transaction {}.",
                        tmp.getNodeId(), tmp);
                queue.remove(tmp);
                continue;
            }

            // Check if the node is awake
            if (node.isAwake() == false) {
                logger.trace("NODE {}: Node not awake!", node.getNodeId());
                continue;
            }

            // Node is awake - remove this transaction and return it
            queue.remove(tmp);
            return tmp;
        }

        return null;
    }

    private void sendNextMessage() {
        synchronized (holdoffActive) {
            logger.debug("Transaction SendNextMessage {} out at start. Holdoff {}", outstandingTransactions.size(),
                    holdoffActive);
            if (holdoffActive) {
                logger.debug("Holdoff Timer active - no send...");
                return;
            }
        }

        synchronized (sendQueue) {
            // If we're currently processing the core of a transaction, or there are too many
            // outstanding transactions, then don't start another right now.
            if (lastTransaction != null) {
                logger.trace("Transaction lastTransaction outstanding...", outstandingTransactions.size());
                return;
            }

            // Highest priority is the controller priority queue
            // These should be very quick response messages that we really want to get to the controller quickly
            ZWaveTransaction transaction = priorityControllerQueue.poll();
            if (transaction == null) {
                // If we're sending a NONCE then we want to ignore the sleeping state of the device.
                // We assume that if the device just sent us a NONCE_REQUEST then it must be awake
                transaction = secureQueue.poll();
                if (transaction != null) {
                    logger.trace("Transaction from secureQueue");
                } else if (outstandingTransactions.size() == 0) {
                    transaction = getMessageFromQueue(sendQueue);
                    if (transaction != null) {
                        logger.trace("Transaction from sendQueue");
                    }
                    if (transaction == null) {
                        transaction = controllerQueue.poll();
                        logger.trace("Transaction from controllerQueue");
                    }
                }
            }
            if (transaction == null) {
                // Nothing to send
                logger.trace("Transaction SendNextMessage nothing");
                return;
            }

            SerialMessage serialMessage;
            // If this requires security, then check if we have a NONCE
            if (transaction.getRequiresSecurity()) {
                logger.trace("NODE {}: Transaction requires security", transaction.getNodeId());
                ZWaveNode node = controller.getNode(transaction.getNodeId());
                ZWaveSecurityCommandClass securityCommandClass = (ZWaveSecurityCommandClass) node
                        .getCommandClass(CommandClass.COMMAND_CLASS_SECURITY);
                if (securityCommandClass == null) {
                    logger.debug("NODE {}: COMMAND_CLASS_SECURITY not found.", transaction.getNodeId());
                    return;
                }

                if (securityCommandClass.isNonceAvailable()) {
                    // We have a NONCE, so encapsulate and send
                    logger.trace("NODE {}: NONCE available so encap and send.", transaction.getNodeId());

                    ZWaveCommandClassTransactionPayload securePayload = new ZWaveCommandClassTransactionPayload(
                            transaction.getNodeId(),
                            securityCommandClass.getSecurityMessageEncapsulation(transaction.getPayloadBuffer()),
                            TransactionPriority.RealTime, transaction.getExpectedCommandClass(),
                            transaction.getExpectedCommandClassCommand());

                    // Get the serial message for the secure message and add it to our transaction so it correlates
                    // properly
                    serialMessage = securePayload.getSerialMessage();
                    transaction.setSerialMessage(serialMessage);
                } else {
                    // Request a nonce - create a temporary transaction
                    // We keep a reference to the original transaction so that if the nonce transaction fails, then we
                    // fail the real transaction and let the application deal with retries.
                    transaction = new ZWaveSecureTransaction(transaction, securityCommandClass.getSecurityNonceGet());
                    serialMessage = transaction.getSerialMessage();
                }
            } else {
                logger.trace("getTransactionToSend 6");
                serialMessage = transaction.getSerialMessage();
            }

            // Add this message to the outstandingTransactions list
            // SerialMessage serialMessage = transaction.getSerialMessage();
            serialMessage
                    .setTransmitOptions(TRANSMIT_OPTION_ACK | TRANSMIT_OPTION_AUTO_ROUTE | TRANSMIT_OPTION_EXPLORE);
            controller.sendPacket(serialMessage);

            transaction.transactionStart();
            logger.trace("Transaction SendNextMessage started: {}", transaction);

            logger.trace("Transaction SendNextMessage started: expected cmd class: {}",
                    transaction.getExpectedCommandClass());
            logger.trace("Transaction SendNextMessage started: expected cmd: {}",
                    transaction.getExpectedCommandClassCommand());

            outstandingTransactions.add(transaction);
            logger.trace("Transaction SendNextMessage Transactions outstanding: {}", outstandingTransactions.size());
            transaction.setTimeout(getNextTimer(transaction));
            startTransactionTimer();
            lastTransaction = transaction;
            logger.trace("Transaction SendNextMessage lastTransaction: {}", lastTransaction);
        }
    }

    private void startHoldoffTimer() {
        synchronized (holdoffActive) {
            logger.debug("Holdoff Timer started...");
            holdoffActive = Boolean.TRUE;
            holdoffDelay.setTimeInMillis(System.currentTimeMillis() + HOLDOFF_DELAY);

            startTransactionTimer();
        }
    }

    private void startTransactionTimer() {
        synchronized (sendQueue) {
            // Stop any existing timer
            resetTransactionTimer();

            synchronized (holdoffActive) {
                if (holdoffActive) {
                    long delay = holdoffDelay.getTimeInMillis() - System.currentTimeMillis();
                    if (delay > 0) {
                        // Create the timer task
                        timerTask = new ZWaveTransactionTimer();

                        logger.debug("Holdoff Timer finishing in {}ms", delay);
                        timer.schedule(timerTask, delay);

                        return;
                    } else {
                        holdoffActive = Boolean.FALSE;
                    }
                }
            }

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

            // Start the timer if required
            if (nextTimer != null) {
                // Create the timer task
                timerTask = new ZWaveTransactionTimer();

                logger.trace("Start transaction timer to {} - {}ms", nextTimer,
                        (nextTimer.getTime() - System.currentTimeMillis()));
                timer.schedule(timerTask, nextTimer);
            }
        }
    }

    private synchronized void resetTransactionTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            logger.trace("STOP transaction timer");

            timerTask.cancel();
            timerTask = null;
        }
    }

    private class ZWaveTransactionTimer extends TimerTask {
        private final Logger logger = LoggerFactory.getLogger(ZWaveTransactionTimer.class);

        @Override
        public void run() {
            // Handle the holdoff case.
            // This is set after a RESponse error to delay the next message
            synchronized (holdoffActive) {
                if (holdoffActive) {
                    logger.debug("Holdoff Timer triggered...");
                    holdoffActive = Boolean.FALSE;
                    sendNextMessage();
                    startTransactionTimer();
                    return;
                }
            }

            synchronized (sendQueue) {
                logger.trace("Transaction Timeout.......... {} outstanding transactions",
                        outstandingTransactions.size());
                Date now = new Date();
                // List<ZWaveTransaction> retries = new ArrayList<ZWaveTransaction>();

                // Loop through all outstanding transactions to see if any have timed out
                Iterator<ZWaveTransaction> iterator = outstandingTransactions.iterator();
                while (iterator.hasNext()) {
                    ZWaveTransaction transaction = iterator.next();
                    Date timer = transaction.getTimeout();
                    if (timer != null && timer.after(now) == false) {
                        // Timeout
                        logger.debug("NODE {}: TID {}: Timeout at state {}. {} retries remaining.",
                                transaction.getNodeId(), transaction.getTransactionId(),
                                transaction.getTransactionState(), transaction.getAttemptsRemaining());

                        // If this is a SendData message, and we're not waiting for DATA
                        // Then we need to cancel this request.
                        // TODO: Maybe this should be generalised to allow for other commands?
                        if (transaction.getSerialMessageClass() == SerialMessageClass.SendData
                                && (transaction.getTransactionState() == TransactionState.WAIT_REQUEST
                                        || transaction.getTransactionState() == TransactionState.WAIT_RESPONSE)) {
                            // SendData requests need to be aborted - so we don't cancel the transaction.
                            // Once aborted we will get the completion of the transaction which will cancel the
                            // transaction.
                            logger.debug("Aborting Transaction!");
                            transaction.setTransactionAborted();
                            transaction.setTimeout(getNextTimer(transaction));

                            controller.sendPacket(new ZWaveTransactionMessageBuilder(SerialMessageClass.SendDataAbort)
                                    .build().getSerialMessage());
                        } else {
                            // Remove this transaction from the outstanding transactions list
                            iterator.remove();

                            if (lastTransaction == transaction) {
                                // If this is the current transaction, then reset it.
                                lastTransaction = null;
                                logger.debug("TID {}: Transaction is current transaction, so clearing!!!!!",
                                        transaction.getTransactionId());
                            }

                            // Resend if there are still attempts remaining
                            // if (transaction.decrementAttemptsRemaining() <= 0) {
                            transaction.setTransactionCanceled();
                            controller.handleTransactionComplete(transaction, null);
                            notifyTransactionComplete(transaction);
                            // } else {
                            // Resend - add to a separate list so as not to impact iterator
                            // transaction.resetTransaction();
                            // retries.add(transaction);
                            // }
                        }
                    }
                }

                // Add retries to the queue
                // for (ZWaveTransaction transaction : retries) {
                // logger.debug("Resending... Transaction " + transaction.getTransactionId());
                // addTransactionToQueue(transaction);
                // }

                // If there's no outstanding transaction, try and send one
                sendNextMessage();
                startTransactionTimer();
            }
        }
    }

    public Future<ZWaveTransactionResponse> sendTransactionAsync(final ZWaveMessagePayloadTransaction transaction) {
        class TransactionWaiter implements Callable<ZWaveTransactionResponse>, TransactionListener {
            ZWaveTransactionResponse response = null;
            long transactionId;

            @Override
            public ZWaveTransactionResponse call() throws Exception {
                // Register a listener
                AddTransactionListener(this);

                // Send the transaction
                transactionId = queueTransactionForSend(transaction);
                if (transactionId == 0) {
                    logger.debug(">>>>>>>>> transaction rejected !!");

                    // The transaction failed!
                    // Remove the listener
                    RemoveTransactionListener(this);

                    return response;
                }

                // Wait for the transaction to complete
                synchronized (this) {
                    while (response == null) {
                        wait();
                    }
                }

                // Remove the listener
                RemoveTransactionListener(this);

                logger.debug("********* Transaction Response Complete -- {} --", transactionId);

                return response;
            }

            @Override
            public void transactionEvent(ZWaveTransaction transactionEvent) {
                // Check if this transaction is ours
                if (transactionEvent.getTransactionId() != transactionId) {
                    return;
                }
                logger.debug("TID {}: Transaction event listener: DONE: {} -> ", transactionId,
                        transactionEvent.getTransactionState(), transactionEvent.getTransactionCancelledState());

                // Return the response
                ZWaveTransactionResponse.State state = State.COMPLETE;
                if (transactionEvent.getTransactionState() != TransactionState.DONE) {
                    switch (transactionEvent.getTransactionCancelledState()) {
                        case CANCELLED:
                            state = State.CANCELLED;
                            break;
                        case DONE:
                            break;
                        case UNINTIALIZED:
                            // This might happen if a transaction is reset due to a CAN
                            state = State.CANCELLED;
                            logger.debug("Completing UNINTIALIZED transaction {}!!! How?!?", transactionId);
                            break;
                        case WAIT_DATA:
                            state = State.TIMEOUT_WAITING_FOR_DATA;
                            break;
                        case WAIT_RESPONSE:
                            state = State.TIMEOUT_WAITING_FOR_CONTROLLER;
                            break;
                        case WAIT_REQUEST:
                            state = State.TIMEOUT_WAITING_FOR_RESPONSE;
                            break;
                        default:
                            break;
                    }
                }
                response = new ZWaveTransactionResponse(state);

                logger.debug("NODE {}: -- To notify -- {}", transaction.getDestinationNode(), state);
                synchronized (this) {
                    notify();
                }
            }
        }

        Callable<ZWaveTransactionResponse> worker = new TransactionWaiter();
        return executor.submit(worker);
    }

    /**
     * Sends a transaction and waits for the response.
     *
     * @param transaction {@link ZWaveMessagePayloadTransaction} to send
     * @return The {@link ZWaveTransactionResponse} or null if there was an error
     */
    public @Nullable ZWaveTransactionResponse sendTransaction(ZWaveMessagePayloadTransaction transaction) {
        logger.debug("NODE {}: sendTransaction {}", transaction.getDestinationNode(), transaction);

        Future<ZWaveTransactionResponse> futureResponse = sendTransactionAsync(transaction);
        try {
            ZWaveTransactionResponse response = futureResponse.get();
            return response;
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("NODE {}: sendTransaction interrupted", e);
        }

        return null;
    }

    interface TransactionListener {
        void transactionEvent(ZWaveTransaction transaction);
    }
}
