package com.cardioconnect.domain.repository

import com.cardioconnect.domain.model.BleDeviceModel
import kotlinx.coroutines.flow.StateFlow

interface DeviceRepository {
    val isBluetoothEnabled: StateFlow<Boolean>
    val isScanning: StateFlow<Boolean>
    val discoveredDevices: StateFlow<List<BleDeviceModel>>
    val connectedDevice: StateFlow<BleDeviceModel?>

    fun startScan()
    fun stopScan()
    fun connectDevice(address: String)
    fun disconnectDevice()
    fun reconnectDevice()
}
