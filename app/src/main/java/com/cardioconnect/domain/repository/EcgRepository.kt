package com.cardioconnect.domain.repository

import com.cardioconnect.domain.model.EcgAlert
import com.cardioconnect.domain.model.EcgEventMarker
import com.cardioconnect.domain.model.EcgSample
import com.cardioconnect.domain.model.EcgSession
import com.cardioconnect.domain.model.EcgVitals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface EcgRepository {
    val liveSamples: Flow<EcgSample>
    val liveVitals: StateFlow<EcgVitals>
    val alerts: StateFlow<List<EcgAlert>>

    fun startMonitoring()
    fun stopMonitoring()
    fun pauseMonitoring()
    fun resumeMonitoring()
    fun clearWaveform()

    fun startRecording(sessionTitle: String)
    fun markEvent(note: String)
    suspend fun stopRecording(): Long // Returns created session ID
    val isRecording: StateFlow<Boolean>
    val recordingDurationSeconds: StateFlow<Long>

    fun getAllSessions(): Flow<List<EcgSession>>
    suspend fun getSessionById(sessionId: Long): EcgSession?
    suspend fun deleteSession(sessionId: Long)
    suspend fun deleteAllSessions()

    fun dismissAlert(alertId: String)
}
