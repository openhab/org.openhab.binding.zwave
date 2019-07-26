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
package org.openhab.binding.zwave.handler;

import static org.openhab.binding.zwave.ZWaveBindingConstants.*;

import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.scheduler.CronScheduler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveSerialHandler} is responsible for the serial communications to the ZWave stick.
 * <p>
 * The {@link ZWaveSerialHandler} is a SmartHome bridge. It handles the serial communications, and provides a number of
 * channels that feed back serial communications statistics.
 *
 * @author Chris Jackson - Initial contribution
 * @author Hilbrand Bouwkamp - Added scheduled watchdog for serial port initialization to better handle problems
 */
@NonNullByDefault
public class ZWaveSerialHandler extends ZWaveControllerHandler {

    private static final int SERIAL_RECEIVE_TIMEOUT = 250;
    private static final long WATCHDOG_INIT_SECONDS = 10;
    private static final long WATCHDOG_CHECK_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(ZWaveSerialHandler.class);

    private final SerialPortManager serialPortManager;

    private String portId = "";

    private @Nullable SerialPort serialPort;

    private int SOFCount = 0;
    private int CANCount = 0;
    private int NAKCount = 0;
    private int ACKCount = 0;
    private int OOFCount = 0;
    private int CSECount = 0;

    private @Nullable ZWaveReceiveThread receiveThread;

    private @NonNullByDefault({}) ScheduledFuture<?> watchdog;

    public ZWaveSerialHandler(Bridge thing, SerialPortManager serialPortManager, CronScheduler cronScheduler) {
        super(thing, cronScheduler);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZWave serial controller.");

        portId = (String) getConfig().get(CONFIGURATION_PORT);

        if (portId == null || portId.isEmpty()) {
            logger.warn("ZWave port is not set.");
            return;
        }

        super.initialize();
        watchdog = scheduler.scheduleWithFixedDelay(this::watchSerialPort, WATCHDOG_INIT_SECONDS,
                WATCHDOG_CHECK_SECONDS, TimeUnit.SECONDS);
        initializeController();
    }

    private void watchSerialPort() {
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(portId);
            if (portIdentifier == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        ZWaveBindingConstants.OFFLINE_SERIAL_EXISTS + ' ' + portId);
                return;
            }
            if (serialPort != null) {
                if (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    return;
                } else {
                    serialPort.close();
                }
            }
            disposeReceiveThread();
            logger.info("Connecting to serial port '{}'", portId);
            SerialPort commPort = portIdentifier.open("org.openhab.binding.zwave", 2000);
            serialPort = commPort;
            commPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            commPort.enableReceiveThreshold(1);
            commPort.enableReceiveTimeout(SERIAL_RECEIVE_TIMEOUT);
            logger.debug("Starting receive thread");
            ZWaveReceiveThread zWaveReceiveThread = new ZWaveReceiveThread();
            receiveThread = zWaveReceiveThread;
            zWaveReceiveThread.start();

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            commPort.addEventListener(zWaveReceiveThread);
            commPort.notifyOnDataAvailable(true);
            logger.info("Serial port is initialized");

