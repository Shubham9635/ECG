package com.cardioconnect.presentation.device

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.domain.model.BleDeviceConnectionState
import com.cardioconnect.domain.model.BleDeviceModel
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.ElectricCyan
import com.cardioconnect.ui.theme.EmergencyRed
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary

@Composable
fun DeviceScreen(
    viewModel: MainViewModel
) {
    val isBtEnabled by viewModel.isBluetoothEnabled.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Connect ECG Device",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wireless Medical BLE Connectivity",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { showConfigDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Protocol Config",
                    tint = PhosphorGreen
                )
            }
        }

        // Bluetooth Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isBtEnabled) PhosphorGreen else EmergencyRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBtEnabled) "Bluetooth ON" else "Bluetooth OFF",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBtEnabled) "Ready to scan for medical sensors" else "Enable Bluetooth in device settings",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isScanning) viewModel.stopScan() else viewModel.startScan()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) EmergencyRed else PhosphorGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.BluetoothSearching, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan for Devices", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Currently Connected Device Card (if any)
        connectedDevice?.let { dev ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PhosphorGreen.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, PhosphorGreen.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BluetoothConnected, contentDescription = null, tint = PhosphorGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = dev.name ?: "Connected Device",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = dev.address, color = TextTertiary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Text(
                            text = "CONNECTED",
                            color = PhosphorGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.disconnectDevice() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Disconnect", color = EmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.reconnectDevice() },
                            colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CardBorderDark),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = PhosphorGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reconnect", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section Title
        Text(
            text = "DISCOVERED DEVICES",
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Discovered Devices List
        if (discoveredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SettingsInputAntenna,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isScanning) "Searching for nearby ECG hardware..." else "No devices discovered yet",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    if (!isScanning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Scan for Devices' to begin searching",
                            color = TextTertiary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(discoveredDevices, key = { it.address }) { device ->
                    DeviceItemCard(
                        device = device,
                        isConnected = connectedDevice?.address == device.address,
                        onConnectClick = { viewModel.connectDevice(device.address) },
                        onDisconnectClick = { viewModel.disconnectDevice() }
                    )
                }
            }
        }
    }

    if (showConfigDialog) {
        ProtocolConfigDialog(onDismiss = { showConfigDialog = false })
    }
}

@Composable
fun DeviceItemCard(
    device: BleDeviceModel,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown Sensor",
                    color = TextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.address,
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Signal: ${device.rssi} dBm",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    device.batteryPercentage?.let { bat ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.BatteryFull, contentDescription = null, tint = PhosphorGreen, modifier = Modifier.size(12.dp))
                        Text(text = " $bat%", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            if (isConnected) {
                Button(
                    onClick = onDisconnectClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text("Disconnect", color = EmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onConnectClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text("Connect", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProtocolConfigDialog(onDismiss: () -> Unit) {
    var serviceUuid by remember { mutableStateOf("0000180D-0000-1000-8000-00805F9B34FB") }
    var ecgCharUuid by remember { mutableStateOf("00002A37-0000-1000-8000-00805F9B34FB") }
    var sampleRate by remember { mutableStateOf("250") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "ECG Hardware Protocol", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure custom BLE Service and Characteristic UUIDs for your ECG machine.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = serviceUuid,
                    onValueChange = { serviceUuid = it },
                    label = { Text("Service UUID", fontSize = 11.sp) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = ecgCharUuid,
                    onValueChange = { ecgCharUuid = it },
                    label = { Text("ECG Data Characteristic UUID", fontSize = 11.sp) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = sampleRate,
                    onValueChange = { sampleRate = it },
                    label = { Text("Sample Rate (Hz)", fontSize = 11.sp) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen)
            ) {
                Text("Save Protocol", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CardDark
    )
}
