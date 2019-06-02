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
package org.openhab.binding.zwave.event;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.openhab.binding.zwave.internal.ZWaveActivator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author Chris Jackson
 *
 */
@Component(immediate = true, service = { EventFactory.class })
public class BindingEventFactory extends AbstractEventFactory {
    private static final String BINDING_EVENT_TOPIC = "smarthome/binding/{binding}/{entity}/{event}";
    private static final String I18_PREFIX = "@text/";

    private static TranslationProvider translationProvider;

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        translationProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        translationProvider = null;
    }

    public BindingEventFactory(Set<String> supportedEventTypes) {
        super(supportedEventTypes);
    }

    public BindingEventFactory() {
        super(Collections.singleton(BindingEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        if (eventType.equals(BindingEvent.TYPE)) {
            return new BindingEvent(topic, source, payload);
        }

        throw new IllegalArgumentException(
                eventType + " not supported by " + BindingEventFactory.class.getSimpleName());
    }

    /**
     *
     * @param binding
     * @param entity
     * @param event
     * @param properties
     * @return
     */
    public static BindingEvent createBindingEvent(String binding, String entity, String event, BindingEventDTO dto) {
        String topic = BINDING_EVENT_TOPIC;
        topic = topic.replace("{binding}", binding);
        topic = topic.replace("{entity}", entity);
        topic = topic.replace("{event}", event);

        String payload = serializePayload(dto);
        return new BindingEvent(topic, binding, payload);
    }

    public static String formatEvent(String constant, Object... arguments) {
        String localConstant = constant;
        if (constant.startsWith(I18_PREFIX)) {
            localConstant = constant.substring(6);
            TranslationProvider translationProviderLocal = translationProvider;
            if (translationProviderLocal != null) {
                localConstant = translationProviderLocal.getText(ZWaveActivator.getContext().getBundle(), localConstant,
                        localConstant, null);
            }
        }

        return MessageFormat.format(localConstant, arguments);
    }

}