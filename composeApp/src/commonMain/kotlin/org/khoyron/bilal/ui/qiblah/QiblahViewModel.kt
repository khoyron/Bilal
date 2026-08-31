package org.khoyron.bilal.ui.qiblah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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

class QiblahViewModel(
    private val sensorManager: QiblahSensorManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QiblahUiState())
    val uiState: StateFlow<QiblahUiState> = _uiState.asStateFlow()

    private var lastGeocodedLat: Double = 0.0
    private var lastGeocodedLon: Double = 0.0
    private var hasCityName: Boolean = false

    // Koordinat Kakbah (Mekah)
    private val kaabahLat = 21.4225
    private val kaabahLon = 39.8262

    fun startSensors() {
        _uiState.update { it.copy(isPermissionGranted = true, isLoading = true) }

        sensorManager.start(
            onBearingChanged = { bearing ->
                updateBearing(bearing)
            },
            onLocationChanged = { lat, lon ->
                handleLocation(lat, lon)
            },
            onError = { error ->
                _uiState.update { it.copy(error = error, isLoading = false) }
            }
        )
    }

    private fun handleLocation(lat: Double, lon: Double) {
        // Ignore placeholder 0.0, 0.0 coordinates
        if (lat == 0.0 && lon == 0.0) return

        val qiblahAngle = getQiblahAngle(lat, lon)
        
        // Check if we need to update geocoding (moved > 1km or first time)
        val isLocationChanged = abs(lat - lastGeocodedLat) > 0.01 || abs(lon - lastGeocodedLon) > 0.01
        val shouldUpdateGeocode = !hasCityName || isLocationChanged

        _uiState.update {
            it.copy(
                qiblahAngle = qiblahAngle.toFloat(),
                // Only show coordinates if we don't have a city name yet
                locationName = if (!hasCityName) {
                    val formattedLat = (lat * 1000000).roundToInt() / 1000000.0
                    val formattedLon = (lon * 1000000).roundToInt() / 1000000.0
                    "$formattedLat, $formattedLon"
                } else {
                    it.locationName
                },
                isLoading = false
            )
        }
        updateInstruction(qiblahAngle.toFloat(), _uiState.value.deviceBearing)
        
        if (shouldUpdateGeocode) {
            lastGeocodedLat = lat
            lastGeocodedLon = lon
            updateLocationName(lat, lon)
        }
    }

    private fun updateLocationName(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder()
                val result = geocoder.reverse(lat, lon)
                if (result is GeocoderResult.Success) {
                    val place = result.data.firstOrNull()
                    if (place != null) {
                        val city = place.locality ?: place.subAdministrativeArea ?: place.administrativeArea
                        if (city != null) {
                            hasCityName = true
                            _uiState.update { it.copy(locationName = city) }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    private fun updateBearing(bearing: Float) {
        _uiState.update { it.copy(deviceBearing = bearing) }
        updateInstruction(_uiState.value.qiblahAngle, bearing)
    }

    private fun updateInstruction(qiblahAngle: Float, deviceBearing: Float) {
        // Calculate the shortest difference between qiblah and bearing
        var diff = qiblahAngle - deviceBearing
        while (diff < -180) diff += 360
        while (diff > 180) diff -= 360
        
        val roundedDiff = diff.roundToInt().absoluteValue
        
        // Revert to qiblahAngle for cardinal direction as preferred by user
        val cardinal = getCardinalDirection(qiblahAngle)

        // Threshold 10 degrees for a smoother "Facing Qibla" experience
        val isFacingQibla = diff.absoluteValue < 10f

        val description = when {
            isFacingQibla ->
                "Your device is pointing towards the Holy Kaaba. ✓"
            else ->
                "Your device is currently facing $cardinal towards the Holy Kaaba."
        }

        val instruction = when {
            isFacingQibla -> "You are facing the Qibla! ✓"
            diff > 0      -> "Rotate the phone ${roundedDiff}° to the right"
            else          -> "Rotate the phone ${roundedDiff}° to the left"
        }

        _uiState.update {
            it.copy(
                directionDescription = description,
                rotationInstruction  = instruction
            )
        }
    }

    fun refreshLocation() {
        hasCityName = false // Force re-geocoding
        stopSensors()
        startSensors()
    }

    fun stopSensors() {
        sensorManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }

    // ── Kalkulasi ─────────────────────────────────────────────────────────────

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
