package com.cardioconnect.domain.model

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class EcgAlert(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestampMs: Long = System.currentTimeMillis(),
    val isDismissed: Boolean = false
)
