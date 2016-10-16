/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Wake Up Command Class. Enables a node to notify another device that it woke up and is ready to receive commands.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("COMMAND_CLASS_WAKE_UP")
public class ZWaveWakeUpCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveEventListener {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveWakeUpCommandClass.class);
    private static final int MAX_SUPPORTED_VERSION = 2;

    public static final int WAKE_UP_INTERVAL_SET = 0x04;
    public static final int WAKE_UP_INTERVAL_GET = 0x05;
    public static final int WAKE_UP_INTERVAL_REPORT = 0x06;
    public static final int WAKE_UP_NOTIFICATION = 0x07;
    public static final int WAKE_UP_NO_MORE_INFORMATION = 0x08;
    public static final int WAKE_UP_INTERVAL_CAPABILITIES_GET = 0x09;
    public static final int WAKE_UP_INTERVAL_CAPABILITIES_REPORT = 0x0A;

    private static final int MAX_BUFFFER_SIZE = 128;

    @XStreamOmitField
    private ArrayBlockingQueue<ZWaveCommandClassTransactionPayload> wakeUpQueue;

    // From interval report
    @XStreamOmitField
    private boolean initReportDone = false;
    private int targetNodeId = 0;
    private int interval = 0;

    // From capabilities report
    @XStreamOmitField
    private boolean initCapabilitiesDone = false;
    private int minInterval = 0;
    private int maxInterval = 0;
    private int defaultInterval = 0;
    private int intervalStep = 0;

    private Date lastWakeup = null;

    @XStreamOmitField
    private volatile boolean isAwake = false;

    @XStreamOmitField
    private Timer timer = null;
    @XStreamOmitField
    private TimerTask timerTask = null;

    @XStreamOmitField
    private boolean initialiseDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveWakeUpCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveWakeUpCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;

        wakeUpQueue = new ArrayBlockingQueue<ZWaveCommandClassTransactionPayload>(MAX_BUFFFER_SIZE, true);

        timer = new Timer();
    }

    /**
     * Resolves uninitialized fields after XML Deserialization.
     *
     * @return The current {@link ZWaveWakeUpCommandClass} instance.
     */
    private Object readResolve() {
        wakeUpQueue = new ArrayBlockingQueue<ZWaveCommandClassTransactionPayload>(MAX_BUFFFER_SIZE, true);
        timer = new Timer();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_WAKE_UP;
    }

    @ZWaveResponseHandler(id = WAKE_UP_INTERVAL_REPORT, name = "WAKE_UP_INTERVAL_REPORT")
    public void handleWakeupIntervalReport(ZWaveCommandClassPayload payload, int endpoint) {
        initReportDone = true;

        // According to open-zwave: it seems that some devices send incorrect interval report messages.
        if (payload.getPayloadLength() < 5) {
            logger.error("NODE {}: Unusual response: WAKE_UP_INTERVAL_REPORT with length = {}. Ignored.",
                    getNode().getNodeId(), payload.getPayloadLength());
            return;
        }

        targetNodeId = payload.getPayloadByte(5);
        int receivedInterval = ((payload.getPayloadByte(2)) << 16) | ((payload.getPayloadByte(3)) << 8)
                | (payload.getPayloadByte(4));
        logger.debug("NODE {}: Wake up interval report, value = {} seconds, targetNodeId = {}",
                this.getNode().getNodeId(), receivedInterval, targetNodeId);

        interval = receivedInterval;

        ZWaveWakeUpEvent event = new ZWaveWakeUpEvent(getNode().getNodeId(), WAKE_UP_INTERVAL_REPORT);
        getController().notifyEventListeners(event);
    }

    @ZWaveResponseHandler(id = WAKE_UP_INTERVAL_CAPABILITIES_REPORT, name = "WAKE_UP_INTERVAL_CAPABILITIES_REPORT")
    public void handleWakeupIntervalCapabilitiesReport(ZWaveCommandClassPayload payload, int endpoint) {
        initCapabilitiesDone = true;

        this.minInterval = ((payload.getPayloadByte(2)) << 16) | ((payload.getPayloadByte(3)) << 8)
                | (payload.getPayloadByte(4));
        this.maxInterval = ((payload.getPayloadByte(5)) << 16) | ((payload.getPayloadByte(6)) << 8)
                | (payload.getPayloadByte(7));
        this.defaultInterval = ((payload.getPayloadByte(8)) << 16) | ((payload.getPayloadByte(9)) << 8)
                | (payload.getPayloadByte(20));
        this.intervalStep = ((payload.getPayloadByte(11)) << 16) | ((payload.getPayloadByte(12)) << 8)
                | (payload.getPayloadByte(13));

        logger.debug("NODE {}: Wake up interval capabilities report", this.getNode().getNodeId());
        logger.debug("NODE {}: Minimum interval = {}", this.getNode().getNodeId(), this.minInterval);
        logger.debug("NODE {}: Maximum interval = {}", this.getNode().getNodeId(), this.maxInterval);
        logger.debug("NODE {}: Default interval = {}", this.getNode().getNodeId(), this.defaultInterval);
        logger.debug("NODE {}: Interval step    = {}", this.getNode().getNodeId(), this.intervalStep);

        ZWaveWakeUpEvent capabilitiesEvent = new ZWaveWakeUpEvent(getNode().getNodeId(),
                WAKE_UP_INTERVAL_CAPABILITIES_REPORT);
        getController().notifyEventListeners(capabilitiesEvent);
    }

    @ZWaveResponseHandler(id = WAKE_UP_NOTIFICATION, name = "WAKE_UP_NOTIFICATION")
    public void handleWakeupNotification(ZWaveCommandClassPayload payload, int endpoint) {
        // serialMessage.setTransactionCanceled();

        // Set the awake flag. This will also empty the queue
        setAwake(true);
    }

    /**
     * Gets a SerialMessage with the WAKE_UP_NO_MORE_INFORMATION command.
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getNoMoreInformationMessage() {
        logger.debug("NODE {}: Creating new message for application command WAKE_UP_NO_MORE_INFORMATION",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                WAKE_UP_NO_MORE_INFORMATION).withPriority(TransactionPriority.Immediate).build();
    }

    /**
     * If the device is awake, it returns true to indicate that this message can be sent immediately. If the device is
     * not awake, it puts the message in the wake-up queue to send the message on next wake-up.
     * The message is only added if it's not the WAKE_UP_NO_MORE_INFORMATION message since we don't want to send this at
     * the next wakeup.
     * This combines the previous 'putInWakeUpQueue' with 'isAlive'.
     *
     * @param serialMessage the message to put in the wake-up queue.
     * @return true if the message can be sent immediately
     */
    public boolean processOutgoingWakeupMessage(ZWaveCommandClassTransactionPayload payload) {
        // The message is Ok, if we're awake, send it now...
        if (isAwake) {
            // We're sending a frame, so we need to stop the timer if it's running
            resetSleepTimer();
            return true;
        }

        // Don't add any RealTime messages to the wakeup queue. This stops us delaying
        // messages like time set which would mean setting the time completely wrong!
        if (payload.getPriority() == TransactionPriority.RealTime) {
            logger.debug("NODE {}: Dropping RealTime message", getNode().getNodeId());
            return false;
        }

        // Make sure we never add the WAKE_UP_NO_MORE_INFORMATION message to the queue
        if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_WAKE_UP.getKey()
                && payload.getCommandClassCommand() == WAKE_UP_NO_MORE_INFORMATION) {
            logger.debug("NODE {}: WAKE_UP_NO_MORE_INFORMATION not queuing.", getNode().getNodeId());
            return false;
        }

        if (wakeUpQueue.contains(payload)) {
            logger.debug("NODE {}: Message already on the wake-up queue. Removing original.", getNode().getNodeId());
            wakeUpQueue.remove(payload);
        }

        // Bump the priority so that messages to battery devices are sent immediately when the device wakes
        payload.setPriority(TransactionPriority.Immediate);

        logger.debug("NODE {}: Putting message in wakeup queue.", getNode().getNodeId());
        wakeUpQueue.add(payload);

        // This message has been queued - don't send it now...
        return false;
    }

    /**
     * Gets a SerialMessage with the WAKE UP INTERVAL GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getIntervalMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command WAKE_UP_INTERVAL_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                WAKE_UP_INTERVAL_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(WAKE_UP_INTERVAL_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the WAKE UP INTERVAL CAPABILITIES GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getIntervalCapabilitiesMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command WAKE_UP_INTERVAL_CAPABILITIES_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                WAKE_UP_INTERVAL_CAPABILITIES_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(WAKE_UP_INTERVAL_CAPABILITIES_REPORT).build();
    }

    /**
     * Returns the interval at which the device wakes up every time.
     *
     * @return the interval in seconds.
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Returns the minimum wake up interval that can be set to the device.
     *
     * @return the minInterval in seconds.
     */
    public int getMinInterval() {
        return minInterval;
    }

    /**
     * Returns the maximum wake up interval that can be set to the device.
     *
     * @return the maxInterval in seconds.
     */
    public int getMaxInterval() {
        return maxInterval;
    }

    /**
     * Returns the factory default wake up interval for the device.
     *
     * @return the defaultInterval in seconds.
     */
    public int getDefaultInterval() {
        return defaultInterval;
    }

    /**
     * Returns the minimum step that must be taken into account when setting the interval for the device.
     *
     * @return the intervalStep in seconds.
     */
    public int getIntervalStep() {
        return intervalStep;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>(2);
        if (refresh == true) {
            initReportDone = false;
            initCapabilitiesDone = false;
        }

        if (initReportDone == false) {
            // get wake up interval.
            result.add(getIntervalMessage());
        }

        if (initCapabilitiesDone == false && getVersion() > 1) {
            // get default values for wake up interval.
            result.add(getIntervalCapabilitiesMessage());
        }
        return result;
    }

    /**
     * Event handler for incoming Z-Wave events. We monitor Z-Wave events for completed transactions. Once a transaction
     * is completed for the WAKE_UP_NO_MORE_INFORMATION event, we set the node state to asleep. {@inheritDoc}
     */
    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        if (!(event instanceof ZWaveTransactionCompletedEvent)) {
            return;
        }

        // TODO: Find a better way to handle this. We should be able to use the transactionId.
        org.openhab.binding.zwave.internal.protocol.ZWaveTransaction transaction = ((ZWaveTransactionCompletedEvent) event)
                .getCompletedTransaction();
        SerialMessage serialMessage = transaction.getSerialMessage();

        if (serialMessage.getMessageClass() != SerialMessageClass.SendData
                && serialMessage.getMessageType() != SerialMessageType.Request) {
            return;
        }

        byte[] payload = serialMessage.getMessagePayload();

        // Check if it's addressed to this node
        if (payload.length == 0 || (payload[0] & 0xFF) != getNode().getNodeId()) {
            return;
        }

        // We now know that this is a message to this node.
        // If it's not the WAKE_UP_NO_MORE_INFORMATION, then we need to set the wakeup timer
        if (payload.length >= 4 && (payload[2] & 0xFF) == getCommandClass().getKey()
                && (payload[3] & 0xFF) == WAKE_UP_NO_MORE_INFORMATION) {
            // This is confirmation of our 'go to sleep' message
            logger.debug("NODE {}: Went to sleep", getNode().getNodeId());
            setAwake(false);
            return;
        }

        // Send the next message in the wake-up queue
        if (!wakeUpQueue.isEmpty()) {
            // Get the next message from the queue.
            // Bump it's priority to highest to try and send it while the node is awake
            ZWaveCommandClassTransactionPayload nextTransaction = wakeUpQueue.poll();
            getController().enqueue(nextTransaction);
        } else if (isAwake() == true) {
            // No more messages in the queue.
            // Start a timer to send the "Go To Sleep" message
            // This gives other tasks some time to do something if they want
            setSleepTimer();
        }
    }

    /**
     * Returns whether the node is awake.
     *
     * @return the isAwake
     */
    public boolean isAwake() {
        return isAwake;
    }

    /**
     * Sets whether the node is awake.
     * If the node is awake we send the first message in the wake-up queue.
     * The remaining messages are triggered within the notification handler
     *
     * @param isAwake the isAwake to set
     */
    public void setAwake(boolean isAwake) {
        // Only do anything if the state has changed.
        // This prevents sending extra notifications which can cause unwanted updates - especially during
        // initialisation.
        if (this.isAwake == isAwake) {
            return;
        }

        this.isAwake = isAwake;

        if (isAwake) {
            logger.debug("NODE {}: Is awake with {} messages in the wake-up queue.", getNode().getNodeId(),
                    wakeUpQueue.size());

            lastWakeup = Calendar.getInstance().getTime();

            ZWaveWakeUpEvent event = new ZWaveWakeUpEvent(getNode().getNodeId(), WAKE_UP_NOTIFICATION);
            getController().notifyEventListeners(event);

            // Handle the wake-up queue for this node.
            // We send the first message, and when that's ACKed, we sent the next
            if (!wakeUpQueue.isEmpty()) {
                // Get the next message from the queue.
                // Bump it's priority to highest to try and send it while the node is awake
                ZWaveCommandClassTransactionPayload nextTransaction = wakeUpQueue.poll();
                nextTransaction.setPriority(TransactionPriority.Immediate);
                getController().enqueue(nextTransaction);
            } else {
                // No messages in the queue.
                // Start a timer to send the "Go To Sleep" message
                // This gives other tasks some time to do something if they want
                setSleepTimer();
            }
        } else {
            logger.debug("NODE {}: Is sleeping", getNode().getNodeId());
        }
    }

    /**
     * Sends a command to the device to set the wakeup interval.
     * The wakeup node is set to the controller.
     *
     * @param interval the wakeup interval in seconds
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setInterval(int interval) {
        logger.debug("NODE {}: Creating new message for application command WAKE_UP_INTERVAL_SET to {}",
                getNode().getNodeId(), interval);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                WAKE_UP_INTERVAL_SET)
                        .withPayload(((interval >> 16) & 0xff), ((interval >> 8) & 0xff), (interval & 0xff),
                                getController().getOwnNodeId())
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets the size of the wake up queue
     *
     * @return number of messages currently queued
     */
    public int getWakeupQueueLength() {
        return wakeUpQueue.size();
    }

    /**
     * Gets the target node for the Wakeup command class
     *
     * @return wakeup target node id
     */
    public int getTargetNodeId() {
        return targetNodeId;
    }

    /**
     * Gets the time the node last woke up
     *
     * @return Date of the last wakeup
     */
    public Date getLastWakeup() {
        return lastWakeup;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * The following timer implements a re-triggerable timer. The timer is triggered when there are no more messages to
     * be sent in the wake-up queue. When the timer times out it will send the 'Go To Sleep' message to the node.
     * The timer just provides some time for anything further to be sent as a result of any processing.@author chris
     *
     * @author Chris Jackson
     */
    private class WakeupTimerTask extends TimerTask {
        ZWaveWakeUpCommandClass wakeup;

        WakeupTimerTask(ZWaveWakeUpCommandClass wakeup) {
            this.wakeup = wakeup;
        }

        @Override
        public void run() {
            if (!wakeup.isAwake()) {
                logger.debug("NODE {}: Already asleep", wakeup.getNode().getNodeId());
                return;
            }
            // Tell the device to back to sleep.
            logger.debug("NODE {}: No more messages, go back to sleep", wakeup.getNode().getNodeId());
            wakeup.getController().enqueue(wakeup.getNoMoreInformationMessage());
        }
    }

    public synchronized void setSleepTimer() {
        // Stop any existing timer
        resetSleepTimer();

        // Create the timer task
        timerTask = new WakeupTimerTask(this);

        // Start the timer
        timer.schedule(timerTask, 1000);
    }

    public synchronized void resetSleepTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = null;
    }

    /**
     * ZWave wake-up event.
     * Notifies users that a device has woken up or changed its wakeup parameters
     *
     * @author Chris Jackson
     */
    public class ZWaveWakeUpEvent extends ZWaveEvent {
        private final int event;

        /**
         * Constructor. Creates a new instance of the ZWaveWakeUpEvent class.
         *
         * @param nodeId the nodeId of the event.
         */
        public ZWaveWakeUpEvent(int nodeId, int event) {
            super(nodeId, 1);
            this.event = event;
        }

        public int getEvent() {
            return this.event;
        }
    }
}
