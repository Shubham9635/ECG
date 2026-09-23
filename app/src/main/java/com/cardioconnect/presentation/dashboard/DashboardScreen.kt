package com.cardioconnect.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.ui.components.AlertBanner
import com.cardioconnect.ui.components.EcgCanvasGraph
import com.cardioconnect.ui.components.HeartRateCard
import com.cardioconnect.ui.components.MedicalSafetyNotice
import com.cardioconnect.ui.components.RhythmClassificationBadge
import com.cardioconnect.ui.components.VitalsGridSection
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
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToLive: () -> Unit,
    onNavigateToDevice: () -> Unit,
    onNavigateToPatients: () -> Unit = {}
) {
    val vitals by viewModel.liveVitals.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val patientProfile by viewModel.patientProfile.collectAsState()
    val intervalMeasurements by viewModel.intervalMeasurements.collectAsState()
    val ecgInterpretation by viewModel.ecgInterpretation.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDurationSeconds.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    var showPatientEditDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedFindingForDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.cardioconnect.domain.model.EcgFindingDetail?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Patient Profile Header
        com.cardioconnect.ui.components.PatientHeaderCard(
            patient = patientProfile,
            onEditClick = { showPatientEditDialog = true },
            onSwitchPatientClick = onNavigateToPatients
        )
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
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bedside Telemetry Monitor",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )
            }

            // Connection / Demo Badge
            if (vitals.isDemoMode) {
                Box(
                    modifier = Modifier
                        .background(MedicalAmber.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .border(0.5.dp, MedicalAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "DEMO DATA — NOT REAL ECG",
                        color = MedicalAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (vitals.isBleConnected) PhosphorGreen.copy(alpha = 0.15f) else EmergencyRed.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (vitals.isBleConnected) PhosphorGreen else EmergencyRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (vitals.isBleConnected) "ECG Connected" else "Disconnected",
                        color = if (vitals.isBleConnected) PhosphorGreen else EmergencyRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Alerts
        alerts.firstOrNull()?.let { alert ->
            AlertBanner(
                alert = alert,
                onDismiss = { viewModel.dismissAlert(alert.id) }
            )
        }

        // Real-Time ECG Waveform Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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

                // Quick Floating Controls on Top-Right of Graph
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.togglePause() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = PhosphorGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToLive,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Recording Status & Actions Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecording) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "Recording",
                            tint = EmergencyRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recording ● ${formatTimer(recordingDuration)}",
                            color = EmergencyRed,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.markEvent("Clinical Event") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.stopRecording() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop", fontSize = 12.sp, color = Color.White)
                        }
                    }
                } else {
                    Column {
                        Text(text = "ECG Session", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Ready to record telemetry", color = TextTertiary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.startRecording() },
                        colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Session", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Heart Rate Main Card
        HeartRateCard(vitals = vitals)

        // ECG Interpretation Section with Risk/Attention Status & Findings
        com.cardioconnect.ui.components.EcgInterpretationCard(
            interpretation = ecgInterpretation,
            onFindingClick = { selectedFindingForDialog = it }
        )

        // ECG Interval Measurements (PR, QRS, QT, QTc, RR)
        com.cardioconnect.ui.components.EcgMeasurementsCard(
            measurements = intervalMeasurements
        )

        // Validated Device Rhythm
        RhythmClassificationBadge(rhythm = vitals.rhythmClassification)

        // Additional Vitals Grid (SpO2, BP, Temp, Battery/Device)
        VitalsGridSection(vitals = vitals)

        // Medical Disclaimer
        MedicalSafetyNotice()

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Patient Profile Edit Dialog
    if (showPatientEditDialog) {
        com.cardioconnect.ui.components.PatientEditDialog(
            initialPatient = patientProfile,
            onSave = { updated ->
                viewModel.updatePatientProfile(updated)
                showPatientEditDialog = false
            },
            onDismiss = { showPatientEditDialog = false }
        )
    }

    // Diagnostic Explanation Modal Dialog
    selectedFindingForDialog?.let { finding ->
        com.cardioconnect.ui.components.DiagnosticExplanationDialog(
            finding = finding,
            onDismiss = { selectedFindingForDialog = null }
        )
    }
}

private fun formatTimer(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
