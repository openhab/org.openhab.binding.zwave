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
package org.openhab.binding.zwave.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.handler.ZWaveThingHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Implementation of the {@link ThingActions} interface used for executing Z-Wave device node actions.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ZWaveThingActions.class)
@ThingActionsScope(name = "zwave")
@NonNullByDefault
public class ZWaveThingActions implements ThingActions {
    private @Nullable ZWaveThingHandler handler;

    public static boolean setNodeAsFailed(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.setNodeAsFailed();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    public static boolean removeFailedNode(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.removeFailedNode();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    public static boolean reinitNode(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.reinitNode();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    public static boolean healNode(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.healNode();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (ZWaveThingHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Set device as FAILed", description = "Set this node as failed")
    public @ActionOutput(type = "boolean", label = "Success") boolean setNodeAsFailed() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.setNodeAsFailed();
        }
        return false;
    }

    @RuleAction(label = "Remove device from controller", description = "Remove this node from the controller if in failed state")
    public @ActionOutput(type = "boolean", label = "Success") boolean removeFailedNode() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.removeFailedNode();
        }
        return false;
    }

    @RuleAction(label = "Reinitialise the device", description = "Reinitialise this node")
    public @ActionOutput(type = "boolean", label = "Success") boolean reinitNode() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.reinitNode();
        }
        return false;
    }

    @RuleAction(label = "Heal the device", description = "Heal this node")
    public @ActionOutput(type = "boolean", label = "Success") boolean healNode() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.healNode();
        }
        return false;
    }
}
