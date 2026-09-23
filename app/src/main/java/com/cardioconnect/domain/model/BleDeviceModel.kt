package com.cardioconnect.domain.model

enum class BleDeviceConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

data class BleDeviceModel(
    val name: String?,
    val address: String,
    val rssi: Int,
    val connectionState: BleDeviceConnectionState = BleDeviceConnectionState.DISCONNECTED,
    val batteryPercentage: Int? = null,
    val lastSeenTimestampMs: Long = System.currentTimeMillis()
)
