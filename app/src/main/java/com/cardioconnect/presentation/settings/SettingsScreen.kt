package com.cardioconnect.presentation.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.domain.model.AppThemeMode
import com.cardioconnect.domain.model.EcgGain
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSweepSpeed
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.EmergencyRed
import com.cardioconnect.ui.theme.MedicalAmber
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    var showLeadMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Application & Device Preferences",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Section: DEMO ECG SIMULATOR
        SettingsSection(title = "DEMO SIMULATOR") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (settings.isDemoMode) MedicalAmber.copy(alpha = 0.08f) else CardDark),
                border = BorderStroke(1.dp, if (settings.isDemoMode) MedicalAmber.copy(alpha = 0.4f) else CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Synthetic Demo Mode",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (settings.isDemoMode) "DEMO DATA — NOT REAL ECG" else "Connect real ECG hardware via Bluetooth",
                                color = if (settings.isDemoMode) MedicalAmber else TextTertiary,
                                fontSize = 11.5.sp,
                                fontWeight = if (settings.isDemoMode) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Switch(
                            checked = settings.isDemoMode,
                            onCheckedChange = { viewModel.updateDemoMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = MedicalAmber
                            )
                        )
                    }
                }
            }
        }

        // Section: ECG Display & Calibration
        SettingsSection(title = "ECG CALIBRATION") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Lead Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Primary Lead", color = TextPrimary, fontSize = 13.5.sp)
                        Box {
                            TextButton(
                                onClick = { showLeadMenu = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.background(BackgroundDark, RoundedCornerShape(8.dp))
                            ) {
                                Text(settings.selectedLead.displayName, color = PhosphorGreen, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(expanded = showLeadMenu, onDismissRequest = { showLeadMenu = false }) {
                                EcgLead.entries.forEach { lead ->
                                    DropdownMenuItem(
                                        text = { Text(lead.displayName) },
                                        onClick = {
                                            viewModel.updateLead(lead)
                                            showLeadMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Sweep Speed
                    Column {
                        Text(text = "Sweep Speed", color = TextPrimary, fontSize = 13.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EcgSweepSpeed.entries.forEach { speed ->
                                val isSelected = settings.sweepSpeed == speed
                                Button(
                                    onClick = { viewModel.updateSweepSpeed(speed) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) PhosphorGreen else BackgroundDark
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = speed.label,
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Gain
                    Column {
                        Text(text = "Gain / Amplitude Sensitivity", color = TextPrimary, fontSize = 13.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EcgGain.entries.forEach { gain ->
                                val isSelected = settings.gain == gain
                                Button(
                                    onClick = { viewModel.updateGain(gain) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) PhosphorGreen else BackgroundDark
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = gain.label,
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Grid Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Medical Millimeter Grid", color = TextPrimary, fontSize = 13.5.sp)
                            Text(text = "1mm & 5mm calibrated grid lines", color = TextTertiary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = settings.isGridVisible,
                            onCheckedChange = { viewModel.updateGridVisible(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = PhosphorGreen
                            )
                        )
                    }

                    // Waveform Thickness
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Waveform Trace Thickness", color = TextPrimary, fontSize = 13.5.sp)
                            Text(text = "${settings.waveformThickness} px", color = PhosphorGreen, fontSize = 12.sp)
                        }
                        Slider(
                            value = settings.waveformThickness,
                            onValueChange = { viewModel.updateWaveformThickness(it) },
                            valueRange = 1.5f..5.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = PhosphorGreen,
                                activeTrackColor = PhosphorGreen
                            )
                        )
                    }
                }
            }
        }

        // Section: Device & Reconnection
        SettingsSection(title = "DEVICE & CONNECTIVITY") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Automatic Reconnection", color = TextPrimary, fontSize = 13.5.sp)
                            Text(text = "Automatically retry connection upon signal drop", color = TextTertiary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = settings.autoReconnect,
                            onCheckedChange = { viewModel.updateAutoReconnect(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PhosphorGreen)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Connection Drop Alerts", color = TextPrimary, fontSize = 13.5.sp)
                            Text(text = "Prompt audio/visual warning when sensor disconnects", color = TextTertiary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = settings.connectionAlertsEnabled,
                            onCheckedChange = { viewModel.updateConnectionAlerts(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PhosphorGreen)
                        )
                    }
                }
            }
        }

        // Section: Data & Storage
        SettingsSection(title = "DATA & PRIVACY") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Local Storage & HIPAA Privacy", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "All ECG telemetry data is stored securely in encrypted local app storage. No health data is uploaded to remote servers without explicit consent.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Delete All Recorded Sessions", color = EmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: About & Medical Safety
        SettingsSection(title = "ABOUT & LEGAL") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "App Version", color = TextPrimary, fontSize = 13.sp)
                        Text(text = "1.0.0 (Build 2026)", color = TextSecondary, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Medical Disclaimer", color = TextPrimary, fontSize = 13.sp)
                        TextButton(onClick = { showDisclaimerDialog = true }) {
                            Text(text = "View Notice", color = PhosphorGreen, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Delete All Sessions?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all saved ECG sessions from local storage. This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllSessions()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MedicalAmber) },
            title = { Text(text = "Medical Safety Disclaimer", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "CardioConnect is an informational ECG monitoring application and data review tool.\n\n" +
                            "It is NOT a medical device, nor does it provide medical diagnosis, treatment advice, or automated clinical decisions.\n\n" +
                            "In an emergency or if you experience chest pain, shortness of breath, or cardiac symptoms, seek immediate emergency medical care.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDisclaimerDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen)
                ) {
                    Text("I Understand", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDark
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        content()
    }
}
