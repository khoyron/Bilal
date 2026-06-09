package org.khoyron.bilal.ui.qiblah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kmp.ksensor.sensor.KSensor
import org.kmp.ksensor.sensor.SensorType
import org.kmp.ksensor.sensor.SensorUpdate
import org.kmp.ksensor.sensor.SensorData
import org.kmp.ksensor.permission.PermissionStatus
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// ── UI State ──────────────────────────────────────────────────────────────────

data class QiblahUiState(
    val isLoading: Boolean = true,
    val locationName: String = "Detecting location...",
    val qiblahAngle: Float = 0f,
    val deviceBearing: Float = 0f,
    val directionDescription: String = "",
    val rotationInstruction: String = "",
    val isPermissionGranted: Boolean = false,
    val error: String? = null
)

class QiblahViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QiblahUiState())
    val uiState: StateFlow<QiblahUiState> = _uiState.asStateFlow()

    // Koordinat Kakbah (Mekah)
    private val kaabahLat = 21.4225
    private val kaabahLon = 39.8262

    // Lokasi user terakhir (default Surabaya)
    private var currentLat: Double = -7.2575
    private var currentLon: Double = 112.7521

    // ── Sensor fusion data ────────────────────────────────────────────────────
    // Raw sensor values
    private var magX = 0f; private var magY = 0f; private var magZ = 0f
    private var accX = 0f; private var accY = 0f; private var accZ = 0f

    // Low pass filter smoothed values
    private var smoothMagX = 0f; private var smoothMagY = 0f; private var smoothMagZ = 0f
    private var smoothAccX = 0f; private var smoothAccY = 0f; private var smoothAccZ = 0f

    // Alpha: 0.1 = sangat smooth (lambat), 0.3 = lebih responsif
    private val alpha = 0.15f

    fun startSensors() {
        _uiState.update { it.copy(isPermissionGranted = true, isLoading = true) }

        viewModelScope.launch {
            KSensor.registerSensors(
                types = listOf(
                    SensorType.MAGNETOMETER,
                    SensorType.ACCELEROMETER,
                    SensorType.LOCATION
                ),
                locationIntervalMillis = 3000L
            ).collect { sensorUpdate ->
                when (sensorUpdate) {
                    is SensorUpdate.Data  -> handleSensorData(sensorUpdate.data)
                    is SensorUpdate.Error -> {
                        _uiState.update {
                            it.copy(error = sensorUpdate.toString(), isLoading = false)
                        }
                    }
                }
            }
        }
    }



    private fun handleSensorData(data: SensorData) {
        when (data) {
            is SensorData.Magnetometer -> {
                // Low pass filter untuk magnetometer
                smoothMagX = alpha * data.x + (1 - alpha) * smoothMagX
                smoothMagY = alpha * data.y + (1 - alpha) * smoothMagY
                smoothMagZ = alpha * data.z + (1 - alpha) * smoothMagZ
                magX = smoothMagX; magY = smoothMagY; magZ = smoothMagZ
                calculateFusedBearing()
            }
            is SensorData.Accelerometer -> {
                // Low pass filter untuk accelerometer
                smoothAccX = alpha * data.x + (1 - alpha) * smoothAccX
                smoothAccY = alpha * data.y + (1 - alpha) * smoothAccY
                smoothAccZ = alpha * data.z + (1 - alpha) * smoothAccZ
                accX = smoothAccX; accY = smoothAccY; accZ = smoothAccZ
                calculateFusedBearing()
            }
            is SensorData.Location -> {
                data.latitude?.let { lat ->
                    data.longitude?.let { lon ->
                        val latRounded = (lat * 100).toLong().toDouble() / 100
                        val lonRounded = (lon * 100).toLong().toDouble() / 100
                        currentLat = lat
                        currentLon = lon
                        val qiblahAngle = getQiblahAngle(lat, lon)
                        _uiState.update {
                            it.copy(
                                qiblahAngle  = qiblahAngle.toFloat(),
                                locationName = "${latRounded}°, ${lonRounded}°",
                                isLoading    = false
                            )
                        }
                        updateInstruction(qiblahAngle.toFloat(), _uiState.value.deviceBearing)
                    }
                }
            }
            else -> {}
        }
    }

    private fun updateBearing(bearing: Float) {
        _uiState.update { it.copy(deviceBearing = bearing) }
        updateInstruction(_uiState.value.qiblahAngle, bearing)
    }

    private fun updateInstruction(qiblahAngle: Float, deviceBearing: Float) {
        val diff = ((qiblahAngle - deviceBearing + 360) % 360)
        val roundedDiff = diff.roundToInt()
        val cardinal = getCardinalDirection(qiblahAngle)

        val description = when {
            diff < 10f || diff > 350f ->
                "Your device is pointing towards the Holy Kaaba. ✓"
            else ->
                "Your device is currently facing $cardinal towards the Holy Kaaba."
        }

        val instruction = when {
            diff < 10f || diff > 350f -> "You are facing the Qibla! ✓"
            diff <= 180f              -> "Rotate the phone ${roundedDiff}° to the left"
            else                      -> "Rotate the phone ${360 - roundedDiff}° to the right"
        }

        _uiState.update {
            it.copy(
                directionDescription = description,
                rotationInstruction  = instruction
            )
        }
    }

    fun refreshLocation() {
        stopSensors()
        startSensors()
    }

    fun stopSensors() {
        KSensor.unregisterSensors(
            listOf(SensorType.MAGNETOMETER, SensorType.ACCELEROMETER, SensorType.LOCATION)
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }

    // ── Kalkulasi ─────────────────────────────────────────────────────────────

    /**
     * Sensor fusion: Magnetometer + Accelerometer → bearing akurat
     * meski device miring. Tidak butuh SensorManager (pure commonMain).
     */
    private fun calculateFusedBearing() {
        // Normalisasi accelerometer
        val accNorm = sqrt((accX * accX + accY * accY + accZ * accZ).toDouble()).toFloat()
        if (accNorm == 0f) {
            // Fallback ke magnetometer biasa kalau acc belum ada datanya
            val bearing = calculateMagnetometerBearing(magX, magY)
            updateBearing(bearing)
            return
        }

        val ax = accX / accNorm
        val ay = accY / accNorm
        val az = accZ / accNorm

        // Pitch & roll dari accelerometer
        val pitch = atan2(ax.toDouble(), az.toDouble()).toFloat()
        val roll  = atan2(ay.toDouble(), az.toDouble()).toFloat()

        val cosPitch = cos(pitch.toDouble()).toFloat()
        val sinPitch = sin(pitch.toDouble()).toFloat()
        val cosRoll  = cos(roll.toDouble()).toFloat()
        val sinRoll  = sin(roll.toDouble()).toFloat()

        // Tilt-compensated magnetic field
        val mx = magX * cosPitch + magZ * sinPitch
        val my = magX * sinRoll * sinPitch + magY * cosRoll - magZ * sinRoll * cosPitch

        var bearing = atan2(my.toDouble(), mx.toDouble()) * (180.0 / PI)
        if (bearing < 0) bearing += 360.0

        updateBearing(bearing.toFloat())
    }

    private fun calculateMagnetometerBearing(x: Float, y: Float): Float {
        var bearing = atan2(y.toDouble(), x.toDouble()) * (180.0 / PI)
        if (bearing < 0) bearing += 360.0
        return bearing.toFloat()
    }

    private fun getQiblahAngle(lat: Double, lon: Double): Double {
        val lat1 = lat * (PI / 180.0)
        val lat2 = kaabahLat * (PI / 180.0)
        val dLon = (kaabahLon - lon) * (PI / 180.0)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        var bearing = atan2(y, x) * (180.0 / PI)
        bearing = (bearing + 360) % 360
        return bearing
    }

    private fun getCardinalDirection(angle: Float): String {
        val n = ((angle % 360) + 360) % 360
        return when {
            n < 22.5  -> "North"
            n < 67.5  -> "North-East"
            n < 112.5 -> "East"
            n < 157.5 -> "South-East"
            n < 202.5 -> "South"
            n < 247.5 -> "South-West"
            n < 292.5 -> "West"
            n < 337.5 -> "North-West"
            else      -> "North"
        }
    }
}