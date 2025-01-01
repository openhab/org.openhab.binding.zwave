/**
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
package org.openhab.binding.zwave.internal;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Implements a converter which re-orders set on marshalling
 *
 * @author Sami Salonen
 */
public class OrderingSetConverter extends CollectionConverter {

    public static boolean allSameClassAndComparable(Set<@Nullable ?> src) {
        Class<?> expectedClass = null;
        for (Object object : src) {
            if (!(object instanceof Comparable<?>)) {
                // not comparable or simply null
                return false;
            } else if (expectedClass == null) {
                // first item
                expectedClass = object.getClass();
            } else if (!object.getClass().equals(expectedClass)) {
                // various classes in the Set, let's not try to sort
                return false;
            }
        }
        return true;
    }

    public OrderingSetConverter(Mapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return type.equals(HashSet.class) || type.equals(LinkedHashSet.class);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Set<Object> set = (Set<Object>) source;
        if (!allSameClassAndComparable(set)) {
            super.marshal(source, writer, context);
            return;
        }
        Set<Object> ordered = new LinkedHashSet(set.size());
        // Map keys are comparable as verified above so casting to plain Comparator is safe
        final Stream<@Nullable Object> sortedStream = set.stream().sorted();
        sortedStream.forEachOrdered(val -> {
            ordered.add(val);
        });
        super.marshal(ordered, writer, context);
    }
}
