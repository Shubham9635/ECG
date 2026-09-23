package com.cardioconnect.domain.model

data class EcgEventMarker(
    val timestampMs: Long,
    val note: String,
    val heartRateAtEvent: Int
)

data class EcgSession(
    val id: Long = 0L,
    val title: String,
    val patientId: String = "P-10245",
    val patientName: String = "Rahul Sharma",
    val startTimeMs: Long,
    val durationSeconds: Long,
    val avgHeartRate: Int,
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val lead: EcgLead = EcgLead.LEAD_II,
    val deviceName: String,
    val spo2: Int? = null,
    val bpSystolic: Int? = null,
    val bpDiastolic: Int? = null,
    val temperature: Float? = null,
    val isDemoData: Boolean = false,
    val eventMarkers: List<EcgEventMarker> = emptyList(),
    val samplePoints: List<Float> = emptyList() // Voltage points sampled for review/export
)
