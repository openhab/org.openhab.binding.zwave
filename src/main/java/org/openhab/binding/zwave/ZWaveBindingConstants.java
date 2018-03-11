/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
    public final static String CONFIGURATION_SISNODE = "controller_sisnode";
    public final static String CONFIGURATION_NETWORKKEY = "security_networkkey";
    public final static String CONFIGURATION_SECUREINCLUSION = "security_inclusionmode";
    public final static String CONFIGURATION_HEALTIME = "heal_time";
    public final static String CONFIGURATION_INCLUSION_MODE = "inclusion_mode";
    public final static String CONFIGURATION_INCLUSIONTIMEOUT = "controller_inclusiontimeout";
    public final static String CONFIGURATION_DEFAULTWAKEUPPERIOD = "controller_wakeupperiod";

    public final static String CONFIGURATION_NODEID = "node_id";

    public final static String CONFIGURATION_SWITCHALLMODE = "switchall_mode";
    public final static String CONFIGURATION_WAKEUPNODE = "wakeup_node";
    public final static String CONFIGURATION_WAKEUPINTERVAL = "wakeup_interval";

    public final static String CONFIGURATION_NODENAME = "nodename_name";
    public final static String CONFIGURATION_NODELOCATION = "nodename_location";

    public final static String CONFIGURATION_USERCODE_LABEL = "usercode_label_";
    public final static String CONFIGURATION_USERCODE_CODE = "usercode_code_";

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
    public final static String PROPERTY_ZWPLUS_ROLETYPE = "zwave_plus_roletype";
    public final static String PROPERTY_LASTWAKEUP = "zwave_lastwakeup";
    public final static String PROPERTY_USINGSECURITY = "zwave_secure";
    public final static String PROPERTY_LASTHEAL = "zwave_lastheal";

    public final static String CHANNEL_SERIAL_SOF = "serial_sof";
    public final static String CHANNEL_SERIAL_ACK = "serial_ack";
    public final static String CHANNEL_SERIAL_NAK = "serial_nak";
    public final static String CHANNEL_SERIAL_CAN = "serial_can";
    public final static String CHANNEL_SERIAL_OOF = "serial_oof";
    public final static String CHANNEL_SERIAL_CSE = "serial_cse";

    public final static String CHANNEL_CFG_BINDING = "binding";
    public final static String CHANNEL_CFG_COMMANDCLASS = "commandClass";

    public final static String OFFLINE_CTLR_OFFLINE = "@text/zwave.thingstate.controller_offline";
    public final static String OFFLINE_NODE_DEAD = "@text/zwave.thingstate.node_dead";
    public final static String OFFLINE_NODE_NOTFOUND = "@text/zwave.thingstate.node_notfound";
    public final static String OFFLINE_SERIAL_EXISTS = "@text/zwave.thingstate.serial_notfound";
    public final static String OFFLINE_SERIAL_INUSE = "@text/zwave.thingstate.serial_inuse";
    public final static String OFFLINE_SERIAL_UNSUPPORTED = "@text/zwave.thingstate.serial_unsupported";
    public final static String OFFLINE_SERIAL_LISTENERS = "@text/zwave.thingstate.serial_listeners";

    public final static String EVENT_INCLUSION_STARTED = "@text/zwave.event.inclusion_started";
    public final static String EVENT_INCLUSION_COMPLETED = "@text/zwave.event.inclusion_completed";
    public final static String EVENT_INCLUSION_FAILED = "@text/zwave.event.inclusion_failed";
    public final static String EVENT_INCLUSION_SECURECOMPLETED = "@text/zwave.event.inclusion_securecompleted";
    public final static String EVENT_INCLUSION_SECUREFAILED = "@text/zwave.event.inclusion_securefailed";

    public final static String EVENT_EXCLUSION_STARTED = "@text/zwave.event.exclusion_started";
    public final static String EVENT_EXCLUSION_COMPLETED = "@text/zwave.event.exclusion_completed";
    public final static String EVENT_EXCLUSION_FAILED = "@text/zwave.event.exclusion_failed";
    public final static String EVENT_EXCLUSION_NODEREMOVED = "z@text/wave.event.exclusion_failed";

    public final static String EVENT_NETWORKUPDATE_DONE = "@text/zwave.event.networkupdate_done";
    public final static String EVENT_NETWORKUPDATE_ABORT = "@text/zwave.event.networkupdate_abort";
    public final static String EVENT_NETWORKUPDATE_WAIT = "@text/zwave.event.networkupdate_wait";
    public final static String EVENT_NETWORKUPDATE_DISABLED = "@text/zwave.event.networkupdate_disabled";
    public final static String EVENT_NETWORKUPDATE_OVERFLOW = "@text/zwave.event.networkupdate_overflow";

    public final static String EVENT_REMOVEFAILED_NOTFOUND = "@text/zwave.event.removenode_notfound";
    public final static String EVENT_REMOVEFAILED_NOTCTLR = "@text/zwave.event.removenode_nocontroller";
    public final static String EVENT_REMOVEFAILED_NOTREMOVED = "@text/zwave.event.removenode_notremoved";
    public final static String EVENT_REMOVEFAILED_NOCALLBACK = "@text/zwave.event.removenode_nocallback";
    public final static String EVENT_REMOVEFAILED_NODEOK = "@text/zwave.event.removenode_nodeok";
    public final static String EVENT_REMOVEFAILED_REMOVED = "@text/zwave.event.removenode_removed";
    public final static String EVENT_REMOVEFAILED_FAILED = "@text/zwave.event.removenode_failed";
    public final static String EVENT_REMOVEFAILED_BUSY = "@text/zwave.event.removenode_busy";
    public final static String EVENT_REMOVEFAILED_UNKNOWN = "@text/zwave.event.removenode_unknown";

    public final static String EVENT_HEAL_START = "@text/zwave.event.heal_start";
    public final static String EVENT_HEAL_DONE = "@text/zwave.event.heal_done";

    public final static String CONFIG_BINDING_POLLINGPERIOD_LABEL = "@text/zwave.config.binding_pollingperiod_label";
    public final static String CONFIG_BINDING_POLLINGPERIOD_DESC = "@text/zwave.config.binding_pollingperiod_desc";

    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = ImmutableSet.of(CONTROLLER_SERIAL);

    public static class I18nConstant {
        public String key;
        public String defaultText;

        I18nConstant(String key, String defaultText) {
            this.key = key;
            this.defaultText = defaultText;
        }
    }
}
