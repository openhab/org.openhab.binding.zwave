/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.zwave.internal.ZWaveActivator;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link ZWaveBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ZWaveBindingConstants {

    public static final String BINDING_ID = "zwave";

    // Controllers
    public final static ThingTypeUID CONTROLLER_SERIAL = new ThingTypeUID(BINDING_ID, "serial_zstick");

    public final static String CONFIGURATION_PORT = "port";
    public final static String CONFIGURATION_MASTER = "controller_master";
    public final static String CONFIGURATION_SUC = "controller_suc";
    public final static String CONFIGURATION_NETWORKKEY = "security_networkkey";
    public final static String CONFIGURATION_SECUREINCLUSION = "security_inclusionmode";
    public final static String CONFIGURATION_HEALTIME = "heal_time";
    public final static String CONFIGURATION_INCLUSION_MODE = "inclusion_mode";
    public final static String CONFIGURATION_INCLUSIONTIMEOUT = "controller_inclusiontimeout";

    public final static String CONFIGURATION_SWITCHALLMODE = "switchall_mode";
    public final static String CONFIGURATION_WAKEUPNODE = "wakeup_node";
    public final static String CONFIGURATION_WAKEUPINTERVAL = "wakeup_interval";

    public final static String CONFIGURATION_NODENAME = "nodename_name";
    public final static String CONFIGURATION_NODELOCATION = "nodename_location";

    public final static String CONFIGURATION_POWERLEVEL_LEVEL = "powerlevel_level";
    public final static String CONFIGURATION_POWERLEVEL_TIMEOUT = "powerlevel_timeout";

    public final static String CONFIGURATION_USERCODE = "usercode_";

    public final static String CONFIGURATION_DOORLOCKTIMEOUT = "doorlock_timeout";

    public final static String CONFIGURATION_POLLPERIOD = "binding_pollperiod";

    public final static String ZWAVE_THING = BINDING_ID + ":device";
    public final static ThingTypeUID ZWAVE_THING_UID = new ThingTypeUID(ZWAVE_THING);

    public final static String NODE_TITLE_FORMAT = "Z-Wave Node %d";

    public final static String PROPERTY_XML_MANUFACTURER = "manufacturerId";
    public final static String PROPERTY_XML_REFERENCES = "manufacturerRef";
    public final static String PROPERTY_XML_VERSIONMIN = "versionMin";
    public final static String PROPERTY_XML_VERSIONMAX = "versionMax";
    public final static String PROPERTY_XML_ASSOCIATIONS = "defaultAssociations";

    public final static String PROPERTY_MANUFACTURER = "zwave_manufacturer";
    public final static String PROPERTY_DEVICEID = "zwave_deviceid";
    public final static String PROPERTY_DEVICETYPE = "zwave_devicetype";
    public final static String PROPERTY_VERSION = "zwave_version";

    public final static String PROPERTY_NODEID = "zwave_nodeid";
    public final static String PROPERTY_NEIGHBOURS = "zwave_neighbours";
    public final static String PROPERTY_LISTENING = "zwave_listening";
    public final static String PROPERTY_FREQUENT = "zwave_frequent";
    public final static String PROPERTY_BEAMING = "zwave_beaming";
    public final static String PROPERTY_ROUTING = "zwave_routing";
    public final static String PROPERTY_CLASS_BASIC = "zwave_class_basic";
    public final static String PROPERTY_CLASS_GENERIC = "zwave_class_generic";
    public final static String PROPERTY_CLASS_SPECIFIC = "zwave_class_specific";
    public final static String PROPERTY_ZWPLUS_DEVICETYPE = "zwave_plus_devicetype";
    public final static String PROPERTY_WAKEUP_TIME = "zwave_wakeup_time";
    public final static String PROPERTY_USINGSECURITY = "zwave_secure";

    public final static String CHANNEL_SERIAL_SOF = "serial_sof";
    public final static String CHANNEL_SERIAL_ACK = "serial_ack";
    public final static String CHANNEL_SERIAL_NAK = "serial_nak";
    public final static String CHANNEL_SERIAL_CAN = "serial_can";
    public final static String CHANNEL_SERIAL_OOF = "serial_oof";
    public final static String CHANNEL_SERIAL_CSE = "serial_cse";

    public final static String CHANNEL_CFG_BINDING = "binding";
    public final static String CHANNEL_CFG_COMMANDCLASS = "commandClass";

    public final static I18nConstant OFFLINE_CTLR_OFFLINE = new I18nConstant("zwave.thingstate.controller_offline",
            "Controller is offline");
    public final static I18nConstant OFFLINE_NODE_DEAD = new I18nConstant("zwave.thingstate.node_dead",
            "Node is not communicating with controller");
    public final static I18nConstant OFFLINE_NODE_NOTFOUND = new I18nConstant("zwave.thingstate.node_notfound",
            "Node not found in Z-Wave network");
    public final static I18nConstant OFFLINE_SERIAL_EXISTS = new I18nConstant("zwave.thingstate.serial_notfound",
            "Serial Error: Port {0} does not exist");
    public final static I18nConstant OFFLINE_SERIAL_INUSE = new I18nConstant("zwave.thingstate.serial_inuse",
            "Serial Error: Port {0} is in use");
    public final static I18nConstant OFFLINE_SERIAL_UNSUPPORTED = new I18nConstant(
            "zwave.thingstate.serial_unsupported", "Serial Error: Unsupported operation on port {0}");
    public final static I18nConstant OFFLINE_SERIAL_LISTENERS = new I18nConstant("zwave.thingstate.serial_listeners",
            "Serial Error: Too many listeners on port {0}");

    public final static I18nConstant EVENT_INCLUSION_STARTED = new I18nConstant("zwave.event.inclusion_started",
            "Z-Wave network inclusion started");
    public final static I18nConstant EVENT_INCLUSION_COMPLETED = new I18nConstant("zwave.event.inclusion_completed",
            "Z-Wave network inclusion completed");
    public final static I18nConstant EVENT_INCLUSION_FAILED = new I18nConstant("zwave.event.inclusion_failed",
            "Z-Wave network inclusion failed");
    public final static I18nConstant EVENT_INCLUSION_SECURECOMPLETED = new I18nConstant(
            "zwave.event.inclusion_securecompleted", "Z-Wave Node {0} secure inclusion complete");
    public final static I18nConstant EVENT_INCLUSION_SECUREFAILED = new I18nConstant(
            "zwave.event.inclusion_securefailed", "Z-Wave Node {0} secure inclusion failed");

    public final static I18nConstant EVENT_EXCLUSION_STARTED = new I18nConstant("zwave.event.exclusion_started",
            "Z-Wave network exclusion started");
    public final static I18nConstant EVENT_EXCLUSION_COMPLETED = new I18nConstant("zwave.event.exclusion_completed",
            "Z-Wave network exclusion completed");
    public final static I18nConstant EVENT_EXCLUSION_FAILED = new I18nConstant("zwave.event.exclusion_failed",
            "Z-Wave network exclusion failed");
    public final static I18nConstant EVENT_EXCLUSION_NODEREMOVED = new I18nConstant("zwave.event.exclusion_failed",
            "Z-Wave network excluded node {0}");

    public final static I18nConstant EVENT_NETWORKUPDATE_DONE = new I18nConstant("zwave.event.networkupdate_done",
            "Z-Wave network update completed");
    public final static I18nConstant EVENT_NETWORKUPDATE_ABORT = new I18nConstant("zwave.event.networkupdate_abort",
            "Z-Wave network update aborted");
    public final static I18nConstant EVENT_NETWORKUPDATE_WAIT = new I18nConstant("zwave.event.networkupdate_wait",
            "Z-Wave network update failed as SUC is busy");
    public final static I18nConstant EVENT_NETWORKUPDATE_DISABLED = new I18nConstant(
            "zwave.event.networkupdate_disabled", "Z-Wave network update failed as no SUC is available");
    public final static I18nConstant EVENT_NETWORKUPDATE_OVERFLOW = new I18nConstant(
            "zwave.event.networkupdate_overflow", "Z-Wave network update failed as more than 64 updates are required");

    public final static I18nConstant EVENT_REMOVEFAILED_NOTFOUND = new I18nConstant("zwave.event.removenode_notfound",
            "Remove node {0} failed as node was not found");
    public final static I18nConstant EVENT_REMOVEFAILED_NOTCTLR = new I18nConstant(
            "zwave.event.removenode_nocontroller", "Remove node {0} failed - this is not the Primary controller");
    public final static I18nConstant EVENT_REMOVEFAILED_NOTREMOVED = new I18nConstant(
            "zwave.event.removenode_notremoved", "Unable to remove node {0} from the network");
    public final static I18nConstant EVENT_REMOVEFAILED_NOCALLBACK = new I18nConstant(
            "zwave.event.removenode_nocallback", "Remove node {0} failed - no callback function was provided");
    public final static I18nConstant EVENT_REMOVEFAILED_NODEOK = new I18nConstant("zwave.event.removenode_nodeok",
            "Remove node {0} failed - the node is operating");
    public final static I18nConstant EVENT_REMOVEFAILED_REMOVED = new I18nConstant("zwave.event.removenode_removed",
            "Node {0} successfully removed from the network");
    public final static I18nConstant EVENT_REMOVEFAILED_FAILED = new I18nConstant("zwave.event.removenode_failed",
            "Remove node {0} from the network failed");
    public final static I18nConstant EVENT_REMOVEFAILED_BUSY = new I18nConstant("zwave.event.removenode_busy",
            "Remove node {0} failed - the controller is busy");
    public final static I18nConstant EVENT_REMOVEFAILED_UNKNOWN = new I18nConstant("zwave.event.removenode_unknown",
            "Remove node {0} failed with unknown error");

    public final static I18nConstant CONFIG_BINDING_POLLINGPERIOD_LABEL = new I18nConstant(
            "zwave.config.binding_pollingperiod_label", "Polling Period");
    public final static I18nConstant CONFIG_BINDING_POLLINGPERIOD_DESC = new I18nConstant(
            "zwave.config.binding_pollingperiod_desc",
            "Set the minimum polling period for this device<BR/>"
                    + "Note that the polling period may be longer than set since the binding treats "
                    + "polls as the lowest priority data within the network.");

    public final static Integer ACTION_CHECK_VALUE = new Integer(-232323);

    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = ImmutableSet.of(CONTROLLER_SERIAL);

    private static I18nProvider i18nProvider;

    protected void setI18nProvider(I18nProvider i18nProvider) {
        ZWaveBindingConstants.i18nProvider = i18nProvider;
    }

    protected void unsetI18nProvider(I18nProvider i18nProvider) {
        ZWaveBindingConstants.i18nProvider = null;
    }

    public static String getI18nConstant(I18nConstant constant) {
        I18nProvider i18nProviderLocal = i18nProvider;
        if (i18nProviderLocal == null) {
            return MessageFormat.format(constant.key, (Object[]) null);
        }
        return i18nProviderLocal.getText(ZWaveActivator.getContext().getBundle(), constant.key, constant.defaultText,
                null, (Object[]) null);
    }

    public static String getI18nConstant(I18nConstant constant, Object... arguments) {
        I18nProvider i18nProviderLocal = i18nProvider;
        if (i18nProviderLocal == null) {
            return MessageFormat.format(constant.key, arguments);
        }
        return i18nProviderLocal.getText(ZWaveActivator.getContext().getBundle(), constant.key, constant.defaultText,
                null, arguments);
    }

    public static class I18nConstant {
        public String key;
        public String defaultText;

        I18nConstant(String key, String defaultText) {
            this.key = key;
            this.defaultText = defaultText;
        }
    }
}
