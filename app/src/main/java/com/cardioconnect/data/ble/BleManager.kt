package com.cardioconnect.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cardioconnect.domain.model.BleDeviceConnectionState
import com.cardioconnect.domain.model.BleDeviceModel
import com.cardioconnect.domain.model.EcgSample
import com.cardioconnect.domain.model.EcgVitals
import com.cardioconnect.domain.model.SignalQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("MissingPermission")
class BleManager(
    private val context: Context,
    var protocolConfig: BleProtocolConfig = BleProtocolConfig.DEFAULT
) {
    private val tag = "CardioBleManager"
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDeviceModel>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDeviceModel>> = _discoveredDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BleDeviceModel?>(null)
    val connectedDevice: StateFlow<BleDeviceModel?> = _connectedDevice.asStateFlow()

    private val _ecgSamplesFlow = MutableSharedFlow<EcgSample>(replay = 0, extraBufferCapacity = 512)
    val ecgSamplesFlow: SharedFlow<EcgSample> = _ecgSamplesFlow.asSharedFlow()

    private val _vitalsFlow = MutableStateFlow(EcgVitals(isBleConnected = false))
    val vitalsFlow: StateFlow<EcgVitals> = _vitalsFlow.asStateFlow()

    private var activeGatt: BluetoothGatt? = null
    private var lastTargetAddress: String? = null
    private var isUserDisconnect = false
    private var autoReconnect = true

    private var parser: EcgPacketParser = DefaultEcgPacketParser(protocolConfig)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = try { device.name } catch (_: Exception) { null } ?: "ECG Device"
            val rssi = result.rssi
            val address = device.address

            val currentList = _discoveredDevices.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == address }

            val updatedModel = BleDeviceModel(
                name = name,
                address = address,
                rssi = rssi,
                connectionState = if (_connectedDevice.value?.address == address) {
                    _connectedDevice.value?.connectionState ?: BleDeviceConnectionState.DISCONNECTED
                } else BleDeviceConnectionState.DISCONNECTED,
                lastSeenTimestampMs = System.currentTimeMillis()
            )

            if (existingIndex >= 0) {
                currentList[existingIndex] = updatedModel
            } else {
                currentList.add(updatedModel)
            }
            _discoveredDevices.value = currentList
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "Scan failed with error: $errorCode")
            _isScanning.value = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            val name = try { gatt.device.name } catch (_: Exception) { null } ?: "ECG Device"

            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(tag, "Connected to GATT server: $address")
                _connectedDevice.value = BleDeviceModel(
                    name = name,
                    address = address,
                    rssi = -60,
                    connectionState = BleDeviceConnectionState.CONNECTED
                )
                _vitalsFlow.value = _vitalsFlow.value.copy(
                    isBleConnected = true,
                    connectedDeviceName = name
                )

                // Request high MTU for bulk ECG streaming
                try {
                    gatt.requestMtu(512)
                } catch (e: Exception) {
                    Log.w(tag, "Failed requesting MTU: ${e.message}")
                    gatt.discoverServices()
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(tag, "Disconnected from GATT server: $address, status: $status")
                _connectedDevice.value = null
                _vitalsFlow.value = _vitalsFlow.value.copy(
                    isBleConnected = false,
                    signalQuality = SignalQuality.NO_SIGNAL
                )
                try {
                    gatt.close()
                } catch (_: Exception) {}
                activeGatt = null

                if (!isUserDisconnect && autoReconnect && lastTargetAddress != null) {
                    scope.launch {
                        delay(2500)
                        connect(lastTargetAddress!!)
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(tag, "MTU changed to: $mtu, discovering services...")
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(tag, "Discovered services, searching for ECG characteristics...")
                subscribeToEcgNotifications(gatt)
            } else {
                Log.w(tag, "Service discovery failed with status $status")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val bytes = characteristic.value ?: return
            handleIncomingBytes(bytes, characteristic.uuid)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleIncomingBytes(value, characteristic.uuid)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectedDevice.value = _connectedDevice.value?.copy(rssi = rssi)
                _vitalsFlow.value = _vitalsFlow.value.copy(rssiDbm = rssi)
            }
        }
    }

    private fun handleIncomingBytes(bytes: ByteArray, uuid: UUID) {
        val now = System.currentTimeMillis()
        val samples = parser.parseWaveformSamples(bytes, now)
        samples.forEach { sample ->
            _ecgSamplesFlow.tryEmit(sample)
        }

        val vitals = parser.parseVitalsPayload(bytes)
        if (vitals != null) {
            val current = _vitalsFlow.value
            _vitalsFlow.value = current.copy(
                heartRate = vitals.heartRate ?: current.heartRate,
                spo2Percentage = vitals.spo2 ?: current.spo2Percentage,
                batteryLevelPercentage = vitals.batteryPercent ?: current.batteryLevelPercentage,
                rhythmClassification = vitals.deviceRhythmStatus ?: current.rhythmClassification,
                signalQuality = parser.parseSignalQuality(bytes)
            )
        }
    }

    private fun subscribeToEcgNotifications(gatt: BluetoothGatt) {
        // Look for service matching config or any heart rate / ecg characteristic
        var targetChar: BluetoothGattCharacteristic? = null

        val ecgService = gatt.getService(protocolConfig.ecgServiceUuid)
        if (ecgService != null) {
            targetChar = ecgService.getCharacteristic(protocolConfig.ecgDataCharacteristicUuid)
                ?: ecgService.characteristics.firstOrNull()
        }

        // If not found, look across all services for notify-enabled characteristic
        if (targetChar == null) {
            for (service in gatt.services) {
                for (characteristic in service.characteristics) {
                    val props = characteristic.properties
                    if ((props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 ||
                        (props and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                    ) {
                        targetChar = characteristic
                        break
                    }
                }
                if (targetChar != null) break
            }
        }

        targetChar?.let { char ->
            Log.d(tag, "Enabling notifications for characteristic: ${char.uuid}")
            gatt.setCharacteristicNotification(char, true)
            val descriptor = char.getDescriptor(protocolConfig.clientConfigDescriptorUuid)
                ?: char.descriptors.firstOrNull()
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    fun startScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            Log.w(tag, "Bluetooth LE Scanner not available")
            return
        }
        _isScanning.value = true
        _discoveredDevices.value = emptyList()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
            // Auto stop scan after 15 seconds to save power
            Handler(Looper.getMainLooper()).postDelayed({
                stopScan()
            }, 15000)
        } catch (e: Exception) {
            Log.e(tag, "Error starting scan: ${e.message}")
            _isScanning.value = false
        }
    }

    fun stopScan() {
        if (!_isScanning.value) return
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(tag, "Error stopping scan: ${e.message}")
        }
        _isScanning.value = false
    }

    fun connect(address: String) {
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return
        lastTargetAddress = address
        isUserDisconnect = false

        stopScan()
        disconnect()

        _connectedDevice.value = BleDeviceModel(
            name = try { device.name } catch (_: Exception) { null } ?: "Connecting...",
            address = address,
            rssi = -70,
            connectionState = BleDeviceConnectionState.CONNECTING
        )

        activeGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        isUserDisconnect = true
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (e: Exception) {
            Log.w(tag, "Error disconnecting: ${e.message}")
        }
        activeGatt = null
        _connectedDevice.value = null
        _vitalsFlow.value = _vitalsFlow.value.copy(isBleConnected = false)
    }

    fun reconnect() {
        lastTargetAddress?.let { address ->
            connect(address)
        }
    }

    fun updateProtocolConfig(newConfig: BleProtocolConfig) {
        protocolConfig = newConfig
        parser = DefaultEcgPacketParser(newConfig)
    }
}
