package com.cardioconnect.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.domain.model.EcgGain
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSample
import com.cardioconnect.domain.model.EcgSweepSpeed
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.GridMajorDark
import com.cardioconnect.ui.theme.GridMinorDark
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.SweepLineColor
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

@Composable
fun EcgCanvasGraph(
    sampleFlow: Flow<EcgSample>?,
    modifier: Modifier = Modifier,
    gain: EcgGain = EcgGain.GAIN_10,
    speed: EcgSweepSpeed = EcgSweepSpeed.SPEED_25_0,
    lead: EcgLead = EcgLead.LEAD_II,
    waveformColor: Color = PhosphorGreen,
    waveformThickness: Float = 2.5f,
    showGrid: Boolean = true,
    isPaused: Boolean = false,
    staticSamples: List<Float>? = null // For review playback
) {
    // Ring buffer of voltage values
    val bufferCapacity = 600
    val voltageBuffer = remember {
        mutableStateListOf<Float>().apply {
            if (staticSamples != null && staticSamples.isNotEmpty()) {
                val sub = staticSamples.take(bufferCapacity)
                addAll(sub)
                while (size < bufferCapacity) add(0f)
            } else {
                repeat(bufferCapacity) { add(0f) }
            }
        }
    }

    // Sweep index pointer
    val sweepIndex = remember { androidx.compose.runtime.mutableIntStateOf(0) }

    // Stream samples into buffer
    LaunchedEffect(sampleFlow, isPaused) {
        if (sampleFlow != null && !isPaused && staticSamples == null) {
            sampleFlow.collect { sample ->
                val currentIndex = sweepIndex.intValue % bufferCapacity
                voltageBuffer[currentIndex] = sample.voltageMv

                // Clear next few samples ahead to create the "erasing beam" effect of clinical monitors
                val eraseAhead = 8
                for (k in 1..eraseAhead) {
                    val eraseIdx = (currentIndex + k) % bufferCapacity
                    voltageBuffer[eraseIdx] = Float.NaN
                }

                sweepIndex.intValue = (currentIndex + 1) % bufferCapacity
            }
        }
    }

    Box(
        modifier = modifier
            .background(BackgroundDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // 1. Draw Medical Standard Millimeter Grid
            if (showGrid) {
                val minorSpacingPx = 18f // ~1mm equivalent scaled on display
                val majorSpacingPx = minorSpacingPx * 5f // 5mm major square

                // Minor vertical
                var x = 0f
                while (x <= width) {
                    val isMajor = ((x / minorSpacingPx).roundToInt() % 5) == 0
                    drawLine(
                        color = if (isMajor) GridMajorDark else GridMinorDark,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = if (isMajor) 1.2f else 0.6f
                    )
                    x += minorSpacingPx
                }

                // Minor horizontal
                var y = 0f
                while (y <= height) {
                    val isMajor = (((y - centerY) / minorSpacingPx).roundToInt() % 5) == 0
                    drawLine(
                        color = if (isMajor) GridMajorDark else GridMinorDark,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = if (isMajor) 1.2f else 0.6f
                    )
                    y += minorSpacingPx
                }
            }

            // 2. Amplitude Calibration Pulse (Standard 1 mV square pulse at the left edge)
            val calibLeft = 12f
            val calibWidth = 24f
            val mvScalePx = (height * 0.30f) * (gain.mmPerMv / 10.0f) // 1 mV height in pixels

            val calibPath = Path().apply {
                moveTo(calibLeft, centerY)
                lineTo(calibLeft + 6f, centerY)
                lineTo(calibLeft + 6f, centerY - mvScalePx)
                lineTo(calibLeft + 18f, centerY - mvScalePx)
                lineTo(calibLeft + 18f, centerY)
                lineTo(calibLeft + calibWidth, centerY)
            }
            drawPath(
                path = calibPath,
                color = waveformColor.copy(alpha = 0.5f),
                style = Stroke(width = 1.5f)
            )

            // 3. Render Waveform (Sweep or Static trace)
            val renderLeft = calibLeft + calibWidth + 10f
            val renderWidth = width - renderLeft
            val stepX = renderWidth / (bufferCapacity - 1)

            val wavePath = Path()
            var pathStarted = false

            for (i in 0 until bufferCapacity) {
                val mv = voltageBuffer[i]
                val px = renderLeft + (i * stepX)

                if (mv.isNaN()) {
                    // Gap in trace (sweep head)
                    pathStarted = false
                    continue
                }

                val py = centerY - (mv * mvScalePx)
                val clampedY = py.coerceIn(4f, height - 4f)

                if (!pathStarted) {
                    wavePath.moveTo(px, clampedY)
                    pathStarted = true
                } else {
                    wavePath.lineTo(px, clampedY)
                }
            }

            drawPath(
                path = wavePath,
                color = waveformColor,
                style = Stroke(
                    width = waveformThickness,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 4. Sweep Indicator Line (Vertical scanline at current sweep head)
            if (staticSamples == null && !isPaused) {
                val sweepX = renderLeft + (sweepIndex.intValue * stepX)
                if (sweepX in renderLeft..width) {
                    drawLine(
                        color = SweepLineColor,
                        start = Offset(sweepX, 0f),
                        end = Offset(sweepX, height),
                        strokeWidth = 2f
                    )
                }
            }
        }

        // Overlay Telemetry Legend (Top-Left)
        Text(
            text = "${lead.displayName}  |  ${gain.label}  |  ${speed.label}",
            color = waveformColor.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 8.dp)
        )

        // Voltage Caliper Legend (Top-Right)
        Text(
            text = "1 mV",
            color = Color(0xFF78909C),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 8.dp)
        )
    }
}
