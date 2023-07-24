/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Implements a converter which re-orders map on marshalling
 *
 * @author Sami Salonen
 */
public class OrderingMapConverter extends MapConverter {

    public OrderingMapConverter(Mapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return type.equals(HashMap.class) || type.getName().equals("java.util.concurrent.ConcurrentHashMap");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map<Object, Object> map = (Map<Object, Object>) source;
        Map<Object, Object> ordered = new LinkedHashMap<>(map.size());

        if (OrderingSetConverter.allSameClassAndComparable(map.keySet())) {
            // Map keys are comparable as verified above so casting to plain Comparator is safe
            final Stream<Entry<@Nullable Object, @Nullable Object>> sortedStream = map.entrySet().stream()
                    .sorted((Comparator) Map.Entry.comparingByKey());
            sortedStream.forEachOrdered(entry -> {
                ordered.put(entry.getKey(), entry.getValue());
            });
            super.marshal(ordered, writer, context);
        } else {
            super.marshal(map, writer, context);
        }
    }
}
