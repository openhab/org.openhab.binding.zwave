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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
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
public class ZWaveWakeUpCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

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

    // From interval report
    @XStreamOmitField
    private boolean initReportDone = false;
    private int targetNodeId = 0;
    private int interval = 0;

    // From capabilities report
    @XStreamOmitField
    private boolean initCapabilitiesDone = false;
    private int minInterval = 0;
    private int maxInterval = Integer.MAX_VALUE;
    private int defaultInterval = 0;
    private int intervalStep = 0;

    private Date lastWakeup = null;

    @XStreamOmitField
    private volatile boolean isAwake = false;

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
    }

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

        // TODO Change this to send a value notification
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
                | (payload.getPayloadByte(10));
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
        // Set the awake flag. This will also empty the queue
        getNode().setAwake(true);
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
     * Sends a command to the device to set the wakeup interval and target node.
     *
     * @param nodeId the wakeup target node ID
     * @param interval the wakeup interval in seconds
     * @return the {@link ZWaveCommandClassTransactionPayload}
     */
    public ZWaveCommandClassTransactionPayload setInterval(int nodeId, int interval) {
        logger.debug("NODE {}: Creating new message for command WAKE_UP_INTERVAL_SET to {}s, node {}",
                getNode().getNodeId(), interval, nodeId);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                WAKE_UP_INTERVAL_SET)
                        .withPayload(((interval >> 16) & 0xff), ((interval >> 8) & 0xff), (interval & 0xff), nodeId)
                        .withPriority(TransactionPriority.Config).build();
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
