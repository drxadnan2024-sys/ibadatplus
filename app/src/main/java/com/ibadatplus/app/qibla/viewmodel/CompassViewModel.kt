package com.ibadatplus.app.qibla.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibadatplus.app.data.model.CityLocation
import com.ibadatplus.app.data.repository.PrayerTimeRepository
import com.ibadatplus.app.qibla.calculator.QiblaCalculator
import com.ibadatplus.app.qibla.model.CompassAccuracy
import com.ibadatplus.app.qibla.model.QiblaUiState
import com.ibadatplus.app.qibla.sensor.CompassSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class CompassViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private var sensorManager: CompassSensorManager? = null

    // Debounce for vibration so it only pulses once when entering alignment
    private var wasAlignedLast = false
    private var lastVibrationTimestamp = 0L

    init {
        // Initialize with default city until GPS resolves
        val defaultCity = PrayerTimeRepository.DEFAULT_CITY
        applyLocation(
            latitude = defaultCity.latitude,
            longitude = defaultCity.longitude,
            locationName = defaultCity.displayLabel,
            isGps = false,
            city = defaultCity
        )
    }

    /**
     * Start listening to phone orientation sensors
     */
    fun startCompass(context: Context) {
        if (sensorManager == null) {
            sensorManager = CompassSensorManager(
                context = context,
                onAzimuthChanged = { azimuth ->
                    handleAzimuthUpdate(context, azimuth)
                },
                onAccuracyChanged = { accuracy ->
                    _uiState.update { it.copy(accuracy = accuracy) }
                },
                onSensorMissing = {
                    _uiState.update {
                        it.copy(
                            hasCompassSensor = false,
                            errorMessage = "Compass sensors (Rotation Vector / Magnetometer) not detected on this device."
                        )
                    }
                }
            )
        }
        sensorManager?.startListening()
    }

    /**
     * Stop sensor listeners when screen paused/disposed
     */
    fun stopCompass() {
        sensorManager?.stopListening()
    }

    private fun handleAzimuthUpdate(context: Context, azimuth: Float) {
        val qiblaBearing = _uiState.value.qiblaBearing
        val needleAngle = QiblaCalculator.calculateNeedleAngle(azimuth, qiblaBearing)
        val isAligned = QiblaCalculator.isAlignedWithQibla(needleAngle, 3.5f)

        // Haptic feedback pulse when user aligns directly with Qibla
        if (isAligned && !wasAlignedLast) {
            val now = System.currentTimeMillis()
            if (now - lastVibrationTimestamp > 1200L) {
                lastVibrationTimestamp = now
                triggerGentleHaptic(context)
            }
        }
        wasAlignedLast = isAligned

        _uiState.update {
            it.copy(
                deviceAzimuth = azimuth,
                needleAngle = needleAngle,
                isAlignedWithQibla = isAligned
            )
        }
    }

    private fun triggerGentleHaptic(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(45)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Fetch GPS Location using LocationManager
     */
    @SuppressLint("MissingPermission")
    fun fetchGpsLocation(context: Context) {
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

                    val locationName = try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        val address = addresses?.firstOrNull()
                        val locality = address?.locality ?: address?.subAdminArea ?: address?.adminArea
                        if (!locality.isNullOrBlank()) "$locality (GPS)" else "GPS (${String.format(Locale.ENGLISH, "%.2f, %.2f", lat, lon)})"
                    } catch (e: Exception) {
                        "GPS Location"
                    }

                    applyLocation(
                        latitude = lat,
                        longitude = lon,
                        locationName = locationName,
                        isGps = true,
                        city = null
                    )
                } else {
                    // Fallback to current selected city if location not fixed yet
                    val city = _uiState.value.selectedCity ?: PrayerTimeRepository.DEFAULT_CITY
                    applyLocation(
                        latitude = city.latitude,
                        longitude = city.longitude,
                        locationName = city.displayLabel,
                        isGps = false,
                        city = city
                    )
                }
            } catch (e: Exception) {
                val city = _uiState.value.selectedCity ?: PrayerTimeRepository.DEFAULT_CITY
                applyLocation(
                    latitude = city.latitude,
                    longitude = city.longitude,
                    locationName = city.displayLabel,
                    isGps = false,
                    city = city
                )
            }
        }
    }

    /**
     * Select a city manually
     */
    fun selectCityManually(city: CityLocation) {
        applyLocation(
            latitude = city.latitude,
            longitude = city.longitude,
            locationName = city.displayLabel,
            isGps = false,
            city = city
        )
    }

    private fun applyLocation(
        latitude: Double,
        longitude: Double,
        locationName: String,
        isGps: Boolean,
        city: CityLocation?
    ) {
        val qiblaData = QiblaCalculator.calculateQibla(
            userLatitude = latitude,
            userLongitude = longitude,
            locationName = locationName,
            isGpsLocation = isGps
        )

        val currentAzimuth = _uiState.value.deviceAzimuth
        val needleAngle = QiblaCalculator.calculateNeedleAngle(currentAzimuth, qiblaData.qiblaBearing)
        val isAligned = QiblaCalculator.isAlignedWithQibla(needleAngle, 3.5f)

        _uiState.update {
            it.copy(
                qiblaBearing = qiblaData.qiblaBearing,
                distanceToKaabaKm = qiblaData.distanceKm,
                currentLocationName = locationName,
                isGpsActive = isGps,
                selectedCity = city,
                needleAngle = needleAngle,
                isAlignedWithQibla = isAligned,
                isLoading = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCompass()
    }
}
