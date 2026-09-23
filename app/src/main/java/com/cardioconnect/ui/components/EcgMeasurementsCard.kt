package com.cardioconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.domain.model.EcgIntervalMeasurements
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary

@Composable
fun EcgMeasurementsCard(
    measurements: EcgIntervalMeasurements,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ECG INTERVAL MEASUREMENTS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2-Column Grid of Core Clinical Intervals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IntervalItem(label = "Heart Rate", value = "${measurements.heartRateBpm} BPM")
                IntervalItem(label = "PR Interval", value = "${measurements.prIntervalMs} ms")
                IntervalItem(label = "QRS Duration", value = "${measurements.qrsDurationMs} ms")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IntervalItem(label = "QT Interval", value = "${measurements.qtIntervalMs} ms")
                IntervalItem(label = "QTc (Bazett)", value = "${measurements.qtcIntervalMs} ms")
                IntervalItem(label = "RR Interval", value = "${measurements.rrIntervalMs} ms")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detailed Wave Morphology Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MorphologyRow(label = "P-Wave", detail = measurements.pWaveDescription)
                MorphologyRow(label = "QRS Complex", detail = measurements.qrsDescription)
                MorphologyRow(label = "ST Segment", detail = measurements.stSegmentDescription)
                MorphologyRow(label = "Regularity", detail = measurements.rhythmRegularity)
            }
        }
    }
}

@Composable
private fun IntervalItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextTertiary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            color = PhosphorGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MorphologyRow(label: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(text = detail, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Normal)
    }
}
