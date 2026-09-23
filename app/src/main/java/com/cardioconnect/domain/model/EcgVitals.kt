package com.cardioconnect.domain.model

enum class SignalQuality(val label: String) {
    GOOD("Signal Good"),
    WEAK("Signal Weak"),
    ELECTRODE_DISCONNECTED("Electrode Disconnected"),
    NO_SIGNAL("No Signal")
}

data class BloodPressure(
    val systolic: Int,
    val diastolic: Int,
    val meanArterialPressure: Int = (diastolic * 2 + systolic) / 3
) {
    val displayValue: String
        get() = "$systolic / $diastolic mmHg"
}

data class EcgVitals(
    val heartRate: Int = 78,
    val avgHeartRate: Int = 76,
    val minHeartRate: Int = 68,
    val maxHeartRate: Int = 88,
    val signalQuality: SignalQuality = SignalQuality.GOOD,
    val rhythmClassification: String? = "Normal Sinus Rhythm (Reported by Device)",
    val spo2Percentage: Int? = 98,
    val bloodPressure: BloodPressure? = BloodPressure(120, 80),
    val temperatureCelsius: Float? = 36.7f,
    val batteryLevelPercentage: Int? = 85,
    val rssiDbm: Int? = -62,
    val isBleConnected: Boolean = false,
    val isDemoMode: Boolean = false,
    val connectionDurationSeconds: Long = 0L,
    val connectedDeviceName: String? = null
)
