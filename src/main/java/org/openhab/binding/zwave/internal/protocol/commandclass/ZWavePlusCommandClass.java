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
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassZwaveplusInfoV1;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassZwaveplusInfoV2;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the ZWave Plus Command command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_ZWAVEPLUS_INFO")
public class ZWavePlusCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWavePlusCommandClass.class);

    @SuppressWarnings("unused")
    private int zwPlusVersion = 0;
    private String zwPlusRole;
    private String zwPlusNodeType;
    @SuppressWarnings("unused")
    private String zwPlusUserIcon;
    @SuppressWarnings("unused")
    private String zwPlusInstallerIcon;

    @XStreamOmitField
    private boolean initialiseDone = false;
    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWavePlusCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWavePlusCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_ZWAVEPLUS_INFO;
    }

    @ZWaveResponseHandler(id = CommandClassZwaveplusInfoV1.ZWAVEPLUS_INFO_REPORT, name = "ZWAVEPLUS_INFO_REPORT")
    public void handleZwavePlusReport(ZWaveCommandClassPayload payload, int endpoint) {
        Map<String, Object> response;

        switch (getVersion()) {
            default:
            case 1:
                response = CommandClassZwaveplusInfoV1.handleZwaveplusInfoReport(payload.getPayloadBuffer());
                break;
            case 2:
                response = CommandClassZwaveplusInfoV2.handleZwaveplusInfoReport(payload.getPayloadBuffer());
                break;
        }

        zwPlusVersion = (Integer) response.get("Z_WAVE_PLUS_VERSION");
        zwPlusRole = (String) response.get("ROLE_TYPE");
        zwPlusNodeType = (String) response.get("NODE_TYPE");

        // These are only available in V2 so will be null
        zwPlusInstallerIcon = (String) response.get("INSTALLER_ICON_TYPE");
        zwPlusUserIcon = (String) response.get("USER_ICON_TYPE");

        initialiseDone = true;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(getValueMessage());
        }
        return result;
    }

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        byte[] payload;
        switch (this.getVersion()) {
            default:
            case 1:
                payload = CommandClassZwaveplusInfoV1.getZwaveplusInfoGet();
                break;
            case 2:
                payload = CommandClassZwaveplusInfoV2.getZwaveplusInfoGet();
                break;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), payload)
                .withExpectedResponseCommand(CommandClassZwaveplusInfoV1.ZWAVEPLUS_INFO_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Return the ZWave Plus Device Type
     *
     * @return
     */
    public String getRoleType() {
        return zwPlusRole;
    }

    public String getNodeType() {
        return zwPlusNodeType;
    }
}
