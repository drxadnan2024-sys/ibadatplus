package com.ibadatplus.app.qibla.calculator

import com.ibadatplus.app.qibla.model.QiblaData
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure mathematical calculator for great-circle Qibla direction and distance to the Holy Kaaba.
 */
object QiblaCalculator {

    // Holy Kaaba Coordinates in Makkah al-Mukarramah
    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262

    // Mean Earth radius in kilometers
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the true forward azimuth (bearing) from user coordinates to the Kaaba
     * using the Great-Circle forward azimuth formula.
     * Returns direction in degrees (0..360, where 0/360 is True North, 90 is East, etc.)
     */
    fun calculateQiblaBearing(userLatitude: Double, userLongitude: Double): Float {
        val userLatRad = Math.toRadians(userLatitude)
        val userLonRad = Math.toRadians(userLongitude)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLonRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLon = kaabaLonRad - userLonRad

        val y = sin(deltaLon) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) -
                sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLon)

        val initialBearingRad = atan2(y, x)
        val initialBearingDeg = Math.toDegrees(initialBearingRad)

        // Normalize to 0..360 degrees
        return normalizeDegrees(initialBearingDeg.toFloat())
    }

    /**
     * Calculates the great-circle surface distance to the Kaaba in kilometers
     * using the Haversine formula.
     */
    fun calculateDistanceToKaabaKm(userLatitude: Double, userLongitude: Double): Double {
        val userLatRad = Math.toRadians(userLatitude)
        val userLonRad = Math.toRadians(userLongitude)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLonRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLat = kaabaLatRad - userLatRad
        val deltaLon = kaabaLonRad - userLonRad

        val a = sin(deltaLat / 2.0) * sin(deltaLat / 2.0) +
                cos(userLatRad) * cos(kaabaLatRad) *
                sin(deltaLon / 2.0) * sin(deltaLon / 2.0)

        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculates complete Qibla data for given user coordinates
     */
    fun calculateQibla(
        userLatitude: Double,
        userLongitude: Double,
        locationName: String,
        isGpsLocation: Boolean
    ): QiblaData {
        val bearing = calculateQiblaBearing(userLatitude, userLongitude)
        val distance = calculateDistanceToKaabaKm(userLatitude, userLongitude)

        return QiblaData(
            qiblaBearing = bearing,
            distanceKm = distance,
            userLatitude = userLatitude,
            userLongitude = userLongitude,
            locationName = locationName,
            isGpsLocation = isGpsLocation
        )
    }

    /**
     * Calculates relative angle for the compass needle to point at Qibla.
     * deviceAzimuth is the heading of the phone (0..360).
     */
    fun calculateNeedleAngle(deviceAzimuth: Float, qiblaBearing: Float): Float {
        return normalizeDegrees(qiblaBearing - deviceAzimuth)
    }

    /**
     * Determines whether the phone is accurately pointing towards Qibla within tolerance (default +/- 3.0 degrees)
     */
    fun isAlignedWithQibla(needleAngle: Float, toleranceDegrees: Float = 3.0f): Boolean {
        val normalized = normalizeDegrees(needleAngle)
        return normalized <= toleranceDegrees || normalized >= (360f - toleranceDegrees)
    }

    /**
     * Normalizes an angle into the 0..360 range
     */
    fun normalizeDegrees(degrees: Float): Float {
        var d = degrees % 360f
        if (d < 0f) d += 360f
        return d
    }

    /**
     * Shortest angular difference between two angles in degrees (-180..180)
     */
    fun shortestAngleDifference(fromDegrees: Float, toDegrees: Float): Float {
        var diff = (toDegrees - fromDegrees) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }
}
