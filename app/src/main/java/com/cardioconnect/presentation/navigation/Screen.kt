package com.cardioconnect.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Patients : Screen("patients")
    object PatientProfile : Screen("patient_profile/{patientId}") {
        fun createRoute(patientId: String) = "patient_profile/$patientId"
    }
    object LiveEcg : Screen("live_ecg")
    object History : Screen("history")
    object SessionDetail : Screen("session_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_detail/$sessionId"
    }
    object Device : Screen("device")
    object Settings : Screen("settings")
    object EcgConditionsGuide : Screen("ecg_conditions_guide")
}

