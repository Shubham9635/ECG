package com.cardioconnect.data.export

import android.content.Context
import com.cardioconnect.domain.model.EcgSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvReportExporter(private val context: Context) {

    suspend fun generateCsv(session: EcgSession): File = withContext(Dispatchers.IO) {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val fileName = "CardioConnect_Session_${session.id}_${System.currentTimeMillis()}.csv"
        val csvFile = File(reportsDir, fileName)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

        FileWriter(csvFile).use { writer ->
            // Medical & Device Header metadata
            writer.append("# CardioConnect ECG Export\n")
            writer.append("# Session ID: ${session.id}\n")
            writer.append("# Device: ${session.deviceName}\n")
            writer.append("# Lead: ${session.lead.displayName}\n")
            writer.append("# Start Time: ${dateFormat.format(Date(session.startTimeMs))}\n")
            writer.append("# Duration (seconds): ${session.durationSeconds}\n")
            writer.append("# Avg HR: ${session.avgHeartRate}, Min HR: ${session.minHeartRate}, Max HR: ${session.maxHeartRate}\n")
            writer.append("# Disclaimer: For monitoring/informational purposes only. Not a medical diagnosis.\n#\n")

            // Column Headers
            writer.append("SampleIndex,TimeOffsetSec,Voltage_mV,Lead,HeartRate_BPM\n")

            val sampleIntervalSec = if (session.samplePoints.isNotEmpty()) {
                session.durationSeconds.toFloat() / session.samplePoints.size
            } else 0.004f // default 250Hz

            session.samplePoints.forEachIndexed { index, mv ->
                val timeOffset = index * sampleIntervalSec
                writer.append("$index,")
                writer.append(String.format(Locale.US, "%.4f,", timeOffset))
                writer.append(String.format(Locale.US, "%.4f,", mv))
                writer.append("${session.lead.name},")
                writer.append("${session.avgHeartRate}\n")
            }
        }

        csvFile
    }
}
