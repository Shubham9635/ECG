package com.cardioconnect.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.cardioconnect.domain.model.EcgFindingDetail
import com.cardioconnect.domain.model.EcgIntervalMeasurements
import com.cardioconnect.domain.model.EcgInterpretation
import com.cardioconnect.domain.model.EcgRiskStatus
import com.cardioconnect.domain.model.PatientProfile
import com.cardioconnect.domain.repository.PatientRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PatientRepositoryImpl(context: Context) : PatientRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("cardioconnect_multi_patients", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _allPatients = MutableStateFlow(loadAllPatients())
    override val allPatients: StateFlow<List<PatientProfile>> = _allPatients.asStateFlow()

    private val _activePatient = MutableStateFlow(loadActivePatient())
    override val activePatient: StateFlow<PatientProfile> = _activePatient.asStateFlow()

    private val _intervalMeasurements = MutableStateFlow(EcgIntervalMeasurements())
    override val intervalMeasurements: StateFlow<EcgIntervalMeasurements> = _intervalMeasurements.asStateFlow()

    private val _ecgInterpretation = MutableStateFlow(createDefaultInterpretation())
    override val ecgInterpretation: StateFlow<EcgInterpretation> = _ecgInterpretation.asStateFlow()

    private fun loadAllPatients(): List<PatientProfile> {
        val json = prefs.getString("key_patient_list", null)
        if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<PatientProfile>>() {}.type
                val list: List<PatientProfile> = gson.fromJson(json, type)
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }

        // Seed default patients
        val seed = listOf(
            PatientProfile(
                patientId = "P-10245",
                fullName = "Rahul Sharma",
                age = 52,
                dateOfBirth = "14 May 1974",
                gender = "Male",
                heightCm = 175f,
                weightKg = 78f,
                bloodGroup = "B+",
                phoneNumber = "+91 98765 43210",
                emergencyContact = "Sunita Sharma (+91 98765 43211)",
                existingMedicalNotes = "Mild hypertension managed with ACE inhibitors. No prior myocardial infarction.",
                currentMedications = "Ramipril 5mg OD, Atorvastatin 10mg HS",
                allergies = "Penicillin (rash)",
                clinicianName = "Dr. Amit Verma (Cardiology)",
                totalSessions = 3,
                lastEcgTimestampMs = System.currentTimeMillis() - 3600000L
            ),
            PatientProfile(
                patientId = "P-10246",
                fullName = "Priya Verma",
                age = 45,
                dateOfBirth = "22 August 1981",
                gender = "Female",
                heightCm = 162f,
                weightKg = 61f,
                bloodGroup = "O+",
                phoneNumber = "+91 98765 43212",
                emergencyContact = "Rohan Verma (+91 98765 43213)",
                existingMedicalNotes = "Occasional palpitations during physical exertion. Normal echocardiogram.",
                currentMedications = "None",
                allergies = "No known drug allergies",
                clinicianName = "Dr. Amit Verma (Cardiology)",
                totalSessions = 1,
                lastEcgTimestampMs = System.currentTimeMillis() - 86400000L
            ),
            PatientProfile(
                patientId = "P-10247",
                fullName = "Vikram Singh",
                age = 64,
                dateOfBirth = "03 November 1961",
                gender = "Male",
                heightCm = 170f,
                weightKg = 82f,
                bloodGroup = "A+",
                phoneNumber = "+91 98765 43214",
                emergencyContact = "Anita Singh (+91 98765 43215)",
                existingMedicalNotes = "Type 2 Diabetes, post-PCI stent placed 2024. Routine outpatient telemetry.",
                currentMedications = "Metformin 500mg, Clopidogrel 75mg, Metoprolol 25mg",
                allergies = "Sulfa drugs",
                clinicianName = "Dr. S. K. Gupta",
                totalSessions = 5,
                lastEcgTimestampMs = System.currentTimeMillis() - 172800000L
            )
        )
        savePatientsList(seed)
        return seed
    }

    private fun loadActivePatient(): PatientProfile {
        val activeId = prefs.getString("key_active_patient_id", "P-10245")
        return _allPatients.value.firstOrNull { it.patientId == activeId }
            ?: _allPatients.value.firstOrNull()
            ?: PatientProfile()
    }

    private fun savePatientsList(list: List<PatientProfile>) {
        prefs.edit().putString("key_patient_list", gson.toJson(list)).apply()
        _allPatients.value = list
    }

    override fun setActivePatient(patientId: String) {
        val patient = _allPatients.value.firstOrNull { it.patientId == patientId } ?: return
        prefs.edit().putString("key_active_patient_id", patientId).apply()
        _activePatient.value = patient
    }

    override fun addPatient(patient: PatientProfile): Boolean {
        // Prevent duplicate IDs
        if (_allPatients.value.any { it.patientId.equals(patient.patientId.trim(), ignoreCase = true) }) {
            return false
        }
        val updatedList = _allPatients.value.toMutableList().apply { add(patient) }
        savePatientsList(updatedList)
        setActivePatient(patient.patientId)
        return true
    }

    override fun updatePatient(patient: PatientProfile) {
        val list = _allPatients.value.toMutableList()
        val index = list.indexOfFirst { it.patientId == patient.patientId }
        if (index >= 0) {
            list[index] = patient
            savePatientsList(list)
            if (_activePatient.value.patientId == patient.patientId) {
                _activePatient.value = patient
            }
        }
    }

    override fun deletePatient(patientId: String) {
        val list = _allPatients.value.filterNot { it.patientId == patientId }
        savePatientsList(list)
        if (_activePatient.value.patientId == patientId && list.isNotEmpty()) {
            setActivePatient(list.first().patientId)
        }
    }

    override fun getPatientById(patientId: String): PatientProfile? {
        return _allPatients.value.firstOrNull { it.patientId == patientId }
    }

    override fun getNextSuggestedPatientId(): String {
        var maxNum = 10245
        _allPatients.value.forEach { p ->
            val numStr = p.patientId.replace(Regex("[^0-9]"), "")
            numStr.toIntOrNull()?.let {
                if (it > maxNum) maxNum = it
            }
        }
        return "P-${maxNum + 1}"
    }

    override fun updateMeasurements(measurements: EcgIntervalMeasurements) {
        _intervalMeasurements.value = measurements
    }

    override fun updateInterpretation(interpretation: EcgInterpretation) {
        _ecgInterpretation.value = interpretation
    }

    private fun createDefaultInterpretation(): EcgInterpretation {
        return EcgInterpretation(
            primaryRhythm = "Normal Sinus Rhythm",
            riskStatus = EcgRiskStatus.NO_SIGNIFICANT_FINDING,
            findings = listOf(
                EcgFindingDetail(
                    title = "Normal sinus rhythm",
                    measuredValue = "HR 78 BPM, PR 160 ms, QRS 92 ms",
                    whatItMeans = "The heart's electrical impulses originate from the sinoatrial node with normal conduction velocities through atria and ventricles.",
                    importantContext = "Heart rate and conduction are within expected resting reference ranges. This does not preclude episodic symptoms.",
                    recommendedAction = "Continue standard continuous clinical monitoring.",
                    riskStatus = EcgRiskStatus.NO_SIGNIFICANT_FINDING
                )
            )
        )
    }
}
