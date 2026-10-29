package com.ibadatplus.app.viewmodel
import com.ibadatplus.app.data.model.PrayerName
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibadatplus.app.data.model.AsrMadhab
import com.ibadatplus.app.data.model.CalculationMethod
import com.ibadatplus.app.data.model.CityLocation
import com.ibadatplus.app.data.model.DailyPrayerSchedule
import com.ibadatplus.app.data.model.PrayerItem
import com.ibadatplus.app.data.model.PrayerTimesUiState
import com.ibadatplus.app.data.preferences.CityPreferences
import com.ibadatplus.app.data.preferences.JamatPreferences
import com.ibadatplus.app.data.repository.PrayerTimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

class PrayerTimeViewModel(
    application: Application,
    private val repository: PrayerTimeRepository = PrayerTimeRepository()
) : AndroidViewModel(application) {

    private var cityPreferences = CityPreferences(application)
    private var jamatPreferences = JamatPreferences(application)

    private val _uiState = MutableStateFlow<PrayerTimesUiState>(PrayerTimesUiState.Loading)
    val uiState: StateFlow<PrayerTimesUiState> = _uiState.asStateFlow()

    // Current settings loaded from persistent SharedPreferences
    private var currentMethod: CalculationMethod = cityPreferences.getSavedCalculationMethod()
    private var currentMadhab: AsrMadhab = cityPreferences.getSavedAsrMadhab()
    private var selectedCity: CityLocation = cityPreferences.getSavedCity()
    private var isUsingGpsLocation: Boolean = false
    private var gpsCoordinates: Pair<Double, Double>? = null
    private var gpsLocationName: String = "GPS Location"

    // Background countdown job
    private var tickerJob: Job? = null

    init {
        // Initialize with saved city (e.g., Mumbai) and saved preferences
        loadPrayerTimes()
        startCountdownTicker()
    }

    /**
     * Reloads preferences when user logs in, out, or switches accounts for multi-user isolation
     */
    fun reloadForUser() {
        val app = getApplication<Application>()
        cityPreferences = CityPreferences(app)
        jamatPreferences = JamatPreferences(app)
        currentMethod = cityPreferences.getSavedCalculationMethod()
        currentMadhab = cityPreferences.getSavedAsrMadhab()
        selectedCity = cityPreferences.getSavedCity()
        isUsingGpsLocation = false
        gpsCoordinates = null
        loadPrayerTimes()
    }

    /**
     * Recompute prayer schedule based on current coordinates and preferences
     */
    fun loadPrayerTimes() {
        val now = LocalTime.now()
        val today = LocalDate.now()

        val (lat, lon, zoneId, locationDisplayName) = if (isUsingGpsLocation && gpsCoordinates != null) {
            val coords = gpsCoordinates!!
            Quadruple(coords.first, coords.second, ZoneId.systemDefault().id, gpsLocationName)
        } else {
            Quadruple(selectedCity.latitude, selectedCity.longitude, selectedCity.timeZoneId, selectedCity.displayLabel)
        }

        try {
            val rawSchedule = repository.getPrayerSchedule(
                date = today,
                currentTime = now,
                latitude = lat,
                longitude = lon,
                timeZoneId = zoneId,
                locationName = locationDisplayName,
                method = currentMethod,
                madhab = currentMadhab
            )

            // Enrich prayers with local Masjid Jamat times
            val enrichedPrayers = rawSchedule.prayers.map { item ->
                if (item.name == PrayerName.SUNRISE) {
                    item
                } else {
                    val jTime = jamatPreferences.getJamatTime(item.name, item.time)
                    val isCustom = jamatPreferences.isCustomJamat(item.name)
                    item.copy(jamatTime = jTime, isCustomJamat = isCustom)
                }
            }
            val schedule = rawSchedule.copy(prayers = enrichedPrayers)

            val (remainingMillis, progress, formattedCountdown) = calculateCountdown(schedule)

            _uiState.value = PrayerTimesUiState.Success(
                schedule = schedule,
                nextPrayerRemainingMillis = remainingMillis,
                formattedCountdown = formattedCountdown,
                progressToNextPrayer = progress,
                isGpsLocation = isUsingGpsLocation,
                currentCity = selectedCity,
                availableCities = PrayerTimeRepository.PREDEFINED_CITIES,
                currentMethod = currentMethod,
                currentMadhab = currentMadhab
            )
        } catch (e: Exception) {
            _uiState.value = PrayerTimesUiState.Error(
                message = e.localizedMessage ?: "Failed to calculate prayer times",
                canFallbackToCity = true
            )
        }
    }

    /**
     * Start continuous 1-second countdown loop
     */
    private fun startCountdownTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val currentState = _uiState.value
                if (currentState is PrayerTimesUiState.Success) {
                    val (remainingMillis, progress, formattedCountdown) = calculateCountdown(currentState.schedule)
                    
                    // If remaining time reached 0, refresh entire schedule for next prayer
                    if (remainingMillis <= 0) {
                        loadPrayerTimes()
                    } else {
                        _uiState.value = currentState.copy(
                            nextPrayerRemainingMillis = remainingMillis,
                            progressToNextPrayer = progress,
                            formattedCountdown = formattedCountdown
                        )
                    }
                }
            }
        }
    }

    /**
     * Calculate remaining time in millis, progress percentage (0.0 to 1.0), and countdown text
     */
    private fun calculateCountdown(schedule: DailyPrayerSchedule): Triple<Long, Float, String> {
        val now = LocalDateTime.now()
        val nextTime = schedule.nextPrayer.time
        
        // If next prayer time is earlier than current time today, it must be tomorrow's Fajr
        val nextDateTime = if (nextTime.isBefore(now.toLocalTime())) {
            schedule.date.plusDays(1).atTime(nextTime)
        } else {
            schedule.date.atTime(nextTime)
        }

        val remainingMillis = Duration.between(now, nextDateTime).toMillis().coerceAtLeast(0L)

        val totalDurationMillis = if (schedule.previousPrayer != null) {
            val prevTime = schedule.previousPrayer.time
            val prevDateTime = if (prevTime.isAfter(nextTime)) {
                schedule.date.atTime(prevTime)
            } else {
                schedule.date.atTime(prevTime)
            }
            Duration.between(prevDateTime, nextDateTime).toMillis().coerceAtLeast(1L)
        } else {
            // Default 6 hours interval
            6 * 3600 * 1000L
        }

        val elapsedMillis = totalDurationMillis - remainingMillis
        val progress = (elapsedMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)

        val hours = (remainingMillis / (1000 * 3600)) % 24
        val minutes = (remainingMillis / (1000 * 60)) % 60
        val seconds = (remainingMillis / 1000) % 60

        val formatted = if (hours > 0) {
            String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
        }

        return Triple(remainingMillis, progress, formatted)
    }

    /**
     * Fetch GPS Location using LocationManager with Geocoder fallback
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocationAndTimes(context: Context) {
        viewModelScope.launch {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

                var bestLocation: Location? = null

                if (isGpsEnabled) {
                    bestLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                if (bestLocation == null && isNetworkEnabled) {
                    bestLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (bestLocation != null) {
                    val lat = bestLocation.latitude
                    val lon = bestLocation.longitude
                    gpsCoordinates = lat to lon
                    isUsingGpsLocation = true
                    cityPreferences.setUsingGps(true)

                    // Reverse geocode to get city name
                    gpsLocationName = try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        val address = addresses?.firstOrNull()
                        val locality = address?.locality ?: address?.subAdminArea ?: address?.adminArea
                        if (!locality.isNullOrBlank()) {
                            "$locality (GPS)"
                        } else {
                            "GPS (${String.format(Locale.ENGLISH, "%.2f, %.2f", lat, lon)})"
                        }
                    } catch (e: Exception) {
                        "Current Location (GPS)"
                    }

                    loadPrayerTimes()
                } else {
                    // Fallback to manual city if GPS location fix wasn't ready
                    isUsingGpsLocation = false
                    loadPrayerTimes()
                }
            } catch (e: Exception) {
                isUsingGpsLocation = false
                loadPrayerTimes()
            }
        }
    }

    /**
     * Called when user denies location permission
     */
    fun onLocationPermissionDenied() {
        isUsingGpsLocation = false
        loadPrayerTimes()
    }

    /**
     * Checks if user has manually chosen a city previously
     */
    fun isManualCitySaved(): Boolean = cityPreferences.isManualCitySelected()

    /**
     * Manually select a city from the list and persist to SharedPreferences
     */
    fun selectCity(city: CityLocation) {
        selectedCity = city
        isUsingGpsLocation = false
        gpsCoordinates = null
        cityPreferences.saveSelectedCity(city)
        loadPrayerTimes()
    }

    /**
     * Change calculation method (Karachi, MWL, Umm al-Qura, ISNA) and persist
     */
    fun updateCalculationMethod(method: CalculationMethod) {
        currentMethod = method
        cityPreferences.saveCalculationMethod(method)
        loadPrayerTimes()
    }

    /**
     * Change Asr Madhab (Hanafi, Shafi) and persist
     */
    fun updateAsrMadhab(madhab: AsrMadhab) {
        currentMadhab = madhab
        cityPreferences.saveAsrMadhab(madhab)
        loadPrayerTimes()
    }

    /**
     * Set a custom Jamat time for a prayer (e.g. Fajr at 05:45 AM)
     */
    fun saveCustomJamatTime(prayerName: PrayerName, time: LocalTime) {
        jamatPreferences.setCustomJamatTime(prayerName, time)
        loadPrayerTimes()
    }

    /**
     * Reset Jamat time back to standard offset
     */
    fun resetJamatTimeToDefault(prayerName: PrayerName) {
        jamatPreferences.resetJamatTimeToDefault(prayerName)
        loadPrayerTimes()
    }

    /**
     * Returns default offset minutes from Azaan for a prayer
     */
    fun getDefaultJamatOffset(prayerName: PrayerName): Int {
        return jamatPreferences.getDefaultOffsetMinutes(prayerName)
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
