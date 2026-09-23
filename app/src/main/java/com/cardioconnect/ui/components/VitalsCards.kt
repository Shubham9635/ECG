package com.cardioconnect.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.domain.model.EcgVitals
import com.cardioconnect.domain.model.SignalQuality
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.ElectricCyan
import com.cardioconnect.ui.theme.EmergencyRed
import com.cardioconnect.ui.theme.HospitalBlue
import com.cardioconnect.ui.theme.MedicalAmber
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun HeartRateCard(
    vitals: EcgVitals,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (60000 / vitals.heartRate.coerceIn(40, 160)).toInt(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Rate",
                        tint = EmergencyRed,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(pulseScale)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HEART RATE",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Signal Quality Chip
                SignalQualityBadge(quality = vitals.signalQuality)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Large Digits
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${vitals.heartRate}",
                    color = PhosphorGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BPM",
                    color = PhosphorGreen.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Stats: Current, Avg, Min, Max
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "AVG", value = "${vitals.avgHeartRate}")
                StatItem(label = "MIN", value = "${vitals.minHeartRate}")
                StatItem(label = "MAX", value = "${vitals.maxHeartRate}")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = "$value bpm",
            color = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SignalQualityBadge(quality: SignalQuality) {
    val (color, text) = when (quality) {
        SignalQuality.GOOD -> PhosphorGreen to "GOOD"
        SignalQuality.WEAK -> MedicalAmber to "WEAK"
        SignalQuality.ELECTRODE_DISCONNECTED -> EmergencyRed to "DISCONNECTED"
        SignalQuality.NO_SIGNAL -> EmergencyRed to "NO SIGNAL"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VitalsGridSection(
    vitals: EcgVitals,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SpO2 Card
            vitals.spo2Percentage?.let { spo2 ->
                VitalParameterCard(
                    modifier = Modifier.weight(1f),
                    title = "SpO₂",
                    value = "$spo2",
                    unit = "%",
                    icon = Icons.Default.Air,
                    accentColor = ElectricCyan,
                    subtitle = if (spo2 >= 95) "Normal" else "Monitor closely"
                )
            }

            // Blood Pressure Card
            vitals.bloodPressure?.let { bp ->
                VitalParameterCard(
                    modifier = Modifier.weight(1f),
                    title = "NIBP",
                    value = "${bp.systolic}/${bp.diastolic}",
                    unit = "mmHg",
                    icon = Icons.Default.Speed,
                    accentColor = HospitalBlue,
                    subtitle = "MAP: ${bp.meanArterialPressure} mmHg"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Temperature Card
            vitals.temperatureCelsius?.let { temp ->
                VitalParameterCard(
                    modifier = Modifier.weight(1f),
                    title = "TEMP",
                    value = String.format(Locale.US, "%.1f", temp),
                    unit = "°C",
                    icon = Icons.Default.Thermostat,
                    accentColor = MedicalAmber,
                    subtitle = "Skin Contact"
                )
            }

            // Device Telemetry / Battery Card
            VitalParameterCard(
                modifier = Modifier.weight(1f),
                title = if (vitals.isDemoMode) "SIMULATOR" else "BLE DEVICE",
                value = vitals.batteryLevelPercentage?.let { "$it%" } ?: "--",
                unit = "BATTERY",
                icon = if (vitals.isBleConnected) Icons.Default.Bluetooth else Icons.Default.BatteryChargingFull,
                accentColor = if (vitals.isBleConnected || vitals.isDemoMode) PhosphorGreen else EmergencyRed,
                subtitle = vitals.connectedDeviceName ?: (if (vitals.isDemoMode) "Demo Pro" else "Disconnected")
            )
        }
    }
}

@Composable
fun VitalParameterCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = TextTertiary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RhythmClassificationBadge(
    rhythm: String?,
    modifier: Modifier = Modifier
) {
    if (rhythm == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(PhosphorGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VALIDATED RHYTHM",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = rhythm,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
