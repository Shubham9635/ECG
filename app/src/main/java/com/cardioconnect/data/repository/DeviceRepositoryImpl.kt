package com.cardioconnect.data.repository

import com.cardioconnect.data.ble.BleManager
import com.cardioconnect.domain.model.BleDeviceModel
import com.cardioconnect.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.StateFlow

class DeviceRepositoryImpl(
    private val bleManager: BleManager
) : DeviceRepository {

    override val isBluetoothEnabled: StateFlow<Boolean> = bleManager.isBluetoothEnabled
    override val isScanning: StateFlow<Boolean> = bleManager.isScanning
    override val discoveredDevices: StateFlow<List<BleDeviceModel>> = bleManager.discoveredDevices
    override val connectedDevice: StateFlow<BleDeviceModel?> = bleManager.connectedDevice

    override fun startScan() {
        bleManager.startScan()
    }

    override fun stopScan() {
        bleManager.stopScan()
    }

    override fun connectDevice(address: String) {
        bleManager.connect(address)
    }

    override fun disconnectDevice() {
        bleManager.disconnect()
    }

    override fun reconnectDevice() {
        bleManager.reconnect()
    }
}
