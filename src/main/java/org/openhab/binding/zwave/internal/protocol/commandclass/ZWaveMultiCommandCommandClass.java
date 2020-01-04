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
import java.util.List;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Multi Command command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_MULTI_CMD")
public class ZWaveMultiCommandCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiCommandCommandClass.class);

    private static final int MULTI_COMMMAND_ENCAP = 0x01;

    /**
     * Creates a new instance of the ZWaveMultiCommandCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveMultiCommandCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_MULTI_CMD;
    }

    /**
     * Handle the multi command message. This processes the received frame, processing each
     * command class in turn.
     *
     * @param serialMessage The received message
     * @param offset The starting offset into the payload
     * @throws ZWaveSerialMessageException
     */
    // @ZWaveResponseHandler(id = MULTI_COMMMAND_ENCAP, name = "MULTI_COMMMAND_ENCAP")
    public List<ZWaveCommandClassPayload> handleMultiCommandEncap(ZWaveCommandClassPayload payload) {
        int classCnt = payload.getPayloadByte(2);

        List<ZWaveCommandClassPayload> commandList = new ArrayList<ZWaveCommandClassPayload>(classCnt);
        int offset = 3;
        // Iterate over all commands, adding them to the list
        for (int c = 0; c < classCnt; c++) {
            commandList.add(
                    new ZWaveCommandClassPayload(payload, offset + 1, offset + payload.getPayloadByte(offset) + 1));

            // Step over this class
            offset += payload.getPayloadByte(offset) + 1;
        }

        return commandList;
    }
}
