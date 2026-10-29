package com.ibadatplus.app.alarm

import android.content.Context
import android.content.SharedPreferences
import com.ibadatplus.app.data.model.AlarmToneType
import com.ibadatplus.app.data.model.AutoSilentConfig
import com.ibadatplus.app.data.model.AutoSilentMode
import com.ibadatplus.app.data.model.BellAlarmState
import com.ibadatplus.app.data.model.PrayerAlarmConfig
import com.ibadatplus.app.data.model.PrayerName
import com.ibadatplus.app.data.model.PreFajrAlarmConfig

class PrayerAlarmPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ibadat_prayer_alarms_prefs"
        private const val KEY_ENABLED_PREFIX = "alarm_enabled_"
        private const val KEY_TONE_PREFIX = "alarm_tone_"
        private const val KEY_OFFSET_PREFIX = "alarm_offset_"

        // Pre-Fajr keys
        private const val KEY_PRE_FAJR_ENABLED = "pre_fajr_enabled"
        private const val KEY_PRE_FAJR_OFFSET = "pre_fajr_offset"

        // Auto-Silent (Masjid Mode) keys
        private const val KEY_AUTO_SILENT_ENABLED = "auto_silent_enabled"
        private const val KEY_AUTO_SILENT_START_MINUTES = "auto_silent_start_minutes"
        private const val KEY_AUTO_SILENT_DURATION = "auto_silent_duration"
        private const val KEY_AUTO_SILENT_USE_JAMAT = "auto_silent_use_jamat"
        private const val KEY_AUTO_SILENT_MODE = "auto_silent_mode"
        private const val KEY_AUTO_SILENT_PRAYERS = "auto_silent_prayers"

        val SUPPORTED_PRAYERS = listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )
    }

    fun getAlarmConfig(prayerName: PrayerName): PrayerAlarmConfig {
        val isEnabled = prefs.getBoolean(KEY_ENABLED_PREFIX + prayerName.name, true)
        val toneName = prefs.getString(KEY_TONE_PREFIX + prayerName.name, AlarmToneType.AZAN.name)
        val toneType = try {
            AlarmToneType.valueOf(toneName ?: AlarmToneType.AZAN.name)
        } catch (e: Exception) {
            AlarmToneType.AZAN
        }
        val minutesBefore = prefs.getInt(KEY_OFFSET_PREFIX + prayerName.name, 0)

        return PrayerAlarmConfig(
            prayerName = prayerName,
            isEnabled = isEnabled,
            toneType = toneType,
            minutesBefore = minutesBefore
        )
    }

    fun saveAlarmConfig(config: PrayerAlarmConfig) {
        prefs.edit()
            .putBoolean(KEY_ENABLED_PREFIX + config.prayerName.name, config.isEnabled)
            .putString(KEY_TONE_PREFIX + config.prayerName.name, config.toneType.name)
            .putInt(KEY_OFFSET_PREFIX + config.prayerName.name, config.minutesBefore)
            .apply()
    }

    fun cycleBellState(prayerName: PrayerName): BellAlarmState {
        val current = getAlarmConfig(prayerName)
        val nextState = current.bellState.next()
        val newConfig = when (nextState) {
            BellAlarmState.FULL_AZAN -> current.copy(isEnabled = true, toneType = AlarmToneType.AZAN)
            BellAlarmState.BEEP_NOTIFICATION -> current.copy(isEnabled = true, toneType = AlarmToneType.NORMAL_BEEP)
            BellAlarmState.SILENT_OFF -> current.copy(isEnabled = false)
        }
        saveAlarmConfig(newConfig)
        return nextState
    }

    fun getAllConfigs(): Map<PrayerName, PrayerAlarmConfig> {
        return SUPPORTED_PRAYERS.associateWith { getAlarmConfig(it) }
    }

    // --- Pre-Fajr (Tahajjud / Sehri) ---

    fun getPreFajrConfig(): PreFajrAlarmConfig {
        val isEnabled = prefs.getBoolean(KEY_PRE_FAJR_ENABLED, false)
        val minutes = prefs.getInt(KEY_PRE_FAJR_OFFSET, 30)
        return PreFajrAlarmConfig(isEnabled = isEnabled, minutesBeforeFajr = minutes)
    }

    fun savePreFajrConfig(config: PreFajrAlarmConfig) {
        prefs.edit()
            .putBoolean(KEY_PRE_FAJR_ENABLED, config.isEnabled)
            .putInt(KEY_PRE_FAJR_OFFSET, config.minutesBeforeFajr)
            .apply()
    }

    // --- Auto-Silent (Masjid Mode) ---

    fun getAutoSilentConfig(): AutoSilentConfig {
        val isEnabled = prefs.getBoolean(KEY_AUTO_SILENT_ENABLED, false)
        val startMinutes = prefs.getInt(KEY_AUTO_SILENT_START_MINUTES, 5)
        val duration = prefs.getInt(KEY_AUTO_SILENT_DURATION, 25)
        val useJamat = prefs.getBoolean(KEY_AUTO_SILENT_USE_JAMAT, true)
        val modeName = prefs.getString(KEY_AUTO_SILENT_MODE, AutoSilentMode.VIBRATE.name)
        val mode = try {
            AutoSilentMode.valueOf(modeName ?: AutoSilentMode.VIBRATE.name)
        } catch (e: Exception) {
            AutoSilentMode.VIBRATE
        }

        val prayerSet = prefs.getStringSet(
            KEY_AUTO_SILENT_PRAYERS,
            setOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
        ) ?: setOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")

        val enabledPrayers = prayerSet.mapNotNull {
            try { PrayerName.valueOf(it) } catch (e: Exception) { null }
        }.toSet()

        return AutoSilentConfig(
            isEnabled = isEnabled,
            startMinutesBefore = startMinutes,
            durationMinutes = duration,
            useJamatTime = useJamat,
            silentMode = mode,
            enabledPrayers = if (enabledPrayers.isNotEmpty()) enabledPrayers else SUPPORTED_PRAYERS.toSet()
        )
    }

    fun saveAutoSilentConfig(config: AutoSilentConfig) {
        prefs.edit()
            .putBoolean(KEY_AUTO_SILENT_ENABLED, config.isEnabled)
            .putInt(KEY_AUTO_SILENT_START_MINUTES, config.startMinutesBefore)
            .putInt(KEY_AUTO_SILENT_DURATION, config.durationMinutes)
            .putBoolean(KEY_AUTO_SILENT_USE_JAMAT, config.useJamatTime)
            .putString(KEY_AUTO_SILENT_MODE, config.silentMode.name)
            .putStringSet(KEY_AUTO_SILENT_PRAYERS, config.enabledPrayers.map { it.name }.toSet())
            .apply()
    }
}
