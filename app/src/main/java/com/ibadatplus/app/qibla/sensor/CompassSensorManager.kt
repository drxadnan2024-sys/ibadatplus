package com.ibadatplus.app.qibla.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ibadatplus.app.qibla.calculator.QiblaCalculator
import com.ibadatplus.app.qibla.model.CompassAccuracy

/**
 * Handles phone rotation vector and magnetic sensors to compute a smooth,
 * jitter-free device azimuth heading (0..360°).
 */
class CompassSensorManager(
    context: Context,
    private val onAzimuthChanged: (azimuth: Float) -> Unit,
    private val onAccuracyChanged: (accuracy: CompassAccuracy) -> Unit,
    private val onSensorMissing: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var magnetometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // For accelerometer + magnetometer fallback
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    private var currentSmoothedAzimuth = 0f
    private val smoothingFactor = 0.25f // Exponential smoothing weight (0..1)

    fun startListening() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometerSensor != null && magnetometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            onSensorMissing()
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var rawAzimuth: Float? = null

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                // orientationAngles[0] is azimuth in radians (-PI..PI)
                val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                rawAzimuth = QiblaCalculator.normalizeDegrees(azimuthDegrees)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                lastAccelerometerSet = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                lastMagnetometerSet = true
            }
        }

        // Compute from accelerometer + magnetometer fallback if rotation vector isn't active
        if (rawAzimuth == null && lastAccelerometerSet && lastMagnetometerSet) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(r, orientationAngles)
                val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                rawAzimuth = QiblaCalculator.normalizeDegrees(azimuthDegrees)
            }
        }

        if (rawAzimuth != null) {
            // Apply angular smoothing to prevent needle shaking/jitter
            val angleDiff = QiblaCalculator.shortestAngleDifference(currentSmoothedAzimuth, rawAzimuth)
            currentSmoothedAzimuth = QiblaCalculator.normalizeDegrees(currentSmoothedAzimuth + angleDiff * smoothingFactor)
            onAzimuthChanged(currentSmoothedAzimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val mappedAccuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassAccuracy.LOW
            SensorManager.SENSOR_STATUS_UNRELIABLE -> CompassAccuracy.UNRELIABLE
            else -> CompassAccuracy.MEDIUM
        }
        onAccuracyChanged(mappedAccuracy)
    }
}
