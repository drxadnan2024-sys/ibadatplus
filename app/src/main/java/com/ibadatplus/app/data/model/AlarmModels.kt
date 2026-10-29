package com.ibadatplus.app.data.model

/**
 * Alert Sound / Vibration mode for each Namaz alarm
 */
enum class AlarmToneType(val displayName: String, val description: String) {
    AZAN("Azan Sound", "Plays full or short Azan audio"),
    NORMAL_BEEP("Notification Tone", "Standard gentle notification chime"),
    VIBRATION_ONLY("Vibration Only", "Silent alert with vibration pulses")
}

/**
 * 3-State Bell Icon for Quick Toggling on Prayer Times Screen
 */
enum class BellAlarmState(val displayName: String) {
    FULL_AZAN("Full Azan"),
    BEEP_NOTIFICATION("Beep / Notification"),
    SILENT_OFF("Silent / Off");

    fun next(): BellAlarmState = when (this) {
        FULL_AZAN -> BEEP_NOTIFICATION
        BEEP_NOTIFICATION -> SILENT_OFF
        SILENT_OFF -> FULL_AZAN
    }
}

/**
 * Individual Prayer Alarm configuration
 */
data class PrayerAlarmConfig(
    val prayerName: PrayerName,
    val isEnabled: Boolean = true,
    val toneType: AlarmToneType = AlarmToneType.AZAN,
    val minutesBefore: Int = 0 // 0 = at exact time, 5, 10, 15, 30 min before
) {
    val displayOffsetLabel: String
        get() = when (minutesBefore) {
            0 -> "At exact time"
            else -> "$minutesBefore min before"
        }

    val bellState: BellAlarmState
        get() = when {
            !isEnabled -> BellAlarmState.SILENT_OFF
            toneType == AlarmToneType.AZAN -> BellAlarmState.FULL_AZAN
            else -> BellAlarmState.BEEP_NOTIFICATION
        }
}

/**
 * Pre-Fajr (Tahajjud / Sehri) Alarm Configuration
 */
data class PreFajrAlarmConfig(
    val isEnabled: Boolean = false,
    val minutesBeforeFajr: Int = 30, // 15, 20, 30, 45 min before Fajr
    val toneType: AlarmToneType = AlarmToneType.NORMAL_BEEP
) {
    val displayLabel: String
        get() = "$minutesBeforeFajr min before Fajr"
}

enum class AutoSilentMode(val displayName: String) {
    VIBRATE("Vibrate Mode (No Special Permission)"),
    DND_SILENT("Do Not Disturb / Pure Silent (Requires DND Access)")
}

/**
 * Auto-Silent (Masjid Mode) during Salah configuration with configurable Silent Window
 */
data class AutoSilentConfig(
    val isEnabled: Boolean = false,
    val startMinutesBefore: Int = 5, // e.g. 0 (at time), 5, 10 min before
    val durationMinutes: Int = 25, // Total duration of silence (e.g. 15, 20, 25, 30, 45 min)
    val useJamatTime: Boolean = true, // Align with local Masjid Jamat time, or Azaan time
    val silentMode: AutoSilentMode = AutoSilentMode.VIBRATE,
    val enabledPrayers: Set<PrayerName> = setOf(
        PrayerName.FAJR,
        PrayerName.DHUHR,
        PrayerName.ASR,
        PrayerName.MAGHRIB,
        PrayerName.ISHA
    )
) {
    val displayWindowSummary: String
        get() {
            val ref = if (useJamatTime) "Jamat" else "Azaan"
            val after = (durationMinutes - startMinutesBefore).coerceAtLeast(0)
            return if (startMinutesBefore > 0) {
                "${startMinutesBefore}m before $ref until ${after}m after (Total ${durationMinutes}m)"
            } else {
                "At $ref time for ${durationMinutes}m"
            }
        }
}

/**
 * UI State for the Prayer Alarm Settings Screen
 */
data class PrayerAlarmSettingsUiState(
    val configs: Map<PrayerName, PrayerAlarmConfig> = emptyMap(),
    val nextScheduledTimes: Map<PrayerName, String> = emptyMap(), // formatted in 12-hour format
    val preFajrConfig: PreFajrAlarmConfig = PreFajrAlarmConfig(),
    val nextPreFajrTime: String = "--",
    val autoSilentConfig: AutoSilentConfig = AutoSilentConfig(),
    val isDndPolicyAccessGranted: Boolean = false,
    val isExactAlarmPermissionGranted: Boolean = true,
    val isNotificationPermissionGranted: Boolean = true,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)

/**
 * Guidance tips for battery optimization across device manufacturers
 */
data class OemBatteryOptimizationGuide(
    val brand: String,
    val steps: String
)
