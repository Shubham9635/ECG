package com.cardioconnect.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.cardioconnect.domain.model.EcgSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportExporter(private val context: Context) {

    suspend fun generatePdf(session: EcgSession): File = withContext(Dispatchers.IO) {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val fileName = "CardioConnect_Report_${session.id}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(reportsDir, fileName)

        // Standard A4 dimensions at 72 DPI: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawReportContent(canvas, session, pageWidth, pageHeight)

        document.finishPage(page)
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        pdfFile
    }

    private fun drawReportContent(canvas: Canvas, session: EcgSession, width: Int, height: Int) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(session.startTimeMs))

        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Header Banner
        val headerPaint = Paint().apply {
            color = Color.parseColor("#0A0E17")
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 64f, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("CardioConnect", 36f, 38f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#00E676")
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText("Clinical-Grade ECG Telemetry Report", 36f, 52f, subtitlePaint)

        // Session Information Table
        val labelPaint = Paint().apply {
            color = Color.parseColor("#5A6B82")
            textSize = 10f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#121A28")
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var y = 95f
        val col1 = 36f
        val col2 = 220f
        val col3 = 400f

        canvas.drawText("SESSION DETAILS", col1, y, labelPaint)
        y += 16f
        canvas.drawText("Date & Time: $dateStr", col1, y, valuePaint)
        canvas.drawText("Duration: ${formatDuration(session.durationSeconds)}", col2, y, valuePaint)
        canvas.drawText("Lead: ${session.lead.displayName}", col3, y, valuePaint)

        y += 18f
        canvas.drawText("Device: ${session.deviceName}", col1, y, valuePaint)
        canvas.drawText("Avg Heart Rate: ${session.avgHeartRate} BPM", col2, y, valuePaint)
        canvas.drawText("Range: ${session.minHeartRate} - ${session.maxHeartRate} BPM", col3, y, valuePaint)

        y += 18f
        val spo2Str = session.spo2?.let { "$it%" } ?: "N/A"
        val bpStr = if (session.bpSystolic != null && session.bpDiastolic != null) "${session.bpSystolic}/${session.bpDiastolic} mmHg" else "N/A"
        val tempStr = session.temperature?.let { String.format(Locale.US, "%.1f°C", it) } ?: "N/A"

        canvas.drawText("SpO₂: $spo2Str", col1, y, valuePaint)
        canvas.drawText("Blood Pressure: $bpStr", col2, y, valuePaint)
        canvas.drawText("Temperature: $tempStr", col3, y, valuePaint)

        // Divider
        y += 20f
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E6ED")
            strokeWidth = 1f
        }
        canvas.drawLine(col1, y, width - col1, y, linePaint)

        // ECG Section Title & Calibration info
        y += 24f
        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#121A28")
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("ELECTROCARDIOGRAM TRACE (${session.lead.displayName})", col1, y, sectionTitlePaint)

        val calibPaint = Paint().apply {
            color = Color.parseColor("#7A8B9E")
            textSize = 9.5f
            isAntiAlias = true
        }
        canvas.drawText("Speed: 25 mm/s | Gain: 10 mm/mV | Standard Millimeter Grid", col1, y + 14f, calibPaint)

        // ECG Grid Box
        y += 26f
        val gridLeft = 36f
        val gridTop = y
        val gridWidth = width - 72f
        val gridHeight = 320f
        val gridRight = gridLeft + gridWidth
        val gridBottom = gridTop + gridHeight

        drawEcgGrid(canvas, gridLeft, gridTop, gridRight, gridBottom)

        // Draw Waveform onto grid
        drawWaveform(canvas, session.samplePoints, gridLeft, gridTop, gridWidth, gridHeight)

        // Event Markers Section
        y = gridBottom + 30f
        if (session.eventMarkers.isNotEmpty()) {
            canvas.drawText("MARKED EVENTS", col1, y, sectionTitlePaint)
            y += 16f
            session.eventMarkers.forEach { marker ->
                canvas.drawText("• ${marker.note} (at ${marker.heartRateAtEvent} BPM)", col1 + 10f, y, valuePaint)
                y += 14f
            }
        }

        // Regulatory & Safety Disclaimer at bottom
        val disclaimerBoxTop = height - 90f
        val disclaimerBg = Paint().apply {
            color = Color.parseColor("#F5F7FA")
        }
        canvas.drawRect(col1, disclaimerBoxTop, width - col1, height - 36f, disclaimerBg)

        val disclaimerPaint = Paint().apply {
            color = Color.parseColor("#718096")
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText("MEDICAL SAFETY NOTICE:", col1 + 12f, disclaimerBoxTop + 18f, Paint().apply {
            color = Color.parseColor("#C53030")
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        })
        canvas.drawText("Data displayed by this application is for monitoring/information purposes and is not a medical diagnosis.", col1 + 12f, disclaimerBoxTop + 32f, disclaimerPaint)
        canvas.drawText("CardioConnect prototype software does not diagnose cardiac pathologies. Consult a physician for medical evaluation.", col1 + 12f, disclaimerBoxTop + 44f, disclaimerPaint)
    }

    private fun drawEcgGrid(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val minorPaint = Paint().apply {
            color = Color.parseColor("#FCE4EC") // Light medical pink grid
            strokeWidth = 0.5f
        }
        val majorPaint = Paint().apply {
            color = Color.parseColor("#F8BBD0") // Major 5mm lines
            strokeWidth = 1.0f
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D81B60")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        val stepMinor = 5f // ~1 mm equivalent in PDF points
        val stepMajor = 25f // ~5 mm

        // Vertical lines
        var x = left
        while (x <= right) {
            val isMajor = ((x - left) % stepMajor).toInt() == 0
            canvas.drawLine(x, top, x, bottom, if (isMajor) majorPaint else minorPaint)
            x += stepMinor
        }

        // Horizontal lines
        var y = top
        while (y <= bottom) {
            val isMajor = ((y - top) % stepMajor).toInt() == 0
            canvas.drawLine(left, y, right, y, if (isMajor) majorPaint else minorPaint)
            y += stepMinor
        }

        canvas.drawRect(left, top, right, bottom, borderPaint)
    }

    private fun drawWaveform(
        canvas: Canvas,
        samples: List<Float>,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ) {
        if (samples.isEmpty()) return

        val wavePaint = Paint().apply {
            color = Color.parseColor("#0A0E17")
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
            isAntiAlias = true
        }

        val centerY = top + (height / 2f)
        val mvScale = 35f // points per mV

        val path = Path()
        val stepX = width / samples.size.coerceAtLeast(1)

        samples.forEachIndexed { index, mv ->
            val px = left + (index * stepX)
            val py = centerY - (mv * mvScale)
            val clampedY = py.coerceIn(top, top + height)
            if (index == 0) {
                path.moveTo(px, clampedY)
            } else {
                path.lineTo(px, clampedY)
            }
        }

        canvas.drawPath(path, wavePaint)
    }

    private fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
