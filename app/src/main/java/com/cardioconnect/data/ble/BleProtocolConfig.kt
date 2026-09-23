package com.cardioconnect.data.ble

import java.util.UUID

/**
 * Configurable Bluetooth Low Energy protocol parameters.
 * Allows CardioConnect to interface with standard medical Bluetooth SIG profiles
 * as well as proprietary commercial or prototype ECG hardware.
 */
data class BleProtocolConfig(
    // ECG Stream Service (Standard Medical or Custom Vendor UUID)
    val ecgServiceUuid: UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB"), // Standard HR or Custom ECG Service
    val ecgDataCharacteristicUuid: UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB"), // ECG Waveform Stream Characteristic
    val heartRateCharacteristicUuid: UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB"), // Standard Heart Rate Measurement
    val deviceStatusCharacteristicUuid: UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB"), // Battery / Device Status
    val clientConfigDescriptorUuid: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"), // CCCD for notifications
    val sampleRateHz: Int = 250,
    val voltageResolutionMvPerLsb: Float = 0.001f // Conversion factor from raw ADC to mV
) {
    companion object {
        val DEFAULT = BleProtocolConfig()

        // Common alternative vendor profiles
        val PROTOTYPE_12_LEAD = BleProtocolConfig(
            ecgServiceUuid = UUID.fromString("E95D0753-251D-470A-A062-FA1922DFA9A8"),
            ecgDataCharacteristicUuid = UUID.fromString("E95D2114-251D-470A-A062-FA1922DFA9A8"),
            sampleRateHz = 500,
            voltageResolutionMvPerLsb = 0.0005f
        )
    }
}
