/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * Handles the Sound Switch command class.
 *
 * @author Kennet Nielsen
 */
@XStreamAlias("COMMAND_CLASS_SOUND_SWITCH")
public class ZWaveSoundSwitchCommandClass extends ZWaveCommandClass {
    public enum Type {
        TONE_PLAY,
        VOLUME
    }

    private static final Logger logger = LoggerFactory.getLogger(ZWaveSoundSwitchCommandClass.class);

    
     /**
     * This command is used to set the configuration for playing tones at the supporting node.
     */
    private static final int CONFIGURATION_SET = 0x05;

     /**
     * This command is used to request the current configuration for playing tones at the supporting node
     */
    private static final int CONFIGURATION_GET = 0x06;
     
     /**
     * This command is used to advertise the current configuration for playing tones at the sending node.
     */
    private static final int CONFIGURATION_REPORT = 0x07;
    
     /**
     * This command is used to instruct a supporting node to play (or stop playing) a tone.
     */
    private static final int TONE_PLAY_SET = 0x08;
   
     /**
     * This command is used to request the current tone being played by the receiving node.
     */
    private static final int TONE_PLAY_GET = 0x09;

     /**
     * This command is used to advertise the current tone being played by the sending node
     */
    private static final int TONE_PLAY_REPORT = 0x0A;
    
    /**
     * Creates a new instance of the ZWaveSoundSwitchCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveSoundSwitchCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SOUND_SWITCH;
    }

    @ZWaveResponseHandler(id = CONFIGURATION_REPORT, name = "CONFIGURATION_REPORT")
    public void handleConfigReport(ZWaveCommandClassPayload payload, int endpoint) {
        int volume = payload.getPayloadByte(2);
        //int defaultTone = payload.getPayloadByte(3);
        logger.debug("NODE {}: Config report - volume={}", this.getNode().getNodeId(), volume);

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                CommandClass.COMMAND_CLASS_SOUND_SWITCH,volume , Type.VOLUME );
        this.getController().notifyEventListeners(zEvent);
    }
     
    @ZWaveResponseHandler(id = TONE_PLAY_REPORT, name = "TONE_PLAY_REPORT")
    public void handleIndicatorReport(ZWaveCommandClassPayload payload, int endpoint) {
        int playTone = payload.getPayloadByte(2);
        logger.debug("NODE {}: Tone play report - playTone={}", this.getNode().getNodeId(), playTone);

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                CommandClass.COMMAND_CLASS_SOUND_SWITCH,  playTone , Type.TONE_PLAY);
        this.getController().notifyEventListeners(zEvent);
    }

    /**
     * Gets a SerialMessage with the TONE_PLAY_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command TONE_PLAY_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), TONE_PLAY_GET)
                .withExpectedResponseCommand(TONE_PLAY_REPORT).withPriority(TransactionPriority.Get).build();
    }

    public ZWaveCommandClassTransactionPayload setValueMessage(int value) {
        logger.debug("NODE {}: Creating new message for application command TONE_PLAY_SET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), TONE_PLAY_SET)
                .withPayload(value).withPriority(TransactionPriority.Set).build();
    }

    public ZWaveCommandClassTransactionPayload getConfigMessage() {
        logger.debug("NODE {}: Creating new message for application command CONFIGURATION_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), CONFIGURATION_GET)
                .withExpectedResponseCommand(CONFIGURATION_REPORT).withPriority(TransactionPriority.Get).build();
    }

    public ZWaveCommandClassTransactionPayload setConfigMessage(int volume) {
        logger.debug("NODE {}: Creating new message for application command CONFIGURATION_SET", this.getNode().getNodeId());
        
        int defaultTone = 0; // The value 0x00 MUST indicate that the receiving node MUST NOT update its current default tone and
        //the command is sent to configure the volume only.
        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), CONFIGURATION_SET)
                .withPayload(volume,defaultTone).withPriority(TransactionPriority.Set).build();
    }
}
