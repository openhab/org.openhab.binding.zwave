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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the DoorLock command class.
 *
 * @author Dave Badia
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_DOOR_LOCK")
public class ZWaveDoorLockCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {
    public enum Type {
        DOOR_CONDITION,
        DOOR_LOCK_STATE,
        DOOR_LOCK_TIMEOUT
    }

    private static final Logger logger = LoggerFactory.getLogger(ZWaveDoorLockCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 1;

    static final int DOOR_LOCK_SET = 1;
    /**
     * Request the state of the door lock, ie {@link #DOOR_LOCK_REPORT}
     */
    private static final int DOOR_LOCK_GET = 2;
    /**
     * Details about the state of the door lock (secured, unsecured)
     */
    private static final int DOOR_LOCK_REPORT = 3;
    private static final int DOOR_LOCK_CONFIG_SET = 4;
    /**
     * Request the config of the door lock, ie {@link #DOOR_LOCK_CONFIG_REPORT}
     */
    private static final int DOOR_LOCK_CONFIG_GET = 5;
    /**
     * Details about the config of the door lock (timed autolock, etc)
     */
    private static final int DOOR_LOCK_CONFIG_REPORT = 6;

    @XStreamOmitField
    private boolean initialisationDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    boolean lockTimeoutSet = false;
    int lockTimeout = 0;
    int lockTimeoutMinutes = 0;
    int lockTimeoutSeconds = 0;
    int insideHandleMode = 0;
    int outsideHandleMode = 0;

    /**
     * Creates a new instance of the ZWaveDoorLockCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveDoorLockCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_DOOR_LOCK;
    }

    @ZWaveResponseHandler(id = DOOR_LOCK_CONFIG_REPORT, name = "DOOR_LOCK_CONFIG_REPORT")
    public void handleDoorLockConfigReport(ZWaveCommandClassPayload payload, int endpoint) {
        initialisationDone = true;
        lockTimeoutSet = payload.getPayloadByte(2) == 2 ? true : false;
        insideHandleMode = payload.getPayloadByte(3) & 0x0f;
        outsideHandleMode = (payload.getPayloadByte(3) & 0xf0) >> 4;
        lockTimeoutMinutes = payload.getPayloadByte(4);
        lockTimeoutSeconds = payload.getPayloadByte(5);

        lockTimeout = 0;
        if (lockTimeoutSet == true) {
            if (lockTimeoutSeconds != 0xfe) {
                lockTimeout = lockTimeoutSeconds;
            }
            if (lockTimeoutMinutes != 0xfe) {
                lockTimeout += lockTimeoutMinutes * 60;
            }
        }
        logger.info("NODE {}: Door-Lock config report - timeoutEnabled={} timeoutMinutes={}, " + "timeoutSeconds={}",
                getNode().getNodeId(), lockTimeoutSet, lockTimeoutMinutes, lockTimeoutSeconds);
        ZWaveCommandClassValueEvent configEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                CommandClass.COMMAND_CLASS_DOOR_LOCK, lockTimeout, Type.DOOR_LOCK_TIMEOUT);
        getController().notifyEventListeners(configEvent);
    }

    @ZWaveResponseHandler(id = DOOR_LOCK_REPORT, name = "DOOR_LOCK_REPORT")
    public void handleDoorLockReport(ZWaveCommandClassPayload payload, int endpoint) {
        dynamicDone = true;

        int lockMode = payload.getPayloadByte(2);
        int handlesMode = payload.getPayloadByte(3);
        int doorCondition = payload.getPayloadByte(4);
        int statusTimeoutMinutes = payload.getPayloadByte(5);
        int statusTimeoutSeconds = payload.getPayloadByte(6);

        DoorLockStateType doorLockState = DoorLockStateType.getDoorLockStateType(lockMode);
        logger.debug(
                "NODE {}: Door-Lock state report - lockState={}, handlesMode={}, doorCondition={}, timeoutMinutes={}, timeoutSeconds={}",
                getNode().getNodeId(), doorLockState.label, handlesMode, doorCondition, statusTimeoutMinutes,
                statusTimeoutSeconds);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                CommandClass.COMMAND_CLASS_DOOR_LOCK, lockMode, Type.DOOR_LOCK_STATE);
        getController().notifyEventListeners(zEvent);
        zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint, CommandClass.COMMAND_CLASS_DOOR_LOCK,
                doorCondition, Type.DOOR_CONDITION);
        getController().notifyEventListeners(zEvent);
    }

    /**
     * Gets a SerialMessage with the DOORLOCK_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command DOORLOCK_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), DOOR_LOCK_GET)
                .withPriority(TransactionPriority.Get).withExpectedResponseCommand(DOOR_LOCK_REPORT).build();
    }

    public ZWaveCommandClassTransactionPayload setValueMessage(int value) {
        logger.debug("NODE {}: Creating new message for application command DOORLOCK_SET, value {}",
                getNode().getNodeId(), value);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), DOOR_LOCK_SET)
                .withPayload(value).withPriority(TransactionPriority.Get).build();
    }

    public ZWaveCommandClassTransactionPayload getConfigMessage() {
        logger.debug("NODE {}: Creating new message for application command DOORLOCK_CONFIG_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                DOOR_LOCK_CONFIG_GET).withExpectedResponseCommand(DOOR_LOCK_CONFIG_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    public ZWaveCommandClassTransactionPayload setConfigMessage(boolean timeoutEnabled, int timeoutValue) {
        lockTimeout = timeoutValue;
        lockTimeoutSet = timeoutEnabled;
        if (lockTimeoutSet == true) {
            lockTimeoutMinutes = timeoutValue / 60;
            lockTimeoutSeconds = timeoutValue % 60;
        } else {
            lockTimeout = 0;
            lockTimeoutMinutes = 0xfe;
            lockTimeoutSeconds = 0xfe;
        }

        logger.debug("NODE {}: Creating new message for application command DOORLOCK_GET", getNode().getNodeId());

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        if (lockTimeoutSet == true) {
            outputData.write((byte) 2);
        } else {
            outputData.write((byte) 1);
        }
        outputData.write((byte) (((outsideHandleMode << 4) & 0xf0) + (insideHandleMode & 0x0f)));
        outputData.write((byte) lockTimeoutMinutes);
        outputData.write((byte) lockTimeoutSeconds);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                DOOR_LOCK_CONFIG_SET).withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Config)
                        .build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        if (refresh) {
            initialisationDone = false;
        }

        if (initialisationDone) {
            return null;
        }

        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        result.add(getConfigMessage());
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        if (refresh) {
            dynamicDone = false;
        }

        if (dynamicDone) {
            return null;
        }

        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        result.add(getConfigMessage());
        result.add(getValueMessage());
        return result;
    }

    /**
     * Z-Wave DoorLockState enumeration. The door lock state type indicates
     * the state of the door that is reported.
     */
    @XStreamAlias("doorLockState")
    enum DoorLockStateType {
        UNSECURED(0x00, "Unsecured"),
        UNSECURED_TIMEOUT(0x01, "Unsecure with Timeout"),
        INSIDE_UNSECURED(0x10, "Inside Handle Unsecured"),
        INSIDE_UNSECURED_TIMEOUT(0x11, "Inside Handle Unsecured with Timeout"),
        OUTSIDE_UNSECURED(0x20, "Outside Handle Unsecured"),
        OUTSIDE_UNSECURED_TIMEOUT(0x21, "Outside Handle Unsecured with Timeout"),
        SECURED(0xFF, "Secured"),
        UNKNOWN(0xFE, "Unknown"), // This isn't per the spec, it's just our definition
        ;
        /**
         * A mapping between the integer code and its corresponding door
         * lock state type to facilitate lookup by code.
         */
        private static Map<Integer, DoorLockStateType> codeToDoorLockStateTypeMapping;

        private int key;
        private String label;

        private static void initMapping() {
            codeToDoorLockStateTypeMapping = new ConcurrentHashMap<Integer, DoorLockStateType>();
            for (DoorLockStateType d : values()) {
                codeToDoorLockStateTypeMapping.put(d.key, d);
            }
        }

        /**
         * Lookup function based on the fan mode type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the fan mode type.
         */
        public static DoorLockStateType getDoorLockStateType(int i) {
            if (codeToDoorLockStateTypeMapping == null) {
                initMapping();
            }
            return codeToDoorLockStateTypeMapping.get(i);
        }

        DoorLockStateType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }
}
