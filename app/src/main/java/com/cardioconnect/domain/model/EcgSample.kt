package com.cardioconnect.domain.model

data class EcgSample(
    val timestampMs: Long,
    val voltageMv: Float,
    val lead: EcgLead = EcgLead.LEAD_II,
    val sampleIndex: Long = 0L
)
