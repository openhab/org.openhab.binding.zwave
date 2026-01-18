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
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Implementation of the {@link ThingActions} interface used for executing Z-Wave controller actions.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ZWaveControllerActions.class)
@ThingActionsScope(name = "zwave")
@NonNullByDefault
public class ZWaveControllerActions implements ThingActions {
    private @Nullable ZWaveControllerHandler handler;

    public static boolean softReset(ThingActions actions) {
        if (actions instanceof ZWaveControllerActions controllerActions) {
            return controllerActions.softReset();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveControllerActions");
        }
    }

    public static boolean hardReset(ThingActions actions) {
        if (actions instanceof ZWaveControllerActions controllerActions) {
            return controllerActions.hardReset();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveControllerActions");
        }
    }

    public static boolean exclude(ThingActions actions) {
        if (actions instanceof ZWaveControllerActions controllerActions) {
            return controllerActions.exclude();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveControllerActions");
        }
    }

    public static boolean sync(ThingActions actions) {
        if (actions instanceof ZWaveControllerActions controllerActions) {
            return controllerActions.sync();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ZWaveControllerActions");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (ZWaveControllerHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actions.controller-soft-reset.label", description = "@text/actions.controller-soft-reset.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean softReset() {
        ZWaveController controller = getController();
        if (controller != null) {
            controller.requestSoftReset();
            return true;
        }
        return false;
    }

    @RuleAction(label = "@text/actions.controller-hard-reset.label", description = "@text/actions.controller-hard-reset.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean hardReset() {
        ZWaveController controller = getController();
        if (controller != null) {
            controller.requestHardReset();
            return true;
        }
        return false;
    }

    @RuleAction(label = "@text/actions.controller-exclude.label", description = "actions.controller-exclude.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean exclude() {
        ZWaveController controller = getController();
        if (controller != null) {
            controller.requestRemoveNodesStart();
            return true;
        }
        return false;
    }

    @RuleAction(label = "@text/actions.controller-sync.label", description = "actions.controller-sync.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean sync() {
        ZWaveController controller = getController();
        if (controller != null) {
            controller.requestNetworkUpdate();
            return true;
        }
        return false;
    }

    private @Nullable ZWaveController getController() {
        ZWaveControllerHandler handler = this.handler;
        if (handler != null) {
            return handler.getController();
        }
        return null;
    }
}
