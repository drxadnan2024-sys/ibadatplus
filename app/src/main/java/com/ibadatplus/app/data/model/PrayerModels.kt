package com.ibadatplus.app.data.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 6 Primary Islamic Daily Prayer and Solar Times
 */
enum class PrayerName(val displayName: String, val arabicName: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء")
}

/**
 * Calculation Methods for Fajr and Isha angles
 */
enum class CalculationMethod(
    val title: String,
    val description: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaFixedMinutesAfterMaghrib: Int? = null
) {
    KARACHI(
        title = "Karachi (UIS)",
        description = "University of Islamic Sciences, Karachi (Fajr 18°, Isha 18°)",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    MWL(
        title = "Muslim World League",
        description = "Muslim World League, Makkah (Fajr 18°, Isha 17°)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    UMM_AL_QURA(
        title = "Umm al-Qura",
        description = "Umm al-Qura University, Makkah (Fajr 18.5°, Isha 90 min after Maghrib)",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaFixedMinutesAfterMaghrib = 90
    ),
    ISNA(
        title = "ISNA",
        description = "Islamic Society of North America (Fajr 15°, Isha 15°)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    )
}

/**
 * Juristic Method (Madhab) for Asr calculation
 * Shafi/Standard: Shadow length equals object length
 * Hanafi: Shadow length equals twice the object length
 */
enum class AsrMadhab(val displayName: String, val shadowMultiplier: Int) {
    HANAFI("Hanafi", 2),
    SHAFI("Shafi / Maliki / Hanbali", 1)
}

/**
 * Single prayer item with its time and status
 */
data class PrayerItem(
    val name: PrayerName,
    val time: LocalTime,
    val jamatTime: LocalTime? = null,
    val isCustomJamat: Boolean = false,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
) {
    val formatted12Hour: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            return time.format(formatter)
        }

    val formattedJamat12Hour: String?
        get() {
            if (jamatTime == null) return null
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            return jamatTime.format(formatter)
        }
}

/**
 * Complete prayer schedule for a day
 */
data class DailyPrayerSchedule(
    val date: LocalDate,
    val hijriDateFormatted: String,
    val englishDateFormatted: String,
    val prayers: List<PrayerItem>,
    val nextPrayer: PrayerItem,
    val previousPrayer: PrayerItem?,
    val calculationMethod: CalculationMethod,
    val asrMadhab: AsrMadhab,
    val locationName: String
)

/**
 * Predefined or custom city with geographic coordinates
 */
data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
) {
    val displayLabel: String
        get() = "$name, $country"
}

/**
 * UI State for the Prayer Times Screen
 */
sealed interface PrayerTimesUiState {
    object Loading : PrayerTimesUiState
    
    data class Success(
        val schedule: DailyPrayerSchedule,
        val nextPrayerRemainingMillis: Long,
        val formattedCountdown: String,
        val progressToNextPrayer: Float,
        val isGpsLocation: Boolean,
        val currentCity: CityLocation,
        val availableCities: List<CityLocation>,
        val currentMethod: CalculationMethod,
        val currentMadhab: AsrMadhab
    ) : PrayerTimesUiState
    
    data class Error(
        val message: String,
        val canFallbackToCity: Boolean = true
    ) : PrayerTimesUiState
}
