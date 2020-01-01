/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal.protocol;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AddNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveInclusionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle inclusion and exclusion
 *
 * @author Chris Jackson
 *
 */
public class ZWaveInclusionController implements ZWaveEventListener {
    private final Logger logger = LoggerFactory.getLogger(ZWaveInclusionController.class);

    private final ZWaveController controller;
    private Timer timer = new Timer();
    private TimerTask timerTask = null;
    private ZWaveInclusionState inclusionState = ZWaveInclusionState.Unknown;
    private final String networkSecurityKey;

    private int nodeId = 0;
    private Basic basicClass;
    private Generic genericClass;
    private Specific specificClass;
    private List<CommandClass> deviceCommands;

    private final int TIMER_MAIN = 90000;
    private final int TIMER_END = 15000;

    /**
     * Create the inclusion controller
     *
     * @param controller the {@link ZWaveController} to include a device into
     * @param networkSecurityKey the network security key
     */
    public ZWaveInclusionController(ZWaveController controller, String networkSecurityKey) {
        this.controller = controller;
        this.networkSecurityKey = networkSecurityKey;
    }

    /**
     * Starts a network inclusion process.
     *
     * @param highPower use high power inclusion
     * @param networkWide use network wide inclusion
     */
    public void startInclusion(boolean highPower, boolean networkWide) {
        if (inclusionState != ZWaveInclusionState.Unknown) {
            logger.debug("ZWave controller unable to start inclusion - state is {}", inclusionState);
            return;
        }
        inclusionState = ZWaveInclusionState.IncludeSent;
        controller.addEventListener(this);
        logger.debug("ZWave controller start inclusion");
        startTimer(TIMER_MAIN);
        controller.enqueue(new AddNodeMessageClass().doRequestStart(highPower, networkWide));
    }

    /**
     * Starts a network exclusion process
     */
    public void startExclusion() {
        if (inclusionState != ZWaveInclusionState.Unknown) {
            logger.debug("ZWave controller unable to start exclusion - state is {}", inclusionState);
            return;
        }
        inclusionState = ZWaveInclusionState.ExcludeSent;
        controller.addEventListener(this);
        logger.debug("ZWave controller start exclusion");
        startTimer(TIMER_MAIN);
        controller.enqueue(new RemoveNodeMessageClass().doRequestStart());
    }

    /**
     * Stops any pending inclusion/exclusion. Note that if an inclusion is currently in progress (ie we have received
     * notification from the controller that a device is being added) then we don't stop the inclusion process, but let
     * this complete naturally.
     */
    public void stop() {
        logger.debug("ZWave controller stopping inclusion at {}", inclusionState);
        stopTimer();

        switch (inclusionState) {
            case Unknown:
            case IncludeDone:
            case ExcludeDone:
                includeDone();
                break;

            case IncludeSent:
            case IncludeStart:
            case IncludeProtocolDone:
            case IncludeFail:
                inclusionState = ZWaveInclusionState.IncludeDone;
                controller.enqueue(new AddNodeMessageClass().doRequestStop(false));
                startTimer(TIMER_END);
                break;

            case ExcludeSent:
            case ExcludeStart:
            case ExcludeFail:
                inclusionState = ZWaveInclusionState.ExcludeDone;
                controller.enqueue(new RemoveNodeMessageClass().doRequestStop(false));
                startTimer(TIMER_END);
                break;

            case IncludeSlaveFound:
            case IncludeControllerFound:
            case ExcludeSlaveFound:
            case ExcludeControllerFound:
                break;

            default:
                break;
        }
    }

    /**
     * Gets the current inclusion/exclusion state
     *
     * @return the current {@link ZWaveInclusionState}
     */
    public ZWaveInclusionState getState() {
        return inclusionState;
    }

    private void includeDone() {
        logger.debug("ZWave controller end exclusion");
        controller.removeEventListener(this);
        stopTimer();
        controller.includeDone();
    }

