package com.cardioconnect.data.repository

import com.cardioconnect.data.ble.BleManager
import com.cardioconnect.data.database.EcgDatabaseHelper
import com.cardioconnect.data.simulator.EcgWaveformSimulator
import com.cardioconnect.domain.model.AlertSeverity
import com.cardioconnect.domain.model.AppSettings
import com.cardioconnect.domain.model.EcgAlert
import com.cardioconnect.domain.model.EcgEventMarker
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSample
import com.cardioconnect.domain.model.EcgSession
import com.cardioconnect.domain.model.EcgVitals
import com.cardioconnect.domain.model.SignalQuality
import com.cardioconnect.domain.repository.EcgRepository
import com.cardioconnect.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.math.max
import kotlin.math.min

class EcgRepositoryImpl(
    private val bleManager: BleManager,
    private val simulator: EcgWaveformSimulator,
    private val dbHelper: EcgDatabaseHelper,
    private val settingsRepository: SettingsRepository
) : EcgRepository {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _liveSamples = MutableSharedFlow<EcgSample>(replay = 0, extraBufferCapacity = 1024)
    override val liveSamples: Flow<EcgSample> = _liveSamples.asSharedFlow()

    private val _liveVitals = MutableStateFlow(EcgVitals())
    override val liveVitals: StateFlow<EcgVitals> = _liveVitals.asStateFlow()

    private val _alerts = MutableStateFlow<List<EcgAlert>>(emptyList())
    override val alerts: StateFlow<List<EcgAlert>> = _alerts.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0L)
    override val recordingDurationSeconds: StateFlow<Long> = _recordingDurationSeconds.asStateFlow()

    private var recordingJob: Job? = null
    private var recordingStartTime = 0L
    private var recordingTitle = ""
    private val recordedSamples = Collections.synchronizedList(mutableListOf<Float>())
    private val recordedMarkers = Collections.synchronizedList(mutableListOf<EcgEventMarker>())
    private val recordedHeartRates = Collections.synchronizedList(mutableListOf<Int>())

    private var simulatorJob: Job? = null
    private var bleJob: Job? = null

    init {
        // Observe demo mode changes
        scope.launch {
            settingsRepository.settings.collect { settings ->
                setupDataSource(settings)
            }
        }

        // Monitoring alerts
        scope.launch {
            liveVitals.collect { vitals ->
                checkVitalsForAlerts(vitals)
            }
        }
    }

    private fun setupDataSource(settings: AppSettings) {
        simulatorJob?.cancel()
        bleJob?.cancel()

        if (settings.isDemoMode) {
            simulator.start()
            simulatorJob = scope.launch {
                launch {
                    simulator.samplesFlow.collect { sample ->
                        _liveSamples.tryEmit(sample)
                        if (_isRecording.value) {
                            recordedSamples.add(sample.voltageMv)
                        }
                    }
                }
                launch {
                    simulator.vitalsFlow.collect { vitals ->
                        _liveVitals.value = vitals
                        if (_isRecording.value) {
                            recordedHeartRates.add(vitals.heartRate)
                        }
                    }
                }
            }
        } else {
            simulator.stop()
            bleJob = scope.launch {
                launch {
                    bleManager.ecgSamplesFlow.collect { sample ->
                        _liveSamples.tryEmit(sample)
                        if (_isRecording.value) {
                            recordedSamples.add(sample.voltageMv)
                        }
                    }
                }
                launch {
                    bleManager.vitalsFlow.collect { vitals ->
                        _liveVitals.value = vitals
                        if (_isRecording.value) {
                            recordedHeartRates.add(vitals.heartRate)
                        }
                    }
                }
            }
        }
    }

    private fun checkVitalsForAlerts(vitals: EcgVitals) {
        val currentAlerts = _alerts.value.toMutableList()

        if (vitals.signalQuality == SignalQuality.ELECTRODE_DISCONNECTED) {
            addAlertIfNotPresent(
                currentAlerts,
                "electrode_disc",
                "Electrode Disconnected",
                "Sensor leads have lost contact with skin.",
                AlertSeverity.WARNING
            )
        } else if (vitals.signalQuality == SignalQuality.NO_SIGNAL) {
            addAlertIfNotPresent(
                currentAlerts,
                "no_signal",
                "ECG Signal Lost",
                "No telemetry received from connected hardware.",
                AlertSeverity.CRITICAL
            )
        }

        vitals.batteryLevelPercentage?.let { bat ->
            if (bat <= 15) {
                addAlertIfNotPresent(
                    currentAlerts,
                    "low_battery",
                    "Device Battery Low",
                    "ECG device battery level is $bat%. Please recharge.",
                    AlertSeverity.WARNING
                )
            }
        }

        _alerts.value = currentAlerts
    }

    private fun addAlertIfNotPresent(
        list: MutableList<EcgAlert>,
        key: String,
        title: String,
        message: String,
        severity: AlertSeverity
    ) {
        if (list.none { it.title == title }) {
            list.add(0, EcgAlert(id = key, title = title, message = message, severity = severity))
        }
    }

    override fun startMonitoring() {
        if (settingsRepository.settings.value.isDemoMode) {
            simulator.start()
        }
    }

    override fun stopMonitoring() {
        simulator.stop()
    }

    override fun pauseMonitoring() {
        simulator.stop()
    }

    override fun resumeMonitoring() {
        startMonitoring()
    }

    override fun clearWaveform() {
        // Triggers UI clear
    }

    override fun startRecording(sessionTitle: String) {
        if (_isRecording.value) return
        recordingTitle = sessionTitle.ifBlank { "ECG Recording ${System.currentTimeMillis() % 10000}" }
        recordingStartTime = System.currentTimeMillis()
        recordedSamples.clear()
        recordedMarkers.clear()
        recordedHeartRates.clear()
        _recordingDurationSeconds.value = 0L
        _isRecording.value = true

        recordingJob?.cancel()
        recordingJob = scope.launch {
            while (isActive && _isRecording.value) {
                delay(1000)
                _recordingDurationSeconds.value = (System.currentTimeMillis() - recordingStartTime) / 1000L
            }
        }
    }

    override fun markEvent(note: String) {
        if (!_isRecording.value) return
        val currentHr = _liveVitals.value.heartRate
        recordedMarkers.add(
            EcgEventMarker(
                timestampMs = System.currentTimeMillis(),
                note = note.ifBlank { "Event Marker" },
                heartRateAtEvent = currentHr
            )
        )
    }

    override suspend fun stopRecording(): Long {
        if (!_isRecording.value) return 0L
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null

        val duration = _recordingDurationSeconds.value.coerceAtLeast(1L)
        val vitals = _liveVitals.value
        val hrList = recordedHeartRates.toList()

        val avgHr = if (hrList.isNotEmpty()) hrList.average().toInt() else vitals.heartRate
        val minHr = if (hrList.isNotEmpty()) hrList.minOrNull() ?: vitals.minHeartRate else vitals.minHeartRate
        val maxHr = if (hrList.isNotEmpty()) hrList.maxOrNull() ?: vitals.maxHeartRate else vitals.maxHeartRate

        // Downsample or copy samples if very large to prevent memory bloat
        val samplesToSave = recordedSamples.toList()

        val session = EcgSession(
            title = recordingTitle,
            startTimeMs = recordingStartTime,
            durationSeconds = duration,
            avgHeartRate = avgHr,
            minHeartRate = minHr,
            maxHeartRate = maxHr,
            lead = settingsRepository.settings.value.selectedLead,
            deviceName = vitals.connectedDeviceName ?: if (vitals.isDemoMode) "Demo Simulator" else "BLE Monitor",
            spo2 = vitals.spo2Percentage,
            bpSystolic = vitals.bloodPressure?.systolic,
            bpDiastolic = vitals.bloodPressure?.diastolic,
            temperature = vitals.temperatureCelsius,
            isDemoData = vitals.isDemoMode,
            eventMarkers = recordedMarkers.toList(),
            samplePoints = samplesToSave
        )

        return dbHelper.insertSession(session)
    }

    override fun getAllSessions(): Flow<List<EcgSession>> {
        return dbHelper.getAllSessionsFlow()
    }

    override suspend fun getSessionById(sessionId: Long): EcgSession? {
        return dbHelper.getSessionById(sessionId)
    }

    override suspend fun deleteSession(sessionId: Long) {
        dbHelper.deleteSession(sessionId)
    }

    override suspend fun deleteAllSessions() {
        dbHelper.deleteAllSessions()
    }

    override fun dismissAlert(alertId: String) {
        _alerts.value = _alerts.value.filterNot { it.id == alertId }
    }
}
