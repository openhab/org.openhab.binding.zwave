/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Base class for {@link ZWaveCommandClassConverter} tests
 *
 * @author Chris Jackson - initial contribution
 *
 */
public class ZWaveCommandClassConverterTest {
    ZWaveNode CreateMockedNode(final int version) {
        return CreateMockedNode(version, new HashMap<String, String>());
    }

    ZWaveNode CreateMockedNode(final int version, final Map<String, String> options) {
        final Map<Integer, ZWaveCommandClass> classes = new HashMap<Integer, ZWaveCommandClass>();
        final ZWaveNode node = Mockito.mock(ZWaveNode.class);
        final ZWaveController controller = Mockito.mock(ZWaveController.class);
        Mockito.when(node.encapsulate(ArgumentMatchers.any(ZWaveCommandClassTransactionPayload.class),
                ArgumentMatchers.anyInt())).then(AdditionalAnswers.returnsFirstArg());

        Mockito.when(node.resolveCommandClass(ArgumentMatchers.any(CommandClass.class), ArgumentMatchers.anyInt()))
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
