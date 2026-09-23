package com.cardioconnect.data.simulator

import com.cardioconnect.domain.model.BloodPressure
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSample
import com.cardioconnect.domain.model.EcgVitals
import com.cardioconnect.domain.model.SignalQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-fidelity synthetic ECG waveform generator.
 * Accurately models electrophysiological P-Q-R-S-T waveforms,
 * respiratory baseline wander, and subtle muscle tremor artifacts.
 */
class EcgWaveformSimulator {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var simulationJob: Job? = null

    private val _samplesFlow = MutableSharedFlow<EcgSample>(replay = 0, extraBufferCapacity = 512)
    val samplesFlow: SharedFlow<EcgSample> = _samplesFlow.asSharedFlow()

    private val _vitalsFlow = MutableStateFlow(
        EcgVitals(
            heartRate = 76,
            avgHeartRate = 75,
            minHeartRate = 68,
            maxHeartRate = 84,
            signalQuality = SignalQuality.GOOD,
            rhythmClassification = "Demo Rhythm: Normal Sinus Pattern",
            spo2Percentage = 98,
            bloodPressure = BloodPressure(120, 80),
            temperatureCelsius = 36.7f,
            batteryLevelPercentage = 94,
            rssiDbm = -54,
            isBleConnected = false,
            isDemoMode = true,
            connectedDeviceName = "Simulated CardioConnect Pro"
        )
    )
    val vitalsFlow: StateFlow<EcgVitals> = _vitalsFlow.asStateFlow()

    private var isRunning = false
    private var baseBpm = 75.0
    private var sampleRateHz = 250 // 250 samples/second standard clinical sampling
    private var sampleIndex = 0L

    fun start() {
        if (isRunning) return
        isRunning = true

        simulationJob = scope.launch {
            val intervalMs = 1000L / sampleRateHz // 4ms per sample
            var cardiacPhase = 0.0 // 0.0 to 1.0 representing one cardiac cycle (R-R interval)
            var respirationPhase = 0.0
            var cycleCount = 0

            var minHr = 72
            var maxHr = 78
            var runningHrTotal = 0L
            var hrUpdateCount = 0L

            while (isActive && isRunning) {
                val cycleDurationSec = 60.0 / baseBpm
                val phaseIncrement = (1.0 / sampleRateHz) / cycleDurationSec

                cardiacPhase += phaseIncrement
                if (cardiacPhase >= 1.0) {
                    cardiacPhase -= 1.0
                    cycleCount++

                    // Mild physiological heart rate variability (Respiratory Sinus Arrhythmia)
                    val rsaVariation = 3.0 * sin(respirationPhase)
                    val currentHr = (baseBpm + rsaVariation + (Math.random() - 0.5) * 1.5).toInt()

                    minHr = minOf(minHr, currentHr)
                    maxHr = maxOf(maxHr, currentHr)
                    runningHrTotal += currentHr
                    hrUpdateCount++

                    val avgHr = (runningHrTotal / hrUpdateCount).toInt()

                    // Periodically update vitals state
                    val currentVitals = _vitalsFlow.value
                    _vitalsFlow.value = currentVitals.copy(
                        heartRate = currentHr,
                        avgHeartRate = avgHr,
                        minHeartRate = minHr,
                        maxHeartRate = maxHr,
                        connectionDurationSeconds = currentVitals.connectionDurationSeconds + 1
                    )
                }

                respirationPhase += (2 * PI * 0.25) / sampleRateHz // ~15 breaths/min baseline drift
                val baselineWander = (0.04 * sin(respirationPhase)).toFloat()
                val muscleNoise = ((Math.random() - 0.5) * 0.015).toFloat()

                // Calculate P-Q-R-S-T voltage
                val ecgMv = calculateSyntheticEcgVoltage(cardiacPhase) + baselineWander + muscleNoise

                val now = System.currentTimeMillis()
                _samplesFlow.tryEmit(
                    EcgSample(
                        timestampMs = now,
                        voltageMv = ecgMv,
                        lead = EcgLead.LEAD_II,
                        sampleIndex = sampleIndex++
                    )
                )

                delay(intervalMs)
            }
        }
    }

    fun stop() {
        isRunning = false
        simulationJob?.cancel()
        simulationJob = null
    }

    /**
     * Mathematical model of cardiac lead II potential.
     * Uses sum of Gaussian curves for each characteristic wave.
     */
    private fun calculateSyntheticEcgVoltage(phase: Double): Float {
        // Gaussian component: amp * exp(-((phase - center)^2) / (2 * width^2))
        // Phase ranges from 0.0 to 1.0:
        // P wave: center ~0.20
        // Q wave: center ~0.35
        // R peak: center ~0.38
        // S wave: center ~0.41
        // T wave: center ~0.65

        fun gaussian(amp: Double, center: Double, width: Double): Double {
            val diff = phase - center
            return amp * exp(-(diff * diff) / (2.0 * width * width))
        }

        val pWave = gaussian(amp = 0.16, center = 0.20, width = 0.028)
        val qWave = gaussian(amp = -0.18, center = 0.35, width = 0.010)
        val rWave = gaussian(amp = 1.35, center = 0.38, width = 0.012)
        val sWave = gaussian(amp = -0.32, center = 0.41, width = 0.013)
        val tWave = gaussian(amp = 0.28, center = 0.65, width = 0.048)

        // Baseline U-wave or tiny atrial repolarization
        val uWave = gaussian(amp = 0.03, center = 0.78, width = 0.030)

        return (pWave + qWave + rWave + sWave + tWave + uWave).toFloat()
    }
}
