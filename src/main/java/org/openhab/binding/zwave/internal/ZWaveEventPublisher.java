/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.events.EventPublisher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author Chris Jackson
 *
 */
@Component()
public class ZWaveEventPublisher {
    private static EventPublisher eventPublisher;

    @Reference
    public void setEventPublisher(EventPublisher eventPublisher) {
        ZWaveEventPublisher.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        ZWaveEventPublisher.eventPublisher = null;
    }

    public static EventPublisher getEventPublisher() {
        return eventPublisher;
    }
}