            initializeNetwork();
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ZWaveBindingConstants.OFFLINE_SERIAL_INUSE + ' ' + portId);
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ZWaveBindingConstants.OFFLINE_SERIAL_UNSUPPORTED + ' ' + portId);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ZWaveBindingConstants.OFFLINE_SERIAL_LISTENERS + ' ' + portId);
        } catch (RuntimeException e) {
            logger.debug("Unexpected runtime exception during serial port initialized ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE);
        }
    }

    /**
     * Closes the connection to the ZWave controller.
     */
    @Override
    public void dispose() {
        disposeReceiveThread();
        if (watchdog != null && !watchdog.isCancelled()) {
            watchdog.cancel(true);
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        logger.info("Stopped ZWave serial handler");

        super.dispose();
    }

    private synchronized void disposeReceiveThread() {
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join();
            } catch (InterruptedException e) {
            }
            receiveThread = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * ZWave controller Receive Thread. Takes care of receiving all messages.
     * It uses a semaphore to synchronize communication with the sending thread.
     */
    private class ZWaveReceiveThread extends Thread implements SerialPortEventListener {

        private static final int SOF = 0x01;
        private static final int ACK = 0x06;
        private static final int NAK = 0x15;
        private static final int CAN = 0x18;

        private final Logger logger = LoggerFactory.getLogger(ZWaveReceiveThread.class);

        private final int SEARCH_SOF = 0;
        private final int SEARCH_LEN = 1;
        private final int SEARCH_DAT = 2;

        private int rxState = SEARCH_SOF;
        private int messageLength;
        private int rxLength;
        private byte @Nullable [] rxBuffer;

        ZWaveReceiveThread() {
            super("ZWaveReceiveInputThread");
        }

        @Override
        public void serialEvent(SerialPortEvent arg0) {
            try {
                logger.trace("RXTX library CPU load workaround, sleep forever");
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
            }
        }

        /**
         * Sends 1 byte frame response.
         *
         * @param response
         *            the response code to send.
         */
        private void sendResponse(int response) {
            try {
                if (serialPort == null) {
                    return;
                }
                synchronized (serialPort.getOutputStream()) {
                    serialPort.getOutputStream().write(response);
                    serialPort.getOutputStream().flush();
                    logger.trace("Response SENT {}", response);
                }
            } catch (IOException e) {
                logger.debug("Exception during send", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE);
            }
        }

        /**
         * Run method. Runs the actual receiving process.
         */
        @Override
        public void run() {
            logger.debug("Starting ZWave thread: Receive");
            try {
                // Initialise all the statistics channels
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_SOF), new DecimalType(SOFCount));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_ACK), new DecimalType(ACKCount));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_NAK), new DecimalType(NAKCount));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_CAN), new DecimalType(CANCount));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_OOF), new DecimalType(OOFCount));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_CSE), new DecimalType(CSECount));

                // Send a NAK to resynchronise communications
                sendResponse(NAK);

                while (!interrupted()) {
                    int nextByte;

                    try {
                        if (serialPort == null) {
                            break;
                        }
                        nextByte = serialPort.getInputStream().read();
                        // logger.debug("SERIAL:: STATE {}, nextByte {}, count {} ", rxState, nextByte, rxLength);

                        // If byte value is -1, this is a timeout
                        if (nextByte == -1) {
                            if (rxState != SEARCH_SOF) {
                                // If we're not searching for a new frame when we get a timeout, something bad happened
                                logger.debug("Receive Timeout - Sending NAK");
                                rxState = SEARCH_SOF;
                            }
                            continue;
                        }
                    } catch (IOException e) {
                        logger.debug("Got I/O exception {} during receiving. exiting thread.", e.getLocalizedMessage());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE);
                        break;
                    }

                    switch (rxState) {
                        case SEARCH_SOF:
                            switch (nextByte) {
                                case SOF:
                                    logger.trace("Received SOF");

                                    // Keep track of statistics
                                    SOFCount++;
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_SOF),
                                            new DecimalType(SOFCount));
                                    rxState = SEARCH_LEN;
                                    break;

                                case ACK:
                                    // Keep track of statistics
                                    ACKCount++;
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_ACK),
                                            new DecimalType(ACKCount));
                                    logger.debug("Receive Message = 06");
                                    SerialMessage ackMessage = new SerialMessage(new byte[] { ACK });
                                    incomingMessage(ackMessage);
                                    break;

                                case NAK:
                                    // A NAK means the CRC was incorrectly received by the controller
                                    NAKCount++;
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_NAK),
                                            new DecimalType(NAKCount));
                                    logger.debug("Receive Message = 15");
                                    SerialMessage nakMessage = new SerialMessage(new byte[] { NAK });
                                    incomingMessage(nakMessage);
                                    break;

                                case CAN:
                                    // The CAN means that the controller dropped the frame
                                    CANCount++;
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_CAN),
                                            new DecimalType(CANCount));
                                    // logger.debug("Protocol error (CAN)");
                                    logger.debug("Receive Message = 18");
                                    SerialMessage canMessage = new SerialMessage(new byte[] { CAN });
                                    incomingMessage(canMessage);
                                    break;

                                default:
                                    OOFCount++;
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_OOF),
                                            new DecimalType(OOFCount));
                                    logger.debug(String.format("Protocol error (OOF). Got 0x%02X.", nextByte));
                                    // Let the timeout deal with sending the NAK
                                    break;
                            }
                            break;

                        case SEARCH_LEN:
                            // Sanity check the frame length
                            if (nextByte < 4 || nextByte > 64) {
                                logger.debug("Frame length is out of limits ({})", nextByte);

                                break;
                            }
                            messageLength = (nextByte & 0xff) + 2;

                            rxBuffer = new byte[messageLength];
                            rxBuffer[0] = SOF;
                            rxBuffer[1] = (byte) nextByte;
                            rxLength = 2;
                            rxState = SEARCH_DAT;
                            break;

                        case SEARCH_DAT:
                            rxBuffer[rxLength] = (byte) nextByte;
                            rxLength++;

                            if (rxLength < messageLength) {
                                break;
                            }

                            logger.debug("Receive Message = {}", SerialMessage.bb2hex(rxBuffer));
                            SerialMessage recvMessage = new SerialMessage(rxBuffer);
                            if (recvMessage.isValid) {
                                logger.trace("Message is valid, sending ACK");
                                sendResponse(ACK);

                                incomingMessage(recvMessage);
                            } else {
                                CSECount++;
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_CSE),
                                        new DecimalType(CSECount));

                                logger.debug("Message is invalid, discarding");
                                sendResponse(NAK);
                            }

                            rxState = SEARCH_SOF;
                            break;
                    }

                }
            } catch (RuntimeException e) {
                logger.debug("Exception during ZWave thread. ", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE);
            } finally {
                logger.debug("Stopped ZWave thread: Receive");
                if (serialPort != null) {
                    serialPort.removeEventListener();
                }
            }
        }
    }

    @Override
    public void sendPacket(@Nullable SerialMessage serialMessage) {
        byte[] buffer = serialMessage.getMessageBuffer();
        if (serialPort == null) {
            logger.debug("NODE {}: Port closed sending REQUEST Message = {}", serialMessage.getMessageNode(),
                    SerialMessage.bb2hex(buffer));
            return;
        }

        logger.debug("NODE {}: Sending REQUEST Message = {}", serialMessage.getMessageNode(),
                SerialMessage.bb2hex(buffer));

        try {
            synchronized (serialPort.getOutputStream()) {
                serialPort.getOutputStream().write(buffer);
                serialPort.getOutputStream().flush();
                logger.debug("Message SENT");
            }
        } catch (IOException e) {
            logger.debug("Got I/O exception {} during sending. exiting thread.", e.getLocalizedMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE);
        }
    }
}
