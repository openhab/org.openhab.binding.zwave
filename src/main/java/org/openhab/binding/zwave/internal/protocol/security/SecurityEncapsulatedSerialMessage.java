/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.security;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SerialMessage} which has been security encapsulated per the
 * semantics of the zwave spec.
 *
 * @author Dave Badia
 * @author Chris Jackson
 * @see ZWaveSecurityCommandClass
 */
public class SecurityEncapsulatedSerialMessage extends SerialMessage {
    private final static Logger logger = LoggerFactory.getLogger(SecurityEncapsulatedSerialMessage.class);

    private static final byte UNSET = -1;

    /**
     * The original message that was encapsulated in {@link ZWaveSecurityCommandClass} encapsulated messages
     */
    private SerialMessage messageBeingEncapsulated = null;

    /**
     * The command class that, when received, indicates that this security transaction is complete
     */
    private byte transactionCompleteCommandClass;

    /**
     * The command byte that, when received in conjunction with
     * {@link SecurityEncapsulatedSerialMessage#transactionCompleteCommandClass},
     * indicates that this security transaction is complete. This is optional, and be default will be set to
     * {@link #UNSET}
     */
    private byte transactionCompleteCommand = UNSET;

    private AtomicBoolean securityTransactionComplete = new AtomicBoolean(false);

    private long transmittedAt = UNSET;

    private byte deviceNonceId;

    /**
     * The original {@link ZWaveSecurityPayloadFrame} from which this message was formed
     */
    private ZWaveSecurityPayloadFrame securityPayload;

    // TODO: DB inherit messageClass and messageType too?
    public SecurityEncapsulatedSerialMessage(SerialMessageClass messageClass, SerialMessageType messageType,
            SerialMessage messageBeingEncapsulated) {
        // Inherit most fields
        super(messageBeingEncapsulated.getMessageNode(), messageClass, messageType,
                messageBeingEncapsulated.getExpectedReply(), ZWaveSecurityCommandClass.SECURITY_MESSAGE_PRIORITY);
        this.messageBeingEncapsulated = messageBeingEncapsulated;
        // Inherit attempts from messageBeingEncapsulated since each retry requires a new
        // SecurityEncapsulatedSerialMessage object
        this.transactionCompleteCommandClass = messageBeingEncapsulated.getMessagePayload()[2];
    }

    public boolean isSecurityTransactionComplete() {
        boolean result = hasBeenTransmitted();
        if (result) {
            result = securityTransactionComplete.get();
            // TODO: set isCompleted to true on door lock set
            // boolean isDoorLockSetMessage = bytesAreEqual(securityPayload.getMessageBytes()[0],
            // ZWaveCommandClass.CommandClass.DOOR_LOCK.getKey())
            // && bytesAreEqual(securityPayload.getMessageBytes()[1], ZWaveDoorLockCommandClass.DOORLOCK_SET);
        }
        logger.debug(
                "NODE {}: securityTransactionComplete={}, payload=({}), transmitted={}, msSinceTransmitted={}, ackWaiting={}",
                messageNode, result, SerialMessage.bb2hex(messageBeingEncapsulated.getMessagePayload()),
                hasBeenTransmitted(), hasBeenTransmitted() ? (System.currentTimeMillis() - getTransmittedAt()) : "",
                ackPending);
        return result;
    }

    /**
     * Checks to see if the given response satisfies the security transaction complete conditions
     * Call {@link #isSecurityTransactionComplete()} afterwards to see if it did
     */
    public void securityReponseReceived(byte[] payloadBytes) {
        if (isSecurityTransactionComplete()) {
            logger.debug("NODE {}: securityReponseReceived is already true, nothing to check", getMessageNode());
            return;
        }
        // TODO: boolean appCommandHandler = ZWaveSecurityCommandClass.bytesAreEqual(payloadBytes[1],
        // SerialMessageClass.ApplicationCommandHandler.getKey());
        boolean result = payloadBytes[1] == transactionCompleteCommandClass;
        if (result && transactionCompleteCommand != UNSET) {
            result = ZWaveSecurityCommandClass.bytesAreEqual(transactionCompleteCommand, payloadBytes[3]);
        }
        logger.debug("NODE {}: securityReponseReceived={} for {}. Class: want={},got={}; Command: want={},got={}",
                getMessageNode(), result, SerialMessage.bb2hex(payloadBytes),
                CommandClass.getCommandClass(transactionCompleteCommandClass & 0xff),
                CommandClass.getCommandClass(payloadBytes[1] & 0xff),
                transactionCompleteCommand == UNSET ? "ANY" : SerialMessage.b2hex(transactionCompleteCommand),
                SerialMessage.b2hex(payloadBytes[2]));
        if (result) {
            securityTransactionComplete.set(true);
        }
    }

    /**
     * @return the original message that was encapsulated, or null if this
     *         {@link SerialMessage} is not a {@link ZWaveSecurityCommandClass} encapsulated message type
     */
    public SerialMessage getMessageBeingEncapsulated() {
        return messageBeingEncapsulated;
    }

    public long getTransmittedAt() {
        return transmittedAt;
    }

    public void setTransmittedAt() {
        this.transmittedAt = System.currentTimeMillis();
    }

    public boolean hasBeenTransmitted() {
        return this.transmittedAt != UNSET;
    };

    public void setDeviceNonceId(byte nonceId) {
        this.deviceNonceId = nonceId;
    }

    public byte getDeviceNonceId() {
        return deviceNonceId;
    }

    public void setSecurityPayload(ZWaveSecurityPayloadFrame securityPayload) {
        this.securityPayload = securityPayload;
    }

    public ZWaveSecurityPayloadFrame getSecurityPayload() {
        return securityPayload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        if (getMessageBeingEncapsulated() != null) {
            buf.append("   encapsulates: ").append(getMessageBeingEncapsulated().toString());
        }
        return buf.toString();
    }
}
