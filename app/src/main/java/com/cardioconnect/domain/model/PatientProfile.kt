package com.cardioconnect.domain.model

data class PatientProfile(
    val patientId: String = "P-10245",
    val fullName: String = "Rahul Sharma",
    val age: Int = 52,
    val dateOfBirth: String = "14 May 1974",
    val gender: String = "Male",
    val heightCm: Float = 175f,
    val weightKg: Float = 78f,
    val bloodGroup: String = "B+",
    val phoneNumber: String = "+91 98765 43210",
    val emergencyContact: String = "Sunita Sharma (+91 98765 43211)",
    val existingMedicalNotes: String = "Mild hypertension managed with ACE inhibitors. No prior myocardial infarction.",
    val currentMedications: String = "Ramipril 5mg OD, Atorvastatin 10mg HS",
    val allergies: String = "Penicillin (rash)",
    val clinicianName: String = "Dr. Amit Verma (Cardiology)",
    val photoUri: String? = null,
    val totalSessions: Int = 3,
    val lastEcgTimestampMs: Long? = System.currentTimeMillis() - 3600000L
) {
    val bmi: Float
        get() {
            val heightM = heightCm / 100f
            return if (heightM > 0) weightKg / (heightM * heightM) else 0f
        }

    val bmiCategory: String
        get() = when {
            bmi < 18.5f -> "Underweight"
            bmi in 18.5f..24.9f -> "Normal weight"
            bmi in 25.0f..29.9f -> "Overweight"
            else -> "Obese"
        }
}
