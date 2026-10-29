package com.ibadatplus.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ibadatplus.app.auth.AuthManager
import com.ibadatplus.app.data.model.AsrMadhab
import com.ibadatplus.app.data.model.CalculationMethod
import com.ibadatplus.app.data.model.CityLocation
import com.ibadatplus.app.data.repository.PrayerTimeRepository

/**
 * Persists user's selected city, location coordinates, calculation method,
 * and Asr Madhab across app restarts using SharedPreferences.
 * Scoped by authenticated User UID for multi-user isolation.
 */
class CityPreferences(
    context: Context,
    userId: String = AuthManager.getCurrentUserId()
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("${PREFS_NAME}_$userId", Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ibadat_city_prefs"

        private const val KEY_CITY_NAME = "key_city_name"
        private const val KEY_CITY_COUNTRY = "key_city_country"
        private const val KEY_CITY_LAT = "key_city_lat"
        private const val KEY_CITY_LON = "key_city_lon"
        private const val KEY_CITY_TIMEZONE = "key_city_timezone"
        private const val KEY_IS_MANUAL_SELECTED = "key_is_manual_selected"

        private const val KEY_CALC_METHOD = "key_calc_method"
        private const val KEY_ASR_MADHAB = "key_asr_madhab"
    }

    /**
     * Saves user selected city (e.g., Mumbai, Delhi, Karachi)
     */
    fun saveSelectedCity(city: CityLocation) {
        prefs.edit()
            .putString(KEY_CITY_NAME, city.name)
            .putString(KEY_CITY_COUNTRY, city.country)
            .putString(KEY_CITY_LAT, city.latitude.toString())
            .putString(KEY_CITY_LON, city.longitude.toString())
            .putString(KEY_CITY_TIMEZONE, city.timeZoneId)
            .putBoolean(KEY_IS_MANUAL_SELECTED, true)
            .apply()
    }

    /**
     * Returns saved city, or defaults to Karachi if none saved yet
     */
    fun getSavedCity(): CityLocation {
        val name = prefs.getString(KEY_CITY_NAME, null) ?: return PrayerTimeRepository.DEFAULT_CITY
        val country = prefs.getString(KEY_CITY_COUNTRY, "") ?: ""
        val latStr = prefs.getString(KEY_CITY_LAT, null)
        val lonStr = prefs.getString(KEY_CITY_LON, null)
        val timeZone = prefs.getString(KEY_CITY_TIMEZONE, "Asia/Karachi") ?: "Asia/Karachi"

        val lat = latStr?.toDoubleOrNull() ?: PrayerTimeRepository.DEFAULT_CITY.latitude
        val lon = lonStr?.toDoubleOrNull() ?: PrayerTimeRepository.DEFAULT_CITY.longitude

        return CityLocation(
            name = name,
            country = country,
            latitude = lat,
            longitude = lon,
            timeZoneId = timeZone
        )
    }

    /**
     * True if user manually chose a city, meaning auto-GPS should not overwrite on launch
     */
    fun isManualCitySelected(): Boolean {
        return prefs.getBoolean(KEY_IS_MANUAL_SELECTED, false)
    }

    /**
     * Resets manual selection flag when user explicitly opts to use live GPS
     */
    fun setUsingGps(usingGps: Boolean) {
        prefs.edit()
            .putBoolean(KEY_IS_MANUAL_SELECTED, !usingGps)
            .apply()
    }

    /**
     * Calculation Method persistence
     */
    fun saveCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString(KEY_CALC_METHOD, method.name).apply()
    }

    fun getSavedCalculationMethod(): CalculationMethod {
        val name = prefs.getString(KEY_CALC_METHOD, null) ?: return PrayerTimeRepository.DEFAULT_METHOD
        return try {
            CalculationMethod.valueOf(name)
        } catch (e: Exception) {
            PrayerTimeRepository.DEFAULT_METHOD
        }
    }

    /**
     * Asr Madhab persistence
     */
    fun saveAsrMadhab(madhab: AsrMadhab) {
        prefs.edit().putString(KEY_ASR_MADHAB, madhab.name).apply()
    }

    fun getSavedAsrMadhab(): AsrMadhab {
        val name = prefs.getString(KEY_ASR_MADHAB, null) ?: return PrayerTimeRepository.DEFAULT_MADHAB
        return try {
            AsrMadhab.valueOf(name)
        } catch (e: Exception) {
            PrayerTimeRepository.DEFAULT_MADHAB
        }
    }
}
