package com.ibadatplus.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ibadatplus.app.data.model.PrayerName
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Manages custom Masjid Jamat times and default offsets from Azaan for each prayer.
 */
class JamatPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ibadat_jamat_prefs"
        private const val KEY_CUSTOM_PREFIX = "custom_jamat_"
        private const val KEY_CUSTOM_SET_PREFIX = "is_custom_set_"

        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

        // Standard traditional default offsets (in minutes) after Azaan
        val DEFAULT_OFFSETS = mapOf(
            PrayerName.FAJR to 25,
            PrayerName.DHUHR to 20,
            PrayerName.ASR to 20,
            PrayerName.MAGHRIB to 10,
            PrayerName.ISHA to 20
        )
    }

    /**
     * Returns the Jamat time for a prayer.
     * If user has set an exact manual time, returns that.
     * Otherwise, calculates Azaan + default offset.
     */
    fun getJamatTime(prayerName: PrayerName, azaanTime: LocalTime): LocalTime {
        if (prayerName == PrayerName.SUNRISE) return azaanTime

        val savedTimeStr = prefs.getString("$KEY_CUSTOM_PREFIX${prayerName.name}", null)
        if (!savedTimeStr.isNullOrBlank()) {
            try {
                return LocalTime.parse(savedTimeStr, TIME_FORMATTER)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val defaultOffset = DEFAULT_OFFSETS[prayerName] ?: 20
        return azaanTime.plusMinutes(defaultOffset.toLong())
    }

    /**
     * Checks if user has manually adjusted the Jamat time for this prayer
     */
    fun isCustomJamat(prayerName: PrayerName): Boolean {
        return prefs.getBoolean("$KEY_CUSTOM_SET_PREFIX${prayerName.name}", false)
    }

    /**
     * Saves user's manually chosen Jamat time for a prayer
     */
    fun setCustomJamatTime(prayerName: PrayerName, jamatTime: LocalTime) {
        prefs.edit()
            .putString("$KEY_CUSTOM_PREFIX${prayerName.name}", jamatTime.format(TIME_FORMATTER))
            .putBoolean("$KEY_CUSTOM_SET_PREFIX${prayerName.name}", true)
            .apply()
    }

    /**
     * Resets Jamat time back to the automatic offset from Azaan
     */
    fun resetJamatTimeToDefault(prayerName: PrayerName) {
        prefs.edit()
            .remove("$KEY_CUSTOM_PREFIX${prayerName.name}")
            .putBoolean("$KEY_CUSTOM_SET_PREFIX${prayerName.name}", false)
            .apply()
    }

    /**
     * Helper to get default offset minutes from Azaan
     */
    fun getDefaultOffsetMinutes(prayerName: PrayerName): Int {
        return DEFAULT_OFFSETS[prayerName] ?: 20
    }
}
