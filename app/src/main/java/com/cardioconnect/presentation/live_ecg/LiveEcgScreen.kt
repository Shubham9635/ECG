package com.cardioconnect.presentation.live_ecg

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.cardioconnect.domain.model.EcgGain
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSweepSpeed
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.ui.components.EcgCanvasGraph
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.EmergencyRed
import com.cardioconnect.ui.theme.MedicalAmber
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun LiveEcgScreen(
    viewModel: MainViewModel
) {
    val vitals by viewModel.liveVitals.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDurationSeconds.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    var showLeadMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showGainMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CardioConnect",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = if (vitals.isDemoMode) MedicalAmber else if (vitals.isBleConnected) PhosphorGreen else EmergencyRed
                    val statusText = if (vitals.isDemoMode) "DEMO DATA — NOT REAL ECG" else if (vitals.isBleConnected) "ECG Connected" else "Disconnected"

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Calibration Selectors (Lead, Speed, Gain)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Lead selector
                Box {
                    TextButton(
                        onClick = { showLeadMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.background(CardDark, RoundedCornerShape(8.dp))
                    ) {
                        Text(settings.selectedLead.displayName, color = PhosphorGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

                // Speed selector
                Box {
                    TextButton(
                        onClick = { showSpeedMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.background(CardDark, RoundedCornerShape(8.dp))
                    ) {
                        Text(settings.sweepSpeed.label, color = TextPrimary, fontSize = 11.sp)
                    }
                    DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                        EcgSweepSpeed.entries.forEach { speed ->
                            DropdownMenuItem(
                                text = { Text(speed.label) },
                                onClick = {
                                    viewModel.updateSweepSpeed(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }

                // Gain selector
                Box {
                    TextButton(
                        onClick = { showGainMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.background(CardDark, RoundedCornerShape(8.dp))
                    ) {
                        Text(settings.gain.label, color = TextPrimary, fontSize = 11.sp)
                    }
                    DropdownMenu(expanded = showGainMenu, onDismissRequest = { showGainMenu = false }) {
                        EcgGain.entries.forEach { gain ->
                            DropdownMenuItem(
                                text = { Text(gain.label) },
                                onClick = {
                                    viewModel.updateGain(gain)
                                    showGainMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center: Full Large Real-Time ECG Waveform
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            EcgCanvasGraph(
                sampleFlow = viewModel.liveSamples,
                gain = settings.gain,
                speed = settings.sweepSpeed,
                lead = settings.selectedLead,
                showGrid = settings.isGridVisible,
                waveformThickness = settings.waveformThickness,
                isPaused = isPaused,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Cards: HR and Signal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // HR Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "HR", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${vitals.heartRate}",
                            color = PhosphorGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "BPM", color = PhosphorGreen.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }

            // Signal Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "SIGNAL", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = vitals.signalQuality.label.uppercase(),
                        color = when (vitals.signalQuality) {
                            com.cardioconnect.domain.model.SignalQuality.GOOD -> PhosphorGreen
                            com.cardioconnect.domain.model.SignalQuality.WEAK -> MedicalAmber
                            else -> EmergencyRed
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Recording Badge if active
            if (isRecording) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "REC", color = EmergencyRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = formatTimer(recordingDuration),
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row: [Pause], [Record/Recording], [Stop], [Mark Event]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // [Pause / Resume]
            Button(
                onClick = { viewModel.togglePause() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = PhosphorGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isPaused) "Resume" else "Pause", color = TextPrimary, fontSize = 12.sp)
            }

            // [Record]
            if (!isRecording) {
                Button(
                    onClick = { viewModel.startRecording() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                // [Stop]
                Button(
                    onClick = { viewModel.stopRecording() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // [Mark Event]
                Button(
                    onClick = { viewModel.markEvent("Symptom Event") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderDark)
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = PhosphorGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatTimer(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
