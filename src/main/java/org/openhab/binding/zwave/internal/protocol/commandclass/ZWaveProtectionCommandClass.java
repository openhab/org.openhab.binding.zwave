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
import java.util.List;

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
 * Handles the protection command class.
 *
 * @author Chris Jackson
 * @author Jorg de Jong
 */
@XStreamAlias("COMMAND_CLASS_PROTECTION")
public class ZWaveProtectionCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveProtectionCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 2;

    public static final int PROTECTION_SET = 1;
    public static final int PROTECTION_GET = 2;
    public static final int PROTECTION_REPORT = 3;

    // Version 2
    public static final int PROTECTION_SUPPORTED_GET = 0x04;
    public static final int PROTECTION_SUPPORTED_REPORT = 0x05;
    public static final int PROTECTION_EXCLUSIVECONTROL_SET = 0x06;
    public static final int PROTECTION_EXCLUSIVECONTROL_GET = 0x07;
    public static final int PROTECTION_EXCLUSIVECONTROL_REPORT = 0x08;
    public static final int PROTECTION_TIMEOUT_SET = 0x09;
    public static final int PROTECTION_TIMEOUT_GET = 0x0a;
    public static final int PROTECTION_TIMEOUT_REPORT = 0x0b;

    // PROTECTION_SUPPORTED_REPORT
    private static final int TIMEOUT_BITMASK = 0x01;
    private static final int EXCLUSIVE_CONTROL_BITMASK = 0x02;

    @XStreamOmitField
    private boolean supportedInitialised = false;

    @XStreamOmitField
    private boolean dynamicDone = false;

    @XStreamOmitField
    private LocalProtectionType currentLocalMode;

    @XStreamOmitField
    private RfProtectionType currentRfMode;

    private List<LocalProtectionType> localModes = new ArrayList<>();
    private List<RfProtectionType> rfModes = new ArrayList<>();

    /**
     * Creates a new instance of the ZWaveProtectionCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveProtectionCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_PROTECTION;
    }

    @ZWaveResponseHandler(id = PROTECTION_REPORT, name = "PROTECTION_REPORT")
    public void handleProtectionReport(ZWaveCommandClassPayload payload, int endpoint) {
        int localMode = payload.getPayloadByte(2) & 0x0f;

        if (localMode < LocalProtectionType.values().length) {
            currentLocalMode = LocalProtectionType.values()[localMode];
            ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                    getCommandClass(), currentLocalMode, Type.PROTECTION_LOCAL);
            getController().notifyEventListeners(zEvent);
        }
        if (getVersion() > 1) {
            int rfMode = payload.getPayloadByte(3) & 0x0f;
            if (rfMode < RfProtectionType.values().length) {
                currentRfMode = RfProtectionType.values()[rfMode];
                ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                        getCommandClass(), currentRfMode, Type.PROTECTION_RF);
                getController().notifyEventListeners(zEvent);
            }
            logger.debug("NODE {}: Received protection report local:{} rf:{}", getNode().getNodeId(),
                    LocalProtectionType.values()[localMode], RfProtectionType.values()[rfMode]);
        } else {
            logger.debug("NODE {}: Received protection report local:{}", getNode().getNodeId(),
                    LocalProtectionType.values()[localMode]);
        }

        dynamicDone = true;
    }

    @ZWaveResponseHandler(id = PROTECTION_SUPPORTED_REPORT, name = "PROTECTION_SUPPORTED_REPORT")
    public void handleProtectionSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        boolean exclusive = ((payload.getPayloadByte(2) & EXCLUSIVE_CONTROL_BITMASK) != 0);
        boolean timeout = ((payload.getPayloadByte(2) & TIMEOUT_BITMASK) != 0);

        int localStateMask = (payload.getPayloadByte(3) | payload.getPayloadByte(4) << 8);
        int rfStateMask = (payload.getPayloadByte(5) | payload.getPayloadByte(6) << 8);

        LocalProtectionType localTypes[] = LocalProtectionType.values();
        for (int i = 0; i < localTypes.length; i++) {
            if ((localStateMask >> i & 0x01) > 0) {
                localModes.add(localTypes[i]);
            }
        }
        RfProtectionType rfTypes[] = RfProtectionType.values();
        for (int i = 0; i < rfTypes.length; i++) {
            if ((rfStateMask >> i & 0x01) > 0) {
                rfModes.add(rfTypes[i]);
            }
        }

        logger.debug(
                "NODE {}: Received protection supported report Exclusive({}), Timeout({}},  Local states={}, RF states={}",
                getNode().getNodeId(), exclusive ? "supported" : "Not supported",
                timeout ? "supported" : "Not supported", localModes, rfModes);
        supportedInitialised = true;
    }

    /**
     * Gets a SerialMessage with the PROTECTION_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        if (getVersion() == 1) {
            logger.debug("NODE {}: PROTECTION_SUPPORTED_GET not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command PROTECTION_SUPPORTED_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                PROTECTION_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(PROTECTION_SUPPORTED_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the PROTECTION_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for command PROTECTION_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), PROTECTION_GET)
                .withPriority(TransactionPriority.Get).withExpectedResponseCommand(PROTECTION_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the PROTECTION_SET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload setValueMessage(LocalProtectionType localMode, RfProtectionType rfMode) {
        logger.debug("NODE {}: Creating new message for command PROTECTION_SET", getNode().getNodeId());

        LocalProtectionType newLocalMode = localMode != null ? localMode : currentLocalMode;
        RfProtectionType newRfMode = rfMode != null ? rfMode : currentRfMode;

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        if (getVersion() < 2) {
            outputData.write(newLocalMode.ordinal());
        } else {
            outputData.write(newLocalMode.ordinal());
            outputData.write(newRfMode.ordinal());
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), PROTECTION_SET)
                .withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Set).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        List<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (getVersion() < 2) {
            return result;
        }

        if (refresh == true || supportedInitialised == false) {
            result.add(getSupportedMessage());
        }
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        List<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true || dynamicDone == false) {
            result.add(getValueMessage());
        }

        return result;
    }

    /**
     * Reported type of protection.
     */
    public enum Type {
        PROTECTION_LOCAL,
        PROTECTION_RF
    }

    /**
     * Z-Wave LocalProtectionType enumeration. The LocalProtection type indicates the type of local protection that is
     * reported.
     *
     * @author Jorg de Jong
     */
    @XStreamAlias("localProtection")
    public enum LocalProtectionType {
        UNPROTECTED,
        SEQUENCE,
        PROTECTED;
    }

    /**
     * Z-Wave RfProtectionType enumeration. The RfProtection type indicates the type of RF protection that is reported.
     *
     * @author Jorg de Jong
     */
    @XStreamAlias("rfProtection")
    public enum RfProtectionType {
        UNPROTECTED,
        NORFCONTROL,
        NORFRESPONSE;
    }
}
