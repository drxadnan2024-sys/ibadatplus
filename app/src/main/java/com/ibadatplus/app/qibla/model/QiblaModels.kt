package com.ibadatplus.app.qibla.model

import com.ibadatplus.app.data.model.CityLocation

/**
 * Sensor accuracy levels reported by the hardware
 */
enum class CompassAccuracy {
    HIGH,
    MEDIUM,
    LOW,
    UNRELIABLE,
    NO_SENSOR
}

/**
 * Data calculated for Qibla from current user location
 */
data class QiblaData(
    val qiblaBearing: Float,      // Bearing towards Kaaba in degrees (0..360)
    val distanceKm: Double,       // Distance to Makkah in kilometers
    val userLatitude: Double,
    val userLongitude: Double,
    val locationName: String,
    val isGpsLocation: Boolean
)

/**
 * Comprehensive UI State for the Qibla Finder Screen
 */
data class QiblaUiState(
    val deviceAzimuth: Float = 0f,           // Current phone heading relative to True/Magnetic North (0..360)
    val qiblaBearing: Float = 0f,            // Angle towards Kaaba relative to North (0..360)
    val needleAngle: Float = 0f,              // Relative angle for the arrow: (qiblaBearing - deviceAzimuth)
    val distanceToKaabaKm: Double = 0.0,
    val isAlignedWithQibla: Boolean = false,  // True when phone is pointing directly at Kaaba (within +/- 3°)
    val accuracy: CompassAccuracy = CompassAccuracy.HIGH,
    val hasCompassSensor: Boolean = true,
    val currentLocationName: String = "Fetching location...",
    val isGpsActive: Boolean = false,
    val selectedCity: CityLocation? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
