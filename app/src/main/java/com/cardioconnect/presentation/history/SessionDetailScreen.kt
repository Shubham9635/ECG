package com.cardioconnect.presentation.history

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.domain.model.EcgGain
import com.cardioconnect.domain.model.EcgSweepSpeed
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.ui.components.EcgCanvasGraph
import com.cardioconnect.ui.components.MedicalSafetyNotice
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.ElectricCyan
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    sessionId: Long,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.selectedSession.collectAsState()
    val scrollState = rememberScrollState()

    var scrubOffsetRatio by remember { mutableFloatStateOf(0f) }
    var zoomLevel by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(sessionId) {
        viewModel.loadSessionDetails(sessionId)
    }

    if (session == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Loading session telemetry...", color = TextSecondary)
        }
        return
    }

    val currentSession = session!!
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
    val dateText = dateFormat.format(Date(currentSession.startTimeMs))

    // Slice samples based on scrub offset and zoom
    val totalSamples = currentSession.samplePoints.size
    val windowSize = (600 / zoomLevel).toInt().coerceIn(100, totalSamples.coerceAtLeast(100))
    val startIndex = ((totalSamples - windowSize).coerceAtLeast(0) * scrubOffsetRatio).toInt()
    val endIndex = (startIndex + windowSize).coerceAtMost(totalSamples)
    val displayedSamples = if (totalSamples > 0) {
        currentSession.samplePoints.subList(startIndex, endIndex)
    } else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = currentSession.title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$dateText • ${currentSession.deviceName}",
                    color = TextTertiary,
                    fontSize = 11.5.sp
                )
            }
        }

        // Complete ECG Waveform Display Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            EcgCanvasGraph(
                sampleFlow = null,
                gain = EcgGain.GAIN_10,
                speed = EcgSweepSpeed.SPEED_25_0,
                lead = currentSession.lead,
                staticSamples = displayedSamples,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Scrubbing Bar & Zoom Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Waveform Scrubbing", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Offset: ${String.format(Locale.US, "%.1f", scrubOffsetRatio * currentSession.durationSeconds)}s / ${currentSession.durationSeconds}s",
                        color = PhosphorGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                Slider(
                    value = scrubOffsetRatio,
                    onValueChange = { scrubOffsetRatio = it },
                    colors = SliderDefaults.colors(
                        thumbColor = PhosphorGreen,
                        activeTrackColor = PhosphorGreen,
                        inactiveTrackColor = CardBorderDark
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Zoom Scale", color = TextTertiary, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1f to "1x", 1.5f to "1.5x", 2f to "2x", 3f to "3x").forEach { (level, label) ->
                            Button(
                                onClick = { zoomLevel = level },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (zoomLevel == level) PhosphorGreen else CardBorderDark
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (zoomLevel == level) androidx.compose.ui.graphics.Color.Black else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Session Information Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "SESSION METRICS", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricText(label = "Duration", value = formatDuration(currentSession.durationSeconds))
                    MetricText(label = "Lead", value = currentSession.lead.displayName)
                    MetricText(label = "Average HR", value = "${currentSession.avgHeartRate} BPM")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricText(label = "Minimum HR", value = "${currentSession.minHeartRate} BPM")
                    MetricText(label = "Maximum HR", value = "${currentSession.maxHeartRate} BPM")
                    MetricText(label = "SpO₂", value = currentSession.spo2?.let { "$it%" } ?: "N/A")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricText(
                        label = "Blood Pressure",
                        value = if (currentSession.bpSystolic != null && currentSession.bpDiastolic != null) "${currentSession.bpSystolic}/${currentSession.bpDiastolic} mmHg" else "N/A"
                    )
                    MetricText(label = "Temperature", value = currentSession.temperature?.let { "$it °C" } ?: "N/A")
                    MetricText(label = "Data Mode", value = if (currentSession.isDemoData) "Demo Simulation" else "Hardware BLE")
                }
            }
        }

        // Event Markers Section
        if (currentSession.eventMarkers.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "MARKED EVENTS", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    currentSession.eventMarkers.forEach { marker ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = PhosphorGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = marker.note, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "(${marker.heartRateAtEvent} BPM)", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Export Actions: PDF and CSV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.exportPdf(currentSession, context) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.exportCsv(currentSession, context) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Regulatory & Medical Safety Notice
        MedicalSafetyNotice()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MetricText(label: String, value: String) {
    Column {
        Text(text = label, color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
