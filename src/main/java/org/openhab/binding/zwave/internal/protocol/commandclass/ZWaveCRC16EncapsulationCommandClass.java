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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
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
 * Handles the CRC 16 Encapsulation Command command class.
 *
 * @author Chris Jackson
 * @author Hans Vanbellingen
 */
@XStreamAlias("COMMAND_CLASS_CRC_16_ENCAP")
public class ZWaveCRC16EncapsulationCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCRC16EncapsulationCommandClass.class);

    // private static final byte CRC_ENCAPSULATION_ENCAP = 1;

    /**
     * Creates a new instance of the ZWaveMultiCommandCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveCRC16EncapsulationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_CRC_16_ENCAP;
    }

    /**
     * Handle the crc16 encapsulated message. This processes the received frame, checks the crc and forwards to the real
     * command class.
     *
     * @param serialMessage
     *            The received message
     * @param offset
     *            The starting offset into the payload
     * @return
     * @throws ZWaveSerialMessageException
     */
    // @ZWaveResponseHandler(id = CRC_ENCAPSULATION_ENCAP, name = "CRC_ENCAPSULATION_ENCAP")
    public ZWaveCommandClassPayload handleCrcEncap(ZWaveCommandClassPayload payload) {
        // calculate CRC
        byte[] messageCrc = payload.getPayloadBuffer(payload.getPayloadLength() - 2, payload.getPayloadLength());
        byte[] tocheck = payload.getPayloadBuffer(0, payload.getPayloadLength() - 2);

        short calculatedCrc = crc_ccit(tocheck);
        // check if messageCrc = calculatedCrc
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.putShort(calculatedCrc);
        if (!Arrays.equals(messageCrc, byteBuffer.array())) {
            logger.info("NODE {}: CRC check failed message contains {} but should be {}", getNode().getNodeId(),
                    SerialMessage.bb2hex(messageCrc), SerialMessage.bb2hex(byteBuffer.array()));
            return null;
        }

        return new ZWaveCommandClassPayload(payload, 2, payload.getPayloadLength() - 2);

        /**
         * // Execute underlying command
         * ZWaveCommandClassPayload encapPayload = new ZWaveCommandClassPayload(payload, 2,
         * payload.getPayloadLength() - 2);
         * CommandClass commandClass;
         * ZWaveCommandClass zwaveCommandClass;
         * int commandClassCode = encapPayload.getCommandClassId();
         * commandClass = CommandClass.getCommandClass(commandClassCode);
         * if (commandClass == null) {
         * logger.error(String.format("NODE %d: Unsupported command class 0x%02x", getNode().getNodeId(),
         * commandClassCode));
         * } else {
         * zwaveCommandClass = getNode().getCommandClass(commandClass);
         *
         * // Apparently, this node supports a command class that we did not
         * // get (yet) during initialization.
         * // Let's add it now then to support handling this message.
         * if (zwaveCommandClass == null) {
         * logger.debug(String.format("NODE %d: Command class %s (0x%02x) not found, trying to add it.",
         * getNode().getNodeId(), commandClass, commandClass.getKey()));
         *
         * zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(), getNode(), getController());
         *
         * if (zwaveCommandClass != null) {
         * logger.debug(String.format("NODE %d: Adding command class %s (0x%02x)", getNode().getNodeId(),
         * commandClass, commandClass.getKey()));
         * getNode().addCommandClass(zwaveCommandClass);
         * }
         * }
         *
         * if (zwaveCommandClass == null) {
         * logger.error(String.format("NODE %d: CommandClass %s (0x%02x) not implemented.", getNode().getNodeId(),
         * commandClass, commandClassCode));
         * } else {
         * logger.debug("NODE {}: Calling handleApplicationCommandRequest.", getNode().getNodeId());
         * zwaveCommandClass.handleApplicationCommandRequest(encapPayload, endpoint);
         * }
         * }
         */
    }

    private short crc_ccit(final byte[] args) {
        int crc = 0x1D0F;
        int polynomial = 0x1021;
        for (byte b : args) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                // If coefficient of bit and remainder polynomial = 1 xor crc with polynomial
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xffff;
        return (short) crc;
    }
}
