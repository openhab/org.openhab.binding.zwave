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
import org.openhab.core.automation.Visibility;
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

    public static String checkIsNodeFailed(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.checkIsNodeFailed();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    public static String removeFailedNode(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.removeFailedNode();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    public static String reinitNode(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.reinitNode();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    public static String healNode(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.healNode();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveThingActions");
        }
    }

    public static String replaceFailedNode(ThingActions actions) {
        if (actions instanceof ZWaveThingActions nodeActions) {
            return nodeActions.replaceFailedNode();
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

    @RuleAction(label = "@text/actions.node-failed.label", description = "@text/actions.node-failed.description", visibility = Visibility.EXPERT)
    public @ActionOutput(type = "String") String checkIsNodeFailed() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.checkIsNodeFailed();
        }
        return "Thing handler is null, check not possible";
    }

    @RuleAction(label = "@text/actions.node-remove.label", description = "@text/actions.node-remove.description", visibility = Visibility.EXPERT)
    public @ActionOutput(type = "String") String removeFailedNode() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.removeFailedNode();
        }
        return "Thing handler is null, removal not possible";
    }

    @RuleAction(label = "@text/actions.node-reinit.label", description = "@text/actions.node-reinit.description")
    public @ActionOutput(type = "String") String reinitNode() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.reinitNode();
        }
        return "Thing handler is null, re-interview not possible";
    }

    @RuleAction(label = "@text/actions.node-heal.label", description = "@text/actions.node-heal.description")
    public @ActionOutput(type = "String") String healNode() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.healNode();
        }
        return "Thing handler is null, Heal not possible.";
    }

    @RuleAction(label = "@text/actions.node-replace.label", description = "@text/actions.node-replace.description", visibility = Visibility.EXPERT)
    public @ActionOutput(type = "String") String replaceFailedNode() {
        ZWaveThingHandler handler = this.handler;
        if (handler != null) {
            return handler.replaceFailedNode();
        }
        return "Handler is null, cannot replace failed node";
    }
}
