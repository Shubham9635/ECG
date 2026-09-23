package com.cardioconnect.domain.model

enum class EcgRiskStatus(val label: String, val colorHex: Long) {
    NO_SIGNIFICANT_FINDING("No significant device/algorithm finding", 0xFF00E676),
    REQUIRES_ATTENTION("Finding requires attention", 0xFFFFD600),
    POTENTIALLY_URGENT("Potentially urgent finding reported by the device/algorithm", 0xFFFF3D71),
    UNABLE_TO_INTERPRET("Unable to interpret", 0xFF8F9BB3)
}

data class EcgFindingDetail(
    val title: String,
    val measuredValue: String,
    val whatItMeans: String,
    val importantContext: String,
    val recommendedAction: String,
    val riskStatus: EcgRiskStatus = EcgRiskStatus.NO_SIGNIFICANT_FINDING
)

data class EcgInterpretation(
    val primaryRhythm: String = "Normal Sinus Rhythm",
    val riskStatus: EcgRiskStatus = EcgRiskStatus.NO_SIGNIFICANT_FINDING,
    val findings: List<EcgFindingDetail> = listOf(
        EcgFindingDetail(
            title = "Normal sinus rhythm",
            measuredValue = "HR 78 BPM, PR 160 ms, QRS 92 ms",
            whatItMeans = "Electrical impulses originate regularly from the sinoatrial node within standard physiological rate and conduction limits.",
            importantContext = "This represents normal cardiac conduction during the recording window. A normal resting ECG does not rule out intermittent symptoms or structural heart disease.",
            recommendedAction = "Continue standard clinical telemetry monitoring.",
            riskStatus = EcgRiskStatus.NO_SIGNIFICANT_FINDING
        )
    ),
    val algorithmSource: String = "Validated Automated Rhythm Classifier v2.4",
    val interpretationTimestampMs: Long = System.currentTimeMillis()
)
