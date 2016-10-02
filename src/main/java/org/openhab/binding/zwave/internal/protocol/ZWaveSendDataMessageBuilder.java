package org.openhab.binding.zwave.internal.protocol;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Builder class for serial message
 *
 * @author Chris Jackson - Initial Implementation
 *
 */
public class ZWaveSendDataMessageBuilder extends ZWaveMessageBuilder {
    private CommandClass commandClass;
    private int command;

    public ZWaveSendDataMessageBuilder() {
        super(SerialMessageClass.SendData);
    }

    public ZWaveSendDataMessageBuilder withCommandClass(CommandClass commandClass, int command) {
        this.commandClass = commandClass;
        this.command = command;
        return this;
    }

    public ZWaveSendDataMessageBuilder withNoResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SerialMessage build() {
        SerialMessage serialMessage = new SerialMessage(nodeId, messageClass, messageType);

        byte[] output;
        if (payload == null) {
            output = new byte[4];
        } else {
            output = new byte[4 + payload.length];
        }

        output[0] = (byte) nodeId;
        output[1] = (byte) (output.length - 2);

        output[2] = (byte) commandClass.getKey();
        output[3] = (byte) command;
        for (int i = 4; i < output.length; ++i) {
            output[i] = payload[i - 4];
        }

        serialMessage.setMessagePayload(output);
        return serialMessage;
    }
}
