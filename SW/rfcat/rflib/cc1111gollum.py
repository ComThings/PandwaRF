#!/usr/bin/env ipython
import re
import sys
import usb
import code
import time
import struct
import pickle
import threading
import binascii
# from chipcondefs import *
from cc1111client import *


APP_SYSTEM_GOLLUM = 0xF1
APP_NIC = 0x42
APP_SPECAN = 0x43
APP_RF = 0xBF

# APP_SYSTEM_GOLLUM
SYS_CMD_PM_SLEEP = 0x8A
SYS_CMD_GET_FW_VERSION = 0x90

# APP_NIC
SYS_CMD_NIC_START_DATARATE_DETECTION = 0x0A
SYS_CMD_NIC_STOP_DATARATE_DETECTION = 0x0B
SYS_CMD_NIC_DATARATE_DETECTED = 0x0C
SYS_CMD_NIC_DATARATE_DETECTED_END = 0x0D

# APP_RF
SYS_CMD_RF_START_JAMMING = 0x62
SYS_CMD_RF_STOP_JAMMING = 0x63
SYS_CMD_RF_BRUTE_FORCE_START = 0x64
SYS_CMD_RF_BRUTE_FORCE_STOP = 0x65
SYS_CMD_RF_BRUTE_FORCE_STATUS_UPDATE = 0x66
SYS_CMD_RF_BRUTE_FORCE_SETUP_FUNCTION = 0x7A
SYS_CMD_RF_BRUTE_FORCE_SETUP = 0x68                 # Gouv custom
SYS_CMD_RF_BRUTE_FORCE_START_SYNC_CODE_TAIL = 0x69  # Gouv custom
SYS_CMD_RF_TXRX_POWER_AMP_ACTION = 0x67
SYS_CMD_RF_GET_TXRX_POWER_AMP_MODE = 0x70

# RF power amplifiers
RF_POWER_AMPLIFIERS_ACTION_ALL_OFF = 0x00         # action turn off amplifiers
RF_TX_POWER_AMPLIFIER_ACTION_ON = 0x01            # action turn on TX amplifier
RF_RX_POWER_AMPLIFIER_ACTION_ON = 0x02            # action turn on RX amplifier
RF_TX_RX_POWER_AMPLIFIER_ACTION_ON = 0x05         # action turn on TX & RX amplifiers (not supported by rev. E)
RF_TX_POWER_AMPLIFIER_ACTION_ON_TX = 0x06         # action turn on TX amplifier only when transmitting
RF_RX_POWER_AMPLIFIER_ACTION_ON_RX = 0x07         # action turn on RX amplifier only when receiving
RF_TX_RX_POWER_AMPLIFIER_ACTION_ON_TX_RX = 0x08   # action turn on TX & RX amplifiers only when transmitting & receiving
RF_ANT_POWER_ENABLE = 0x03                        # action enable antenna power
RF_ANT_POWER_DISABLE = 0x04                       # action disable antenna power

# Data rate measurement
USB_DATARATE_MEAS_WAIT_MS = 100
DATARATE_MEAS_OCC_THRESHOLD_DEFAULT = 100

# Brute Force
USB_BRUTEFORCE_STATUS_WAIT_MS = 5000


