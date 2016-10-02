package org.openhab.binding.zwave.test.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.mockito.AdditionalAnswers;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.zwave.internal.converter.ZWaveCommandClassConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Base class for {@link ZWaveCommandClassConverter} tests
 *
 * @author Chris Jackson - initial contribution
 *
 */
public class ZWaveCommandClassConverterTest {
    ZWaveNode CreateMockedNode(final int version, final Map<String, String> options) {
        final Map<Integer, ZWaveCommandClass> classes = new HashMap<Integer, ZWaveCommandClass>();
        final ZWaveNode node = Mockito.mock(ZWaveNode.class);
        final ZWaveController controller = Mockito.mock(ZWaveController.class);
        Mockito.when(node.encapsulate(Matchers.any(ZWaveTransaction.class), Matchers.any(ZWaveCommandClass.class),
                Matchers.anyInt())).then(AdditionalAnswers.returnsFirstArg());

        Mockito.when(node.resolveCommandClass(Matchers.any(CommandClass.class), Matchers.anyInt()))
                .thenAnswer(new Answer<ZWaveCommandClass>() {
                    @Override
                    public ZWaveCommandClass answer(InvocationOnMock invocation) {
                        if (classes.get(((CommandClass) invocation.getArguments()[0]).getKey()) != null) {
                            return classes.get(((CommandClass) invocation.getArguments()[0]).getKey());
                        }
                        ZWaveCommandClass commandClass = ZWaveCommandClass
                                .getInstance(((CommandClass) invocation.getArguments()[0]).getKey(), node, controller);
                        commandClass.setVersion(version);
                        commandClass.setOptions(options);

                        classes.put(((CommandClass) invocation.getArguments()[0]).getKey(), commandClass);
                        return commandClass;
                    }
                });

        return node;
    }
}
