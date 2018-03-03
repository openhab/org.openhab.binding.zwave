/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
