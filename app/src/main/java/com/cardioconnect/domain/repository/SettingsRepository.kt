package com.cardioconnect.domain.repository

import com.cardioconnect.domain.model.AppSettings
import com.cardioconnect.domain.model.AppThemeMode
import com.cardioconnect.domain.model.EcgGain
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSweepSpeed
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    fun updateDemoMode(enabled: Boolean)
    fun updateAutoReconnect(enabled: Boolean)
    fun updateLead(lead: EcgLead)
    fun updateSweepSpeed(speed: EcgSweepSpeed)
    fun updateGain(gain: EcgGain)
    fun updateGridVisible(visible: Boolean)
    fun updateWaveformThickness(thickness: Float)
    fun updateThemeMode(mode: AppThemeMode)
    fun updateSoundAlerts(enabled: Boolean)
    fun updateConnectionAlerts(enabled: Boolean)
    fun completeOnboarding()
}
