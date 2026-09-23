package com.cardioconnect.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.cardioconnect.domain.model.AppSettings
import com.cardioconnect.domain.model.AppThemeMode
import com.cardioconnect.domain.model.EcgGain
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSweepSpeed
import com.cardioconnect.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(context: Context) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("cardioconnect_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            isDemoMode = prefs.getBoolean("key_demo_mode", true), // Default to Demo Mode on fresh install
            autoReconnect = prefs.getBoolean("key_auto_reconnect", true),
            selectedLead = try {
                EcgLead.valueOf(prefs.getString("key_lead", EcgLead.LEAD_II.name) ?: EcgLead.LEAD_II.name)
            } catch (_: Exception) { EcgLead.LEAD_II },
            sweepSpeed = try {
                EcgSweepSpeed.valueOf(prefs.getString("key_sweep_speed", EcgSweepSpeed.SPEED_25_0.name) ?: EcgSweepSpeed.SPEED_25_0.name)
            } catch (_: Exception) { EcgSweepSpeed.SPEED_25_0 },
            gain = try {
                EcgGain.valueOf(prefs.getString("key_gain", EcgGain.GAIN_10.name) ?: EcgGain.GAIN_10.name)
            } catch (_: Exception) { EcgGain.GAIN_10 },
            isGridVisible = prefs.getBoolean("key_grid_visible", true),
            waveformThickness = prefs.getFloat("key_waveform_thickness", 2.5f),
            themeMode = try {
                AppThemeMode.valueOf(prefs.getString("key_theme", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
            } catch (_: Exception) { AppThemeMode.DARK },
            soundAlertsEnabled = prefs.getBoolean("key_sound_alerts", true),
            connectionAlertsEnabled = prefs.getBoolean("key_conn_alerts", true),
            hasCompletedOnboarding = prefs.getBoolean("key_completed_onboarding", false)
        )
    }

    override fun updateDemoMode(enabled: Boolean) {
        prefs.edit().putBoolean("key_demo_mode", enabled).apply()
        _settings.value = _settings.value.copy(isDemoMode = enabled)
    }

    override fun updateAutoReconnect(enabled: Boolean) {
        prefs.edit().putBoolean("key_auto_reconnect", enabled).apply()
        _settings.value = _settings.value.copy(autoReconnect = enabled)
    }

    override fun updateLead(lead: EcgLead) {
        prefs.edit().putString("key_lead", lead.name).apply()
        _settings.value = _settings.value.copy(selectedLead = lead)
    }

    override fun updateSweepSpeed(speed: EcgSweepSpeed) {
        prefs.edit().putString("key_sweep_speed", speed.name).apply()
        _settings.value = _settings.value.copy(sweepSpeed = speed)
    }

    override fun updateGain(gain: EcgGain) {
        prefs.edit().putString("key_gain", gain.name).apply()
        _settings.value = _settings.value.copy(gain = gain)
    }

    override fun updateGridVisible(visible: Boolean) {
        prefs.edit().putBoolean("key_grid_visible", visible).apply()
        _settings.value = _settings.value.copy(isGridVisible = visible)
    }

    override fun updateWaveformThickness(thickness: Float) {
        prefs.edit().putFloat("key_waveform_thickness", thickness).apply()
        _settings.value = _settings.value.copy(waveformThickness = thickness)
    }

    override fun updateThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("key_theme", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    override fun updateSoundAlerts(enabled: Boolean) {
        prefs.edit().putBoolean("key_sound_alerts", enabled).apply()
        _settings.value = _settings.value.copy(soundAlertsEnabled = enabled)
    }

    override fun updateConnectionAlerts(enabled: Boolean) {
        prefs.edit().putBoolean("key_conn_alerts", enabled).apply()
        _settings.value = _settings.value.copy(connectionAlertsEnabled = enabled)
    }

    override fun completeOnboarding() {
        prefs.edit().putBoolean("key_completed_onboarding", true).apply()
        _settings.value = _settings.value.copy(hasCompletedOnboarding = true)
    }
}
