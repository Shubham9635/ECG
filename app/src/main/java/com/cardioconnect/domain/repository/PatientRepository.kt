package com.cardioconnect.domain.repository

import com.cardioconnect.domain.model.EcgIntervalMeasurements
import com.cardioconnect.domain.model.EcgInterpretation
import com.cardioconnect.domain.model.PatientProfile
import kotlinx.coroutines.flow.StateFlow

interface PatientRepository {
    val activePatient: StateFlow<PatientProfile>
    val allPatients: StateFlow<List<PatientProfile>>

    val intervalMeasurements: StateFlow<EcgIntervalMeasurements>
    val ecgInterpretation: StateFlow<EcgInterpretation>

    fun setActivePatient(patientId: String)
    fun addPatient(patient: PatientProfile): Boolean
    fun updatePatient(patient: PatientProfile)
    fun deletePatient(patientId: String)
    fun getPatientById(patientId: String): PatientProfile?
    fun getNextSuggestedPatientId(): String

    fun updateMeasurements(measurements: EcgIntervalMeasurements)
    fun updateInterpretation(interpretation: EcgInterpretation)
}
