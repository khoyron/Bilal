package org.khoyron.bilal.ui.qiblah

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class AndroidQiblahSensorManager(private val context: Context) : QiblahSensorManager, SensorEventListener, LocationListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    private var onBearingChanged: ((Float) -> Unit)? = null
    private var onLocationChanged: ((Double, Double) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var declination = 0f

    override fun start(
        onBearingChanged: (Float) -> Unit,
        onLocationChanged: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onBearingChanged = onBearingChanged
        this.onLocationChanged = onLocationChanged
        this.onError = onError

        // Start Rotation Vector Sensor
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            onError("Rotation Vector sensor not available")
        }

        // Start Location Updates
        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 0f, this)
            } else if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 0f, this)
            } else {
                onError("Location providers not enabled")
            }
        } catch (e: SecurityException) {
            onError("Location permission not granted: ${e.message}")
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        onBearingChanged = null
        onLocationChanged = null
        onError = null
    }

    // --- SensorEventListener ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            
            // Azimuth is in orientationAngles[0], range [-PI, PI]
            // Convert to degrees [0, 360]
            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            
            // Apply magnetic declination correction to get True North
            azimuth += declination
            
            if (azimuth < 0) azimuth += 360f
            if (azimuth >= 360f) azimuth -= 360f
            
            onBearingChanged?.invoke(azimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // --- LocationListener ---
    override fun onLocationChanged(location: Location) {
        // Ignore suspicious/garbage values
        if (location.latitude == 0.0 && location.longitude == 0.0) return

        val geoField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        declination = geoField.declination
        
        onLocationChanged?.invoke(location.latitude, location.longitude)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
