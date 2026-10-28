package com.ibadatplus.app.data.repository

import com.ibadatplus.app.data.model.AsrMadhab
import com.ibadatplus.app.data.model.CalculationMethod
import com.ibadatplus.app.data.model.CityLocation
import com.ibadatplus.app.data.model.DailyPrayerSchedule
import com.ibadatplus.app.data.model.PrayerItem
import com.ibadatplus.app.data.model.PrayerName
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Clean repository responsible for calculating precise Islamic prayer times,
 * providing predefined cities, Hijri date formatting, and configuration defaults.
 */
class PrayerTimeRepository {

    companion object {
        val DEFAULT_METHOD = CalculationMethod.KARACHI
        val DEFAULT_MADHAB = AsrMadhab.HANAFI

        val PREDEFINED_CITIES = listOf(
            CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
            CityLocation("Lahore", "Pakistan", 31.5204, 74.3587, "Asia/Karachi"),
            CityLocation("Islamabad", "Pakistan", 33.6844, 73.0479, "Asia/Karachi"),
            CityLocation("Rawalpindi", "Pakistan", 33.5651, 73.0169, "Asia/Karachi"),
            CityLocation("Peshawar", "Pakistan", 34.0151, 71.5249, "Asia/Karachi"),
            CityLocation("Quetta", "Pakistan", 30.1798, 66.9750, "Asia/Karachi"),
            CityLocation("Multan", "Pakistan", 30.1575, 71.5249, "Asia/Karachi"),
            CityLocation("Faisalabad", "Pakistan", 31.4504, 73.1350, "Asia/Karachi"),
            CityLocation("Delhi", "India", 28.6139, 77.2090, "Asia/Kolkata"),
            CityLocation("Mumbai", "India", 19.0760, 72.8777, "Asia/Kolkata"),
            CityLocation("Hyderabad", "India", 17.3850, 78.4867, "Asia/Kolkata"),
            CityLocation("Lucknow", "India", 26.8467, 80.9462, "Asia/Kolkata"),
            CityLocation("Bengaluru", "India", 12.9716, 77.5946, "Asia/Kolkata"),
            CityLocation("Kolkata", "India", 22.5726, 88.3639, "Asia/Kolkata"),
            CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
            CityLocation("Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
            CityLocation("Abu Dhabi", "UAE", 24.4539, 54.3773, "Asia/Dubai"),
            CityLocation("Makkah", "Saudi Arabia", 21.3891, 39.8579, "Asia/Riyadh"),
            CityLocation("Madinah", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
            CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
            CityLocation("Doha", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
            CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
            CityLocation("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
            CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
            CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
            CityLocation("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto")
        )

        val DEFAULT_CITY = PREDEFINED_CITIES.first() // Karachi
    }

    /**
     * Compute full daily schedule for a given date, coordinates, calculation method, and madhab
     */
    fun getPrayerSchedule(
        date: LocalDate = LocalDate.now(),
        currentTime: LocalTime = LocalTime.now(),
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        locationName: String,
        method: CalculationMethod = DEFAULT_METHOD,
        madhab: AsrMadhab = DEFAULT_MADHAB
    ): DailyPrayerSchedule {
        val zone = ZoneId.of(timeZoneId)
        val zoneOffsetHours = zone.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0

        val rawTimes = calculateAstronomicalPrayerTimes(
            date = date,
            latitude = latitude,
            longitude = longitude,
            timeZoneOffset = zoneOffsetHours,
            method = method,
            madhab = madhab
        )

        val fajrTime = doubleHoursToLocalTime(rawTimes.fajr)
        val sunriseTime = doubleHoursToLocalTime(rawTimes.sunrise)
        val dhuhrTime = doubleHoursToLocalTime(rawTimes.dhuhr)
        val asrTime = doubleHoursToLocalTime(rawTimes.asr)
        val maghribTime = doubleHoursToLocalTime(rawTimes.maghrib)
        val ishaTime = doubleHoursToLocalTime(rawTimes.isha)

        val items = listOf(
            PrayerItem(PrayerName.FAJR, fajrTime),
            PrayerItem(PrayerName.SUNRISE, sunriseTime),
            PrayerItem(PrayerName.DHUHR, dhuhrTime),
            PrayerItem(PrayerName.ASR, asrTime),
            PrayerItem(PrayerName.MAGHRIB, maghribTime),
            PrayerItem(PrayerName.ISHA, ishaTime)
        )

        // Determine next and previous prayer
        val nextIndex = items.indexOfFirst { it.time.isAfter(currentTime) }
        val (nextPrayer, previousPrayer) = if (nextIndex != -1) {
            val next = items[nextIndex]
            val prev = if (nextIndex > 0) items[nextIndex - 1] else null
            next to prev
        } else {
            // All prayers today have passed -> Next is tomorrow's Fajr
            items.first() to items.last()
        }

        val enrichedItems = items.map { item ->
            item.copy(
                isNext = (item.name == nextPrayer.name && nextIndex != -1) || (nextIndex == -1 && item.name == PrayerName.FAJR),
                isPassed = item.time.isBefore(currentTime)
            )
        }

        val englishFormatted = formatEnglishDate(date)
        val hijriFormatted = formatHijriDate(date)

        return DailyPrayerSchedule(
            date = date,
            hijriDateFormatted = hijriFormatted,
            englishDateFormatted = englishFormatted,
            prayers = enrichedItems,
            nextPrayer = nextPrayer,
            previousPrayer = previousPrayer,
            calculationMethod = method,
            asrMadhab = madhab,
            locationName = locationName
        )
    }

    /**
     * Format English Gregorian date: e.g. "Wednesday, 28 Oct 2026"
     */
    fun formatEnglishDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        return date.format(formatter)
    }

    /**
     * Format Islamic Hijri date: e.g. "17 Jumada al-Awwal 1448 AH"
     */
    fun formatHijriDate(date: LocalDate): String {
        return try {
            val hijrahDate = HijrahDate.from(date)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val year = hijrahDate.get(ChronoField.YEAR)

            val monthNames = listOf(
                "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
                "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
                "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
            )
            val monthName = if (month in 1..12) monthNames[month - 1] else "Month $month"
            "$day $monthName $year AH"
        } catch (e: Exception) {
            // Safe fallback
            "Hijri Calendar"
        }
    }

    // --- Astronomical Solar Calculations ---

    private data class RawPrayerTimes(
        val fajr: Double,
        val sunrise: Double,
        val dhuhr: Double,
        val asr: Double,
        val maghrib: Double,
        val isha: Double
    )

    private fun calculateAstronomicalPrayerTimes(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timeZoneOffset: Double,
        method: CalculationMethod,
        madhab: AsrMadhab
    ): RawPrayerTimes {
        val julianDay = getJulianDay(date.year, date.monthValue, date.dayOfMonth)
        val d = julianDay - 2451545.0 // days from J2000.0

        // Mean anomaly of the Sun
        val g = fixAngle(357.529 + 0.98560028 * d)
        // Mean longitude of the Sun
        val q = fixAngle(280.459 + 0.98564736 * d)
        // Apparent longitude of the Sun
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))

        // Obliquity of the ecliptic
        val e = 23.439 - 0.00000036 * d

        // Sun's declination
        val sinDelta = sin(degToRad(e)) * sin(degToRad(l))
        val delta = radToDeg(asin(sinDelta))

        // Equation of Time (in minutes)
        val ra = radToDeg(atan(cos(degToRad(e)) * tan(degToRad(l)))) / 15.0
        val raAdjusted = fixHours(ra + if (l > 90 && l < 270) 12.0 else if (l >= 270) 24.0 else 0.0)
        val equationOfTime = (q / 15.0 - raAdjusted) * 60.0

        // Solar Noon (Dhuhr)
        val solarNoon = fixHours(12.0 + timeZoneOffset - (longitude / 15.0) - (equationOfTime / 60.0))

        // Sunrise & Sunset Angle: zenith = 90.833° (accounting for refraction and sun diameter)
        val sunZenith = 90.8333
        val sunriseHourAngle = calculateHourAngle(latitude, delta, sunZenith)
        val sunrise = fixHours(solarNoon - sunriseHourAngle)
        val sunset = fixHours(solarNoon + sunriseHourAngle)

        // Fajr
        val fajrZenith = 90.0 + method.fajrAngle
        val fajrHourAngle = calculateHourAngle(latitude, delta, fajrZenith)
        val fajr = fixHours(solarNoon - fajrHourAngle)

        // Asr: shadow length = object + multiplier * noon shadow
        val shadowRatio = madhab.shadowMultiplier.toDouble()
        val noonAngle = abs(latitude - delta)
        val asrAltitude = radToDeg(atan(1.0 / (shadowRatio + tan(degToRad(noonAngle)))))
        val asrZenith = 90.0 - asrAltitude
        val asrHourAngle = calculateHourAngle(latitude, delta, asrZenith)
        val asr = fixHours(solarNoon + asrHourAngle)

        // Maghrib: same as sunset
        val maghrib = sunset

        // Isha
        val isha = if (method.ishaFixedMinutesAfterMaghrib != null) {
            fixHours(maghrib + method.ishaFixedMinutesAfterMaghrib / 60.0)
        } else {
            val ishaZenith = 90.0 + method.ishaAngle
            val ishaHourAngle = calculateHourAngle(latitude, delta, ishaZenith)
            fixHours(solarNoon + ishaHourAngle)
        }

        return RawPrayerTimes(
            fajr = fajr,
            sunrise = sunrise,
            dhuhr = solarNoon,
            asr = asr,
            maghrib = maghrib,
            isha = isha
        )
    }

    private fun calculateHourAngle(lat: Double, dec: Double, zenith: Double): Double {
        val cosH = (cos(degToRad(zenith)) - sin(degToRad(lat)) * sin(degToRad(dec))) /
                (cos(degToRad(lat)) * cos(degToRad(dec)))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return radToDeg(acos(clampedCosH)) / 15.0
    }

    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun doubleHoursToLocalTime(hours: Double): LocalTime {
        val normalized = fixHours(hours)
        val totalSeconds = (normalized * 3600.0).toInt()
        val h = (totalSeconds / 3600) % 24
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return LocalTime.of(h, m, s)
    }

    private fun degToRad(deg: Double): Double = deg * Math.PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / Math.PI

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHours(hours: Double): Double {
        var h = hours % 24.0
        if (h < 0) h += 24.0
        return h
    }
}
