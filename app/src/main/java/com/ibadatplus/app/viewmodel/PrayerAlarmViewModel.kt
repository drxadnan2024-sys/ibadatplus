package com.ibadatplus.app.viewmodel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibadatplus.app.alarm.PrayerAlarmPreferences
import com.ibadatplus.app.alarm.PrayerAlarmScheduler
import com.ibadatplus.app.alarm.autosilent.AutoSilentManager
import com.ibadatplus.app.alarm.receiver.AlarmReceiver
import com.ibadatplus.app.data.model.AlarmToneType
import com.ibadatplus.app.data.model.AutoSilentConfig
import com.ibadatplus.app.data.model.AutoSilentMode
import com.ibadatplus.app.data.model.BellAlarmState
import com.ibadatplus.app.data.model.PrayerAlarmConfig
import com.ibadatplus.app.data.model.PrayerAlarmSettingsUiState
import com.ibadatplus.app.data.model.PrayerName
import com.ibadatplus.app.data.model.PreFajrAlarmConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrayerAlarmViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerAlarmSettingsUiState())
    val uiState: StateFlow<PrayerAlarmSettingsUiState> = _uiState.asStateFlow()

    fun loadSettings(context: Context) {
        viewModelScope.launch {
            val prefs = PrayerAlarmPreferences(context)
            val configs = prefs.getAllConfigs()

            val nextTimes = configs.mapValues { (prayerName, config) ->
                PrayerAlarmScheduler.getFormattedNextAlarmTime(context, prayerName, config)
            }

            val preFajrConfig = prefs.getPreFajrConfig()
            val nextPreFajrTime = PrayerAlarmScheduler.getFormattedNextPreFajrTime(context, preFajrConfig)
            val autoSilentConfig = prefs.getAutoSilentConfig()

            val exactAlarmGranted = PrayerAlarmScheduler.canScheduleExactAlarms(context)
            val batteryOptimizationIgnored = PrayerAlarmScheduler.isIgnoringBatteryOptimizations(context)
            val dndAccessGranted = AutoSilentManager.isNotificationPolicyAccessGranted(context)

            val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            _uiState.value = PrayerAlarmSettingsUiState(
                configs = configs,
                nextScheduledTimes = nextTimes,
                preFajrConfig = preFajrConfig,
                nextPreFajrTime = nextPreFajrTime,
                autoSilentConfig = autoSilentConfig,
                isDndPolicyAccessGranted = dndAccessGranted,
                isExactAlarmPermissionGranted = exactAlarmGranted,
                isNotificationPermissionGranted = notificationGranted,
                isIgnoringBatteryOptimizations = batteryOptimizationIgnored,
                isLoading = false
            )
        }
    }

    fun toggleAlarm(context: Context, prayerName: PrayerName, enabled: Boolean) {
        val prefs = PrayerAlarmPreferences(context)
        val currentConfig = prefs.getAlarmConfig(prayerName)
        val newConfig = currentConfig.copy(isEnabled = enabled)

        prefs.saveAlarmConfig(newConfig)
        if (enabled) {
            PrayerAlarmScheduler.scheduleAlarm(context, prayerName, newConfig)
        } else {
            PrayerAlarmScheduler.cancelAlarm(context, prayerName)
        }
        loadSettings(context)
    }

    fun cycleBellState(context: Context, prayerName: PrayerName): BellAlarmState {
        val prefs = PrayerAlarmPreferences(context)
        val nextState = prefs.cycleBellState(prayerName)
        val newConfig = prefs.getAlarmConfig(prayerName)

        if (newConfig.isEnabled) {
            PrayerAlarmScheduler.scheduleAlarm(context, prayerName, newConfig)
        } else {
            PrayerAlarmScheduler.cancelAlarm(context, prayerName)
        }
        loadSettings(context)
        return nextState
    }

    fun updateToneType(context: Context, prayerName: PrayerName, toneType: AlarmToneType) {
        val prefs = PrayerAlarmPreferences(context)
        val currentConfig = prefs.getAlarmConfig(prayerName)
        val newConfig = currentConfig.copy(toneType = toneType)

        prefs.saveAlarmConfig(newConfig)
        if (newConfig.isEnabled) {
            PrayerAlarmScheduler.scheduleAlarm(context, prayerName, newConfig)
        }
        loadSettings(context)
    }

    fun updateMinutesBefore(context: Context, prayerName: PrayerName, minutes: Int) {
        val prefs = PrayerAlarmPreferences(context)
        val currentConfig = prefs.getAlarmConfig(prayerName)
        val newConfig = currentConfig.copy(minutesBefore = minutes)

        prefs.saveAlarmConfig(newConfig)
        if (newConfig.isEnabled) {
            PrayerAlarmScheduler.scheduleAlarm(context, prayerName, newConfig)
        }
        loadSettings(context)
    }

    // --- Pre-Fajr Controls ---

    fun togglePreFajrAlarm(context: Context, enabled: Boolean) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getPreFajrConfig()
        val updated = current.copy(isEnabled = enabled)

        prefs.savePreFajrConfig(updated)
        PrayerAlarmScheduler.schedulePreFajrAlarm(context, updated)
        loadSettings(context)
    }

    fun updatePreFajrOffset(context: Context, minutes: Int) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getPreFajrConfig()
        val updated = current.copy(minutesBeforeFajr = minutes)

        prefs.savePreFajrConfig(updated)
        if (updated.isEnabled) {
            PrayerAlarmScheduler.schedulePreFajrAlarm(context, updated)
        }
        loadSettings(context)
    }

    // --- Auto-Silent (Masjid Mode) Controls ---

    fun toggleAutoSilent(context: Context, enabled: Boolean) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getAutoSilentConfig()
        val updated = current.copy(isEnabled = enabled)

        prefs.saveAutoSilentConfig(updated)
        PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
        loadSettings(context)
    }

    fun updateAutoSilentStartMinutes(context: Context, minutes: Int) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getAutoSilentConfig()
        val updated = current.copy(startMinutesBefore = minutes)

        prefs.saveAutoSilentConfig(updated)
        if (updated.isEnabled) {
            PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
        }
        loadSettings(context)
    }

    fun updateAutoSilentDuration(context: Context, durationMinutes: Int) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getAutoSilentConfig()
        val updated = current.copy(durationMinutes = durationMinutes)

        prefs.saveAutoSilentConfig(updated)
        if (updated.isEnabled) {
            PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
        }
        loadSettings(context)
    }

    fun updateAutoSilentUseJamat(context: Context, useJamat: Boolean) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getAutoSilentConfig()
        val updated = current.copy(useJamatTime = useJamat)

        prefs.saveAutoSilentConfig(updated)
        if (updated.isEnabled) {
            PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
        }
        loadSettings(context)
    }

    fun updateAutoSilentMode(context: Context, mode: AutoSilentMode) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getAutoSilentConfig()
        val updated = current.copy(silentMode = mode)

        prefs.saveAutoSilentConfig(updated)
        if (updated.isEnabled) {
            PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
        }
        loadSettings(context)
    }

    fun toggleAutoSilentPrayer(context: Context, prayerName: PrayerName) {
        val prefs = PrayerAlarmPreferences(context)
        val current = prefs.getAutoSilentConfig()
        val updatedPrayers = current.enabledPrayers.toMutableSet()
        if (updatedPrayers.contains(prayerName)) {
            updatedPrayers.remove(prayerName)
        } else {
            updatedPrayers.add(prayerName)
        }
        val updated = current.copy(enabledPrayers = updatedPrayers)

        prefs.saveAutoSilentConfig(updated)
        if (updated.isEnabled) {
            PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
        }
        loadSettings(context)
    }

    fun triggerTestAlarm(context: Context, prayerName: PrayerName) {
        val prefs = PrayerAlarmPreferences(context)
        val config = prefs.getAlarmConfig(prayerName)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(PrayerAlarmScheduler.EXTRA_TONE_TYPE, config.toneType.name)
            putExtra(PrayerAlarmScheduler.EXTRA_MINUTES_BEFORE, 0)
        }
        context.sendBroadcast(intent)
    }

    fun refreshPermissions(context: Context) {
        loadSettings(context)
    }
}
