package org.khoyron.bilal.ui.mosquefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swmansion.kmpmaps.core.Coordinates
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.khoyron.bilal.data.local.SessionManager
import org.khoyron.bilal.domain.model.Mosque
import org.khoyron.bilal.domain.repository.MosqueRepository
import org.khoyron.bilal.util.PlatformActions
import kotlin.math.*

class MosqueFinderViewModel(
    private val mosqueRepository: MosqueRepository,
    private val sessionManager: SessionManager,
    private val platformActions: PlatformActions
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MosqueFinderUiState(
            userLocation = if (sessionManager.hasLocation()) {
                Coordinates(sessionManager.getLat() ?: 0.0, sessionManager.getLon() ?: 0.0)
            } else null
        )
    )
    val uiState: StateFlow<MosqueFinderUiState> = _uiState.asStateFlow()

    private val geocoder = Geocoder()

    init {
        // Trigger initial search if session location exists
        _uiState.value.userLocation?.let {
            searchMosquesByLocation(it.latitude, it.longitude)
        }
    }

    fun updateUserLocation(lat: Double, lon: Double) {
        _uiState.update { it.copy(userLocation = Coordinates(lat, lon)) }
    }

    fun searchMosquesByLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Get ISO Country Code and names from Geocoder (very reliable)
            val geocoderResult = geocoder.reverse(lat, lon)
            var countryCode = "ID"
            if (geocoderResult is GeocoderResult.Success) {
                val place = geocoderResult.data.firstOrNull()
                if (place != null) {
                    countryCode = place.isoCountryCode ?: "ID"
                    
                    // Save to session for other features (Azan, Qibla, etc)
                    val cityName = place.locality ?: place.subAdministrativeArea ?: place.administrativeArea ?: "Unknown"
                    val countryName = place.country ?: "Indonesia"
                    sessionManager.saveLocation(cityName, countryName, lat, lon)
                }
            }

            // Fetch mosques using coordinate-based PIP lookup
            val mosqueList = mosqueRepository.getMosquesByLocation(lat, lon, countryCode)
            val nextPrayer = sessionManager.getNextPrayer() ?: "-"
            
            // Process mosques: calculate distance and take top 10
            val processedMosques = mosqueList.map { mosque ->
                val dist = calculateDistance(lat, lon, mosque.latitude, mosque.longitude)
                mosque.copy(
                    distance = "${dist.format(1)} km",
                    nextPrayer = nextPrayer
                )
            }.sortedBy { 
                // We could use the distance string but raw double is better
                calculateDistance(lat, lon, it.latitude, it.longitude)
            }.take(10)

            println("MOSQUE_RESULT: Found ${processedMosques.size} mosques nearby")
            processedMosques.forEachIndexed { index, mosque ->
                println("MOSQUE_RESULT: ${index + 1}. ${mosque.name} (${mosque.distance}) - Lat: ${mosque.latitude}, Lng: ${mosque.longitude}")
            }

            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    mosques = processedMosques,
                    selectedMosque = processedMosques.firstOrNull()
                ) 
            }
        }
    }

    fun searchMosquesByName(query: String) {
        val currentMosques = _uiState.value.mosques
        if (query.isEmpty()) {
            _uiState.value.userLocation?.let { userLoc ->
                searchMosquesByLocation(userLoc.latitude, userLoc.longitude)
            }
            return
        }
        
        val filtered = currentMosques.filter { it.name.contains(query, ignoreCase = true) }
        _uiState.update { it.copy(mosques = filtered, selectedMosque = filtered.firstOrNull()) }
    }

    fun selectMosque(mosque: Mosque) {
        _uiState.update { it.copy(selectedMosque = mosque) }
    }

    fun shareMosque(mosque: Mosque) {
        val googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=${mosque.latitude},${mosque.longitude}"
        val shareText = """
            Masjid: ${mosque.name}
            Jarak: ${mosque.distance}
            Lokasi: $googleMapsUrl
        """.trimIndent()
        platformActions.shareText(shareText)
    }

    fun navigateToMosque(mosque: Mosque) {
        val userLoc = _uiState.value.userLocation
        platformActions.openMaps(
            destLat = mosque.latitude,
            destLon = mosque.longitude,
            label = mosque.name,
            fromLat = userLoc?.latitude,
            fromLon = userLoc?.longitude
        )
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of the earth in km
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2.0) * sin(dLon / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return r * c
    }

    private fun Double.format(digits: Int): String {
        val multiplier = 10.0.pow(digits)
        return (round(this * multiplier) / multiplier).toString()
    }
}

data class MosqueFinderUiState(
    val isLoading: Boolean = true,
    val mosques: List<Mosque> = emptyList(),
    val selectedMosque: Mosque? = null,
    val userLocation: Coordinates? = null,
    val searchQuery: String = ""
)
