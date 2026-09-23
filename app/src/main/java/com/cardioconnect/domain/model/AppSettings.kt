package com.cardioconnect.domain.model

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

enum class EcgSweepSpeed(val mmPerSec: Float, val label: String) {
    SPEED_12_5(12.5f, "12.5 mm/s"),
    SPEED_25_0(25.0f, "25.0 mm/s"),
    SPEED_50_0(50.0f, "50.0 mm/s")
}

enum class EcgGain(val mmPerMv: Float, val label: String) {
    GAIN_5(5.0f, "5 mm/mV (0.5x)"),
    GAIN_10(10.0f, "10 mm/mV (1.0x)"),
    GAIN_20(20.0f, "20 mm/mV (2.0x)")
}

data class AppSettings(
    val isDemoMode: Boolean = true, // Default to true so app works immediately on launch!
    val autoReconnect: Boolean = true,
    val selectedLead: EcgLead = EcgLead.LEAD_II,
    val sweepSpeed: EcgSweepSpeed = EcgSweepSpeed.SPEED_25_0,
    val gain: EcgGain = EcgGain.GAIN_10,
    val isGridVisible: Boolean = true,
    val waveformThickness: Float = 2.5f,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val soundAlertsEnabled: Boolean = true,
    val connectionAlertsEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)
