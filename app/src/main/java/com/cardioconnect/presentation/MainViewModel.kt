package com.cardioconnect.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioconnect.CardioConnectApp
import com.cardioconnect.domain.model.AppSettings
import com.cardioconnect.domain.model.AppThemeMode
import com.cardioconnect.domain.model.BleDeviceModel
import com.cardioconnect.domain.model.EcgAlert
import com.cardioconnect.domain.model.EcgGain
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSample
import com.cardioconnect.domain.model.EcgSession
import com.cardioconnect.domain.model.EcgSweepSpeed
import com.cardioconnect.domain.model.EcgVitals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {

    private val app = CardioConnectApp.instance
    private val ecgRepository = app.ecgRepository
    private val deviceRepository = app.deviceRepository
    private val settingsRepository = app.settingsRepository
    private val pdfExporter = app.pdfExporter
    private val csvExporter = app.csvExporter

    val liveSamples: Flow<EcgSample> = ecgRepository.liveSamples
    val liveVitals: StateFlow<EcgVitals> = ecgRepository.liveVitals
    val alerts: StateFlow<List<EcgAlert>> = ecgRepository.alerts
    val isRecording: StateFlow<Boolean> = ecgRepository.isRecording
    val recordingDurationSeconds: StateFlow<Long> = ecgRepository.recordingDurationSeconds

    val settings: StateFlow<AppSettings> = settingsRepository.settings
    val activePatient: StateFlow<com.cardioconnect.domain.model.PatientProfile> = app.patientRepository.activePatient
    val patientProfile: StateFlow<com.cardioconnect.domain.model.PatientProfile> = app.patientRepository.activePatient
    val allPatients: StateFlow<List<com.cardioconnect.domain.model.PatientProfile>> = app.patientRepository.allPatients
    val intervalMeasurements: StateFlow<com.cardioconnect.domain.model.EcgIntervalMeasurements> = app.patientRepository.intervalMeasurements
    val ecgInterpretation: StateFlow<com.cardioconnect.domain.model.EcgInterpretation> = app.patientRepository.ecgInterpretation

    val isBluetoothEnabled: StateFlow<Boolean> = deviceRepository.isBluetoothEnabled
    val isScanning: StateFlow<Boolean> = deviceRepository.isScanning
    val discoveredDevices: StateFlow<List<BleDeviceModel>> = deviceRepository.discoveredDevices
    val connectedDevice: StateFlow<BleDeviceModel?> = deviceRepository.connectedDevice

    private val _sessions = MutableStateFlow<List<EcgSession>>(emptyList())
    val sessions: StateFlow<List<EcgSession>> = _sessions.asStateFlow()

    private val _selectedSession = MutableStateFlow<EcgSession?>(null)
    val selectedSession: StateFlow<EcgSession?> = _selectedSession.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    init {
        viewModelScope.launch {
            loadSessions()
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _sessions.value = app.dbHelper.getAllSessionsList()
        }
    }

    fun loadSessionDetails(sessionId: Long) {
        viewModelScope.launch {
            _selectedSession.value = ecgRepository.getSessionById(sessionId)
        }
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
        if (_isPaused.value) {
            ecgRepository.pauseMonitoring()
        } else {
            ecgRepository.resumeMonitoring()
        }
    }

    fun clearWaveform() {
        ecgRepository.clearWaveform()
    }

    fun startRecording(title: String = "") {
        ecgRepository.startRecording(title)
    }

    fun stopRecording(onSessionSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = ecgRepository.stopRecording()
            loadSessions()
            onSessionSaved(id)
        }
    }

    fun markEvent(note: String = "Clinical Marker") {
        ecgRepository.markEvent(note)
    }

    fun startScan() = deviceRepository.startScan()
    fun stopScan() = deviceRepository.stopScan()
    fun connectDevice(address: String) = deviceRepository.connectDevice(address)
    fun disconnectDevice() = deviceRepository.disconnectDevice()
    fun reconnectDevice() = deviceRepository.reconnectDevice()

    fun updateDemoMode(enabled: Boolean) = settingsRepository.updateDemoMode(enabled)
    fun updatePatientProfile(updated: com.cardioconnect.domain.model.PatientProfile) = app.patientRepository.updatePatient(updated)
    fun setActivePatient(patientId: String) = app.patientRepository.setActivePatient(patientId)
    fun stopRecordingAndSwitchPatient(patientId: String, onSwitched: () -> Unit = {}) {
        viewModelScope.launch {
            if (isRecording.value) {
                stopRecording {
                    app.patientRepository.setActivePatient(patientId)
                    onSwitched()
                }
            } else {
                app.patientRepository.setActivePatient(patientId)
                onSwitched()
            }
        }
    }
    fun addPatient(patient: com.cardioconnect.domain.model.PatientProfile): Boolean = app.patientRepository.addPatient(patient)
    fun updatePatient(patient: com.cardioconnect.domain.model.PatientProfile) = app.patientRepository.updatePatient(patient)
    fun deletePatient(patientId: String) = app.patientRepository.deletePatient(patientId)
    fun getPatientById(patientId: String): com.cardioconnect.domain.model.PatientProfile? = app.patientRepository.getPatientById(patientId)
    fun getNextSuggestedPatientId(): String = app.patientRepository.getNextSuggestedPatientId()
    fun updateAutoReconnect(enabled: Boolean) = settingsRepository.updateAutoReconnect(enabled)
    fun updateLead(lead: EcgLead) = settingsRepository.updateLead(lead)
    fun updateSweepSpeed(speed: EcgSweepSpeed) = settingsRepository.updateSweepSpeed(speed)
    fun updateGain(gain: EcgGain) = settingsRepository.updateGain(gain)
    fun updateGridVisible(visible: Boolean) = settingsRepository.updateGridVisible(visible)
    fun updateWaveformThickness(thickness: Float) = settingsRepository.updateWaveformThickness(thickness)
    fun updateThemeMode(mode: AppThemeMode) = settingsRepository.updateThemeMode(mode)
    fun updateSoundAlerts(enabled: Boolean) = settingsRepository.updateSoundAlerts(enabled)
    fun updateConnectionAlerts(enabled: Boolean) = settingsRepository.updateConnectionAlerts(enabled)
    fun completeOnboarding() = settingsRepository.completeOnboarding()

    fun dismissAlert(alertId: String) = ecgRepository.dismissAlert(alertId)

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            ecgRepository.deleteSession(sessionId)
            loadSessions()
        }
    }

    fun deleteAllSessions() {
        viewModelScope.launch {
            ecgRepository.deleteAllSessions()
            loadSessions()
        }
    }

    fun exportPdf(session: EcgSession, context: Context) {
        viewModelScope.launch {
            try {
                val pdfFile = pdfExporter.generatePdf(session)
                shareFile(pdfFile, context, "application/pdf", "ECG Report - ${session.title}")
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun exportCsv(session: EcgSession, context: Context) {
        viewModelScope.launch {
            try {
                val csvFile = csvExporter.generateCsv(session)
                shareFile(csvFile, context, "text/csv", "ECG Data - ${session.title}")
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun shareFile(file: File, context: Context, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Exported clinical ECG telemetry from CardioConnect.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share $title"))
    }
}
