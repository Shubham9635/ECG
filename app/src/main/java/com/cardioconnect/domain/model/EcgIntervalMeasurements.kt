package com.cardioconnect.domain.model

data class EcgIntervalMeasurements(
    val heartRateBpm: Int = 78,
    val prIntervalMs: Int = 160,
    val qrsDurationMs: Int = 92,
    val qtIntervalMs: Int = 380,
    val qtcIntervalMs: Int = 410,
    val rrIntervalMs: Int = 770,
    val pWaveDescription: String = "Normal upright morphology in Lead II",
    val qrsDescription: String = "Narrow QRS complex (<120 ms)",
    val stSegmentDescription: String = "Isoelectric (no acute elevation or depression)",
    val rhythmRegularity: String = "Regular",
    val signalQuality: String = "Good",
    val timestampMs: Long = System.currentTimeMillis()
)