# PandwaRF/Gollum specific methods
class GollumDongle(USBDongle):

    def __init__(self, idx=0, debug=False, copyDongle=None, RfMode=RFST_SRX):
        USBDongle.__init__(self, idx, debug, copyDongle, RfMode)
        self.endec = None
        self.gollumRfConfigure()

    def gollumRfConfigure(self):
        '''
        makes special RF configuration for PandwaRF
        '''
        self.setAmpMode(RF_POWER_AMPLIFIERS_ACTION_ALL_OFF)

    def getFwVersion(self):
        '''
        requests the CC1111 FW version
        '''
        r, t = self.send(APP_SYSTEM_GOLLUM, SYS_CMD_GET_FW_VERSION, '')
        return r

    def setPmSleep(self):
        '''
        sets the CC1111 into specified sleep mode (0 to 3)
        '''
        r, t = self.send(APP_SYSTEM_GOLLUM, SYS_CMD_PM_SLEEP, '')

    def sendDataRateDetectionStart(self, occurence_threshold=DATARATE_MEAS_OCC_THRESHOLD_DEFAULT):
        '''
        requests the CC1111 to perform a data rate measurement
        occurence_threshold: number of occurence needed for a duration to be elected as the winner
        '''
        r, t = self.send(APP_NIC, SYS_CMD_NIC_START_DATARATE_DETECTION, struct.pack("B", occurence_threshold))

    def sendDataRateDetectionStop(self):
        '''
        requests the CC1111 to perform a data rate measurement
        '''
        r, t = self.send(APP_NIC, SYS_CMD_NIC_STOP_DATARATE_DETECTION, '')

    def recvDataRateReceive(self, timeout=USB_DATARATE_MEAS_WAIT_MS, blocksize=None):
        '''
        extract a data rate measurement message
        '''

        data = self.recv(APP_NIC, SYS_CMD_NIC_DATARATE_DETECTED, timeout)
        # decode, if necessary
        if self.endec is not None:
            data = self.endec.decode(data)
        return data

    def recvDataRateReceiveEnd(self, timeout=USB_DATARATE_MEAS_WAIT_MS, blocksize=None):
        '''
        extract a data rate measurement end message
        '''
        data = self.recv(APP_NIC, SYS_CMD_NIC_DATARATE_DETECTED_END, timeout)
        # decode, if necessary
        if self.endec is not None:
            data = self.endec.decode(data)
        return data

    def doDataRateDetect(self, occurence_threshold=DATARATE_MEAS_OCC_THRESHOLD_DEFAULT):
        '''
        starts the Data rate measurement procedure. Frequency needs to be setup first.
        '''
        print "Entering data rate measurement mode...  measured data rates arriving will be displayed on the screen"
        print "(press Enter to stop)"

        self.setFreq(433880000)
        self.setMdmModulation(MOD_ASK_OOK)
        self.setMdmDRate(100000)
        self.setMdmSyncMode(SYNCM_CARRIER)
        self.setPktPQT(0)
        self.makePktFLEN(250)
        self.setEnablePktDataWhitening(0)
        self.setEnableMdmFEC(0)
        self.setEnablePktCRC(0)
        self.setMdmChanBW(125000)
        self.setAmpMode(RF_POWER_AMPLIFIERS_ACTION_ALL_OFF)
        self.sendDataRateDetectionStart(occurence_threshold)

        while not keystop():
            # check for SYS_CMD_NIC_DATARATE_DETECTED
            try:
                (y, t) = self.recvDataRateReceive()
                dr, = struct.unpack("<L", y)
                print "(%5.3f) Data rate received: %d bits/s" % (t, dr)

            except ChipconUsbTimeoutException:
                # print "Timeout dataRateReceive"
                pass
            except KeyboardInterrupt:
                print "Please press <enter> to stop"

            # check for SYS_CMD_NIC_DATARATE_DETECTED_END
            try:
                y, t = self.recvDataRateReceiveEnd()
                print "(%5.3f) Data rate measurement ended" % t
                break

            except ChipconUsbTimeoutException:
                # print "Timeout dataRateReceiveEnd"
                pass
            except KeyboardInterrupt:
                print "Please press <enter> to stop"

        sys.stdin.read(1)  # remove extra input char

        self.sendDataRateDetectionStop()

    def setAmpMode(self, ampmode=0):
        '''
        set the amplifier mode (RF amp external to CC1111)
        0x00    turn off amplifiers
        0x01    turn on TX amplifier only
        0x02    turn on RX amplifier only
        0x05    turn on TX & RX amplifiers (not supported by rev. E)
        0x06    turn on TX amplifier only when transmitting
        0x07    turn on RX amplifier only when receiving
        0x08    turn on TX & RX amplifiers only when transmitting & receiving
        0x03    enable antenna power
        0x04    disable antenna power
        '''
        self.send(APP_RF, SYS_CMD_RF_TXRX_POWER_AMP_ACTION, "%c" % ampmode)

    def getAmpMode(self):
        '''
        get the amplifier mode (RF amp external to CC1111)
        '''
        return self.send(APP_RF, SYS_CMD_RF_GET_TXRX_POWER_AMP_MODE, "")

    def sendJammingStart(self, freq=433920000, dataRate=10000, modulation=MODULATION_ASK_OOK):
        '''
        requests the CC1111 to perform a RF jamming
        '''
        r, t = self.send(APP_RF, SYS_CMD_RF_START_JAMMING, struct.pack("<IIB", freq, dataRate, modulation))

    def sendJammingStop(self):
        '''
        requests the CC1111 to stop RF jamming
        '''
        r, t = self.send(APP_RF, SYS_CMD_RF_STOP_JAMMING, "")

    def doJamming(self, freq=433920000, dataRate=10000, modulation=MODULATION_ASK_OOK):
        '''
        starts RF jamming
        '''
        print "Entering RF jamming mode..."

        self.sendJammingStart(freq, dataRate, modulation)

        raw_input("press Enter to stop")

        self.sendJammingStop()

    def sendBruteForceStart(self, freq=433920000, dataRate=10000, modulation=MODULATION_ASK_OOK, codeLength=8, startValue=0, stopValue=255, repeat=1, littleEndian=True, delayMs=100,
                            encSymbolZero='0x8E', encSymbolOne='0xEE', encSymbolTwo='0x00', encSymbolThree='0x00', syncWord='8000'):
        '''
        requests the CC1111 to perform a RF Brute force attack
        Legacy Brute force for public version.
        Deprecated in favor of CMD_RF_BRUTE_FORCE_SETUP_ATTACK (+ CMD_RF_BRUTE_FORCE_SETUP_FUNCTION) + CMD_RF_BRUTE_FORCE_START_SYNC_CODE_TAIL.
        Includes Setup + Start in the same message.
        Parameters:
            freq in Hz (4 bytes),
            dataRate in baud (4 bytes),
            modulation (1 byte),
            codeLength (number of bits in the code) (1 byte). max 16bits
            startValue (2 bytes),
            stopValue (2 bytes),
            repeat frame (1 byte)
            little endian (true = LE, false = BE) (1 byte)
            delay between attempts (1 byte)
            encoder_symbol_0 (1 byte),
            encoder_symbol_1 (1 byte),
            encoder_symbol_2 (1 byte),
            encoder_symbol_3 (1 byte),
            syncWord (syncWordSize bytes)
        Usage:
            (CMD_RF_BRUTE_FORCE_SETUP_FUNCTION)
            CMD_RF_BRUTE_FORCE_START

            CMD_RF_BRUTE_FORCE_STATUS_UPDATE
            CMD_RF_BRUTE_FORCE_STATUS_UPDATE
            CMD_RF_BRUTE_FORCE_STATUS_UPDATE
            ...
            CMD_RF_BRUTE_FORCE_STOP_ATTACK
        '''

        msg_1 = struct.pack("<IIB", freq, dataRate, modulation)
        msg_2 = struct.pack("<BHHBBB", codeLength, startValue, stopValue, repeat, littleEndian, delayMs)
        msg_3 = struct.pack("BBBB", int(encSymbolZero, 16), int(encSymbolOne, 16), int(encSymbolTwo, 16), int(encSymbolThree, 16))
        msg_4 = struct.pack("B", len(binascii.unhexlify(syncWord))) + binascii.unhexlify(syncWord)

        r, t = self.send(APP_RF, SYS_CMD_RF_BRUTE_FORCE_START, msg_1 + msg_2 + msg_3 + msg_4)

    def sendBruteForceSetup(self, freq=433920000, dataRate=10000, modulation=MODULATION_ASK_OOK, delayMs=100, encSymbolZero='0x8E', encSymbolOne='0xEE', encSymbolTwo='0x00', encSymbolThree='0x00'):
        '''
        Brute Force setup. Must be the first message of the BF because it resets CC1111.
        Parameters:
            freq in Hz (4 bytes),
            dataRate in baud (4 bytes),
            modulation (1 byte),
            delay between attempts (1 byte)
            encoder_symbol_0 (1 byte),
            encoder_symbol_1 (1 byte),
            encoder_symbol_2 (1 byte),
            encoder_symbol_3 (1 byte),
        '''

        msg_1 = struct.pack("<IIB", freq, dataRate, modulation)
        msg_2 = struct.pack("B", delayMs)
        msg_3 = struct.pack("BBBB", int(encSymbolZero, 16), int(encSymbolOne, 16), int(encSymbolTwo, 16), int(encSymbolThree, 16))

        r, t = self.send(APP_RF, SYS_CMD_RF_BRUTE_FORCE_SETUP, msg_1 + msg_2 + msg_3)

    def sendBruteForceSetupFunction(self, maskWord='FFFFFF0000FFFFFF', valueWord='000000E8E8000000'):
        '''
        Brute Force setup of the Function Mask and Value.
        Parameters:
            maskWord: function mask
            valueWord: function value
            maskWord and valueWord shall have the same size
        '''

        msg_1 = struct.pack("B", len(binascii.unhexlify(maskWord))) + binascii.unhexlify(maskWord) + binascii.unhexlify(valueWord)

        r, t = self.send(APP_RF, SYS_CMD_RF_BRUTE_FORCE_SETUP_FUNCTION, msg_1)

    def sendBruteForceStartSyncCodeTail(self, codeLength=8, startValue=0, stopValue=255, repeat=1, littleEndian=True, syncWord='8000', tailWord='00'):
        '''
        Starts a brute force after setup has been done. Warning: codeLength is max 32bits.
        Warning: Only available in PandwaRF Rogue CC1111 FW.
        Parameters:
            codeLength (number of bits in the code) (1 byte). max 32bits
            startValue (2 bytes),
            stopValue (2 bytes),
            repeat frame (1 byte)
            little endian (true = LE, false = BE) (1 byte)
            syncWord: synchro word, added at the beggining of each codeword
            tailWord: tail word, added at the end of each codeword
        Usage:
            CMD_RF_BRUTE_FORCE_SETUP
            (CMD_RF_BRUTE_FORCE_SETUP_FUNCTION)
            CMD_RF_BRUTE_FORCE_START_SYNC_CODE_TAIL

            CMD_RF_BRUTE_FORCE_STATUS_UPDATE
            CMD_RF_BRUTE_FORCE_STATUS_UPDATE
            CMD_RF_BRUTE_FORCE_STATUS_UPDATE
            ...
            CMD_RF_BRUTE_FORCE_STOP_ATTACK
        '''

        msg_1 = struct.pack("B", len(binascii.unhexlify(syncWord))) + struct.pack("B", len(binascii.unhexlify(tailWord)))
        msg_2 = struct.pack("<BHHBB", codeLength, startValue, stopValue, repeat, littleEndian)
        msg_3 = binascii.unhexlify(syncWord) + binascii.unhexlify(tailWord)

        r, t = self.send(APP_RF, SYS_CMD_RF_BRUTE_FORCE_START_SYNC_CODE_TAIL, msg_1 + msg_2 + msg_3)

    def sendBruteForceStop(self):
        '''
        requests the CC1111 to stop RF Brute force attack
        '''
        r, t = self.send(APP_RF, SYS_CMD_RF_BRUTE_FORCE_STOP, "")

    def recvBruteForeStatusUpdate(self, timeout=USB_BRUTEFORCE_STATUS_WAIT_MS, blocksize=None):
        '''
        extract a Brute force status update message
        '''
        data = self.recv(APP_RF, SYS_CMD_RF_BRUTE_FORCE_STATUS_UPDATE, timeout)
        # decode, if necessary
        if self.endec is not None:
            data = self.endec.decode(data)
        return data

    def doBruteForce(self, freq=433890000, dataRate=5320, modulation=MODULATION_ASK_OOK, codeLength=8, startValue=0, stopValue=6560, repeat=10, littleEndian=False, delayMs=100,
                     encSymbolZero='0x8E', encSymbolOne='0xEE', encSymbolTwo='0xE8', encSymbolThree='0x00', syncWord='0008', maskWord='FFFFFF0000FFFFFF', valueWord='000000EEE8000000'):
        '''
        starts the brute force procedure
        '''
        print "Entering brute force mode...  status arriving will be displayed on the screen"
        print "(press Enter to stop)"

        self.sendBruteForceSetupFunction(maskWord, valueWord)
        self.sendBruteForceStart(freq, dataRate, modulation, codeLength, startValue, stopValue, repeat, littleEndian,
                                 delayMs, encSymbolZero, encSymbolOne, encSymbolTwo, encSymbolThree, syncWord)

        status = 0
        while (not keystop() and (status <= stopValue)):
            # check for SYS_CMD_RF_BRUTE_FORCE_STATUS_UPDATE
            try:
                (y, t) = self.recvBruteForeStatusUpdate()
                status, = struct.unpack("<L", y)
                # output to the same line overwriting previous output: '\r' used with ',' at the end
                print "(%5.3f) Brute force status: %d/%d\r" % (t, status, stopValue),
                sys.stdout.flush()

            except ChipconUsbTimeoutException:
                # print "Timeout Brute force status update"
                pass
            except KeyboardInterrupt:
                print "Please press <enter> to stop"

        sys.stdin.read(1)  # remove extra input char

        self.sendBruteForceStop()