    @Override
    protected void finalize() {
        logger.debug("ZWave inclusion controller finalised");
        stopTimer();
        controller.removeEventListener(this);
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        // We also need to handle the inclusion internally within the controller
        if (!(event instanceof ZWaveInclusionEvent)) {
            return;
        }

        ZWaveInclusionEvent incEvent = (ZWaveInclusionEvent) event;
        logger.debug("Inclusion event: Current state {}, new event {}", inclusionState, incEvent.getEvent());

        inclusionState = incEvent.getEvent();
        switch (incEvent.getEvent()) {
            case IncludeStart:
                break;

            case IncludeControllerFound:
            case IncludeSlaveFound:
                startTimer(TIMER_MAIN);
                // When a device is found we get the IncludeSlaveFound notification.
                // Here we need to end inclusion.
                logger.debug("NODE {}: Including node.", incEvent.getNodeId());

                // First make sure this isn't an existing node
                if (controller.getNode(incEvent.getNodeId()) != null) {
                    logger.debug("NODE {}: Newly included node already exists - not initialising.",
                            incEvent.getNodeId());
                    break;
                }

                // Remember the data
                nodeId = incEvent.getNodeId();
                deviceCommands = incEvent.getCommandClasses();
                basicClass = incEvent.getBasic();
                genericClass = incEvent.getGeneric();
                specificClass = incEvent.getSpecific();
                break;

            case IncludeProtocolDone:
                controller.enqueue(new AddNodeMessageClass().doRequestStop(false));
                startTimer(TIMER_END);

                // node ID will be 0 if no node was added
                if (nodeId == 0) {
                    logger.debug("Inclusion protocol completed with nodeId == 0! Nothing to do!");
                    break;
                }

                logger.debug("NODE {}: Inclusion protocol completed.", nodeId);

                // Create a new node
                ZWaveNode newNode = new ZWaveNode(controller.getHomeId(), nodeId, controller);
                ZWaveDeviceClass deviceClass = newNode.getDeviceClass();
                deviceClass.setBasicDeviceClass(basicClass);
                deviceClass.setGenericDeviceClass(genericClass);
                deviceClass.setSpecificDeviceClass(specificClass);

                // Add mandatory classes
                newNode.addCommandClass(ZWaveCommandClass.getInstance(CommandClass.COMMAND_CLASS_NO_OPERATION.getKey(),
                        newNode, controller));
                newNode.addCommandClass(
                        ZWaveCommandClass.getInstance(CommandClass.COMMAND_CLASS_BASIC.getKey(), newNode, controller));
                newNode.updateNifClasses(deviceCommands);

                boolean control = false;
                // If we have the NIF as part of the inclusion, use it
                for (CommandClass commandClass : deviceCommands) {
                    // Check if this is the control marker
                    if (commandClass == CommandClass.COMMAND_CLASS_MARK) {
                        control = true;
                        continue;
                    }

                    ZWaveCommandClass zwaveCommandClass;
                    if (commandClass == CommandClass.COMMAND_CLASS_SECURITY_2) {
                        // S0 is mandatory when S2 is supported, so we add that instead for now
                        zwaveCommandClass = ZWaveCommandClass.getInstance(CommandClass.COMMAND_CLASS_SECURITY.getKey(),
                                newNode, controller);
                    } else {
                        zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(), newNode, controller);
                    }
                    if (zwaveCommandClass != null) {
                        logger.debug("NODE {}: Inclusion is adding command class {}.", incEvent.getNodeId(),
                                commandClass);

                        // Add the network key to the security class
                        if (commandClass == CommandClass.COMMAND_CLASS_SECURITY) {
                            ((ZWaveSecurityCommandClass) zwaveCommandClass).setNetworkKey(networkSecurityKey);
                        }
                        zwaveCommandClass.setControlClass(control);
                        newNode.addCommandClass(zwaveCommandClass);
                    }
                }

                newNode.setInclusionTimer();

                // Kick off the initialisation.
                // Since the node is awake, we jump straight into the initialisation sequence
                // without some of the initial stages like PING that are designed to detect if
                // the device is responding.
                // This is primarily designed to speed up the secure inclusion but is valid for all.
                controller.includeNode(newNode);
                break;

            case IncludeFail:
                controller.enqueue(new AddNodeMessageClass().doRequestStop(false));
                startTimer(TIMER_END);
                break;

            case IncludeDone:
                controller.enqueue(new AddNodeMessageClass().doRequestStop(true));
                includeDone();
                break;

            case ExcludeNodeFound:
                startTimer(TIMER_END);
                break;

            case ExcludeSlaveFound:
            case ExcludeControllerFound:
                startTimer(TIMER_END);
                nodeId = incEvent.getNodeId();
                break;

            case ExcludeFail:
                controller.enqueue(new RemoveNodeMessageClass().doRequestStop(false));
                startTimer(TIMER_END);
                break;

            case ExcludeDone:
                controller.enqueue(new RemoveNodeMessageClass().doRequestStop(true));

                // Ignore node 0 - this just indicates exclusion finished
                if (nodeId != 0) {
                    logger.debug("NODE {}: Excluding node.", incEvent.getNodeId());
                    // Remove the node from the controller
                    if (controller.getNode(incEvent.getNodeId()) == null) {
                        logger.debug("NODE {}: Excluding node that doesn't exist.", incEvent.getNodeId());
                    } else {
                        controller.removeNode(nodeId);
                    }
                }

                // End exclusion
                includeDone();
                break;
            default:
                break;
        }
    }

    // The following timer class implements a re-triggerable timer to stop the inclusion
    // mode after 30 seconds.
    private class InclusionTimerTask extends TimerTask {
        @Override
        public void run() {
            logger.debug("Inclusion timer at {}", inclusionState);
            stopTimer();
            switch (inclusionState) {
                case Unknown:
                case IncludeSent:
                case IncludeStart:
                case IncludeSlaveFound:
                case IncludeControllerFound:
                    inclusionState = ZWaveInclusionState.IncludeFail;
                    startTimer(TIMER_END);
                    controller.enqueue(new AddNodeMessageClass().doRequestStop(false));
                    break;

                case IncludeProtocolDone:
                case IncludeFail:
                    controller.enqueue(new AddNodeMessageClass().doRequestStop(true));
                    includeDone();
                    break;

                case IncludeDone:
                    // We already notified the ZWaveController we were done!
                    break;

                case ExcludeSent:
                case ExcludeStart:
                case ExcludeNodeFound:
                case ExcludeSlaveFound:
                case ExcludeControllerFound:
                    inclusionState = ZWaveInclusionState.ExcludeFail;
                    controller.enqueue(new RemoveNodeMessageClass().doRequestStop(false));
                    startTimer(TIMER_END);
                    break;

                case ExcludeDone:
                case ExcludeFail:
                    controller.enqueue(new RemoveNodeMessageClass().doRequestStop(true));
                    includeDone();
                    break;
                default:
                    break;
            }
        }
    }

    private synchronized void startTimer(int period) {
        // Stop any existing timer
        stopTimer();

        // Create the timer task
        timerTask = new InclusionTimerTask();

        // Start the timer
        timer.schedule(timerTask, period);
    }

    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}
