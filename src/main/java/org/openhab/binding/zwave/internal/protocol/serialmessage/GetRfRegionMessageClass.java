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
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Requests the RF region from a Z-Wave controller.
 * Only supported by 700 and 800 series Z-Wave controllers.
 * 500 series controllers do not support this command (UNKNOWN region will be returned).
 * 
 * @author Bob Eckhoff - Initial contribution
 */
public class GetRfRegionMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(GetRfRegionMessageClass.class);

    public ZWaveSerialPayload doRequest() {
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.GetRfRegion).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        if (incomingMessage.getMessagePayload().length == 0) {
            logger.debug("RF region response was empty; leaving region unchanged");
            transaction.setTransactionComplete();
            return true;
        }

        int regionCode = incomingMessage.getMessagePayloadByte(0) & 0xFF;
        ZWaveRegion region = ZWaveRegion.fromCode(regionCode);
        zController.setRfRegion(region);
        logger.debug("Got RF region response {} -> {}", regionCode, region);

        transaction.setTransactionComplete();
        return true;
    }

    /**
     * Enumerates the RF regions reported by supported Z-Wave controllers.
     */
    public enum ZWaveRegion {
        UNKNOWN(-1),
        EU(0x00),
        US(0x01),
        ANZ(0x02),
        HK(0x03),
        IN(0x04),
        IL(0x05),
        RU(0x06),
        CN(0x07),
        JP(0x08),
        KR(0x09),
        TW(0x0A),
        BR(0x0B);

        private final int code;

        ZWaveRegion(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public @Nullable String getLowerCaseName() {
            return switch (this) {
                case EU -> "europe";
                case US -> "usa";
                case ANZ -> "australia/new zealand";
                case HK -> "hong kong";
                case IN -> "india";
                case IL -> "israel";
                case RU -> "russia";
                case CN -> "china";
                case JP -> "japan";
                case KR -> "korea";
                case TW -> "taiwan";
                case BR -> "brazil";
                case UNKNOWN -> null;
                default -> name().toLowerCase();
            };
        }

        public static ZWaveRegion fromCode(int code) {
            for (ZWaveRegion region : values()) {
                if (region.code == code) {
                    return region;
                }
            }

            return UNKNOWN;
        }

        public static ZWaveRegion fromLocale(Locale locale) {
            String country = locale.getCountry();

            if ("US".equals(country) || "CA".equals(country) || "MX".equals(country)) {
                return US;
            }

            if ("AU".equals(country) || "NZ".equals(country)) {
                return ANZ;
            }

            if ("AT".equals(country) || "BE".equals(country) || "BG".equals(country) || "HR".equals(country)
                    || "CY".equals(country) || "CZ".equals(country) || "DK".equals(country)
                    || "EE".equals(country) || "FI".equals(country) || "FR".equals(country)
                    || "DE".equals(country) || "GR".equals(country) || "HU".equals(country)
                    || "IE".equals(country) || "IT".equals(country) || "LV".equals(country)
                    || "LT".equals(country) || "LU".equals(country) || "MT".equals(country)
                    || "NL".equals(country) || "PL".equals(country) || "PT".equals(country)
                    || "RO".equals(country) || "SK".equals(country) || "SI".equals(country)
                    || "ES".equals(country) || "SE".equals(country) || "GB".equals(country)) {
                return EU;
            }

            return UNKNOWN;
        }
    }
}
