package org.khoyron.bilal.ui.azan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.compass.Priority
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.LocationRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.khoyron.bilal.data.local.SessionManager
import org.khoyron.bilal.domain.usecase.GetAzanTimesUseCase
import org.khoyron.bilal.model.PrayerTimeUi
import kotlin.math.abs
import kotlin.time.Clock

// ── UI State ──────────────────────────────────────────────────────────────────
data class AzanUiState(
    val isLoading: Boolean = true,
    val prayerList: List<PrayerTimeUi> = emptyList(),
    val nextPrayer: PrayerTimeUi? = null,
    val countdown: String = "00:00:00",
    val prayerProgress: Float = 0f,
    val alertEnabled: Boolean = false,
    val todayDate: String = "",
    val cityName: String = "Surabaya",
    val countryName: String = "Indonesia",
    val timezone: String = "Asia/Jakarta",
    val error: String? = null
)

class AzanViewModel(
    private val getAzanTimesUseCase: GetAzanTimesUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AzanUiState())
    val uiState: StateFlow<AzanUiState> = _uiState.asStateFlow()

    init {
        checkSessionAndLoad()
        updateLocationInfo()
        startCountdownTicker()
    }

    // ── Location & Timezone ───────────────────────────────────────────────────

    private fun checkSessionAndLoad() {
        if (sessionManager.hasLocation()) {
            val city = sessionManager.getCity() ?: "Unknown"
            val country = sessionManager.getCountry() ?: "Unknown"
            val lat = sessionManager.getLat() ?: -7.2575
            val lon = sessionManager.getLon() ?: 112.7521

            _uiState.update { 
                it.copy(
                    cityName = city,
                    countryName = country,
                    isLoading = true // Show loading while fetching API
                )
            }
            loadPrayerTimes(lat, lon)
        }
    }

    private fun updateLocationInfo() {
        val currentTimeZone = TimeZone.currentSystemDefault()
        _uiState.update { it.copy(timezone = currentTimeZone.id) }

        viewModelScope.launch {
            try {
                val geolocator = Geolocator()
                val locationResult = geolocator.current(LocationRequest(Priority.HighAccuracy))
                
                if (locationResult is GeolocatorResult.Success) {
                    val location = locationResult.data
                    val newLat = location.coordinates.latitude
                    val newLon = location.coordinates.longitude
                    
                    // Cek jika lokasi berubah signifikan (misal beda 0.01 derajat ~1km)
                    val oldLat = sessionManager.getLat() ?: 0.0
                    val oldLon = sessionManager.getLon() ?: 0.0
                    val isLocationChanged = abs(newLat - oldLat) > 0.001 || abs(newLon - oldLon) > 0.001

                    if (isLocationChanged || !sessionManager.hasLocation()) {
                        val geocoder = Geocoder()
                        val geocoderResult = geocoder.reverse(newLat, newLon)
                        
                        var city = "Unknown"
                        var country = "Unknown"

                        if (geocoderResult is GeocoderResult.Success) {
                            val place = geocoderResult.data.firstOrNull()
                            if (place != null) {
                                city = place.locality ?: "Unknown"
                                country = place.country ?: "Unknown"
                                
                                _uiState.update {
                                    it.copy(cityName = city, countryName = country)
                                }
                            }
                        }

                        // Simpan ke session
                        sessionManager.saveLocation(city, country, newLat, newLon)
                        // Load ulang jadwal sholat
                        loadPrayerTimes(newLat, newLon)
                    }
                } else if (!sessionManager.hasLocation()) {
                    // Fallback jika gagal ambil lokasi DAN tidak ada di session
                    loadPrayerTimes(-7.2575, 112.7521) // Surabaya coords
                }
            } catch (e: Exception) {
                println("Error getting location: ${e.message}")
                if (!sessionManager.hasLocation()) {
                    loadPrayerTimes(-7.2575, 112.7521)
                }
            }
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private fun loadPrayerTimes(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val country = _uiState.value.countryName
                val methodId = getMethodIdForCountry(country)
                val prayers = getAzanTimesUseCase(lat, lon, methodId)
                val enriched = generatePrayerUi(prayers)
                val next = getNextPrayer(enriched)
                
                // Save next prayer to session
                sessionManager.saveNextPrayer("${next.name} ${next.time}")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        prayerList = enriched,
                        nextPrayer = next,
                        todayDate = getTodayFormattedDate(),
                        countdown = getRemainingTimeToPrayer(next),
                        prayerProgress = getPrayerProgress(enriched)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ── Countdown ticker (update setiap detik) ────────────────────────────────

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _uiState.value
                if (state.isLoading || state.prayerList.isEmpty()) continue

                val enriched = generatePrayerUi(state.prayerList)
                val next = getNextPrayer(enriched)
                
                // Keep session updated
                sessionManager.saveNextPrayer("${next.name} ${next.time}")

                _uiState.update {
                    it.copy(
                        prayerList = enriched,
                        nextPrayer = next,
                        countdown = getRemainingTimeToPrayer(next),
                        prayerProgress = getPrayerProgress(enriched)
                    )
                }
            }
        }
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun toggleAlert(enabled: Boolean) {
        _uiState.update { it.copy(alertEnabled = enabled) }
    }

    fun togglePrayerSwitch(prayerName: String, enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                prayerList = state.prayerList.map { prayer ->
                    if (prayer.name == prayerName) prayer.copy(isEnabled = enabled)
                    else prayer
                }
            )
        }
    }

    fun refresh() {
        updateLocationInfo()
    }

    fun updateManualLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val geocoder = Geocoder()
                val geocoderResult = geocoder.reverse(lat, lon)
                
                var city = "Unknown"
                var country = "Unknown"

                if (geocoderResult is GeocoderResult.Success) {
                    val place = geocoderResult.data.firstOrNull()
                    if (place != null) {
                        city = place.locality ?: "Unknown"
                        country = place.country ?: "Unknown"
                    }
                }

                _uiState.update {
                    it.copy(cityName = city, countryName = country)
                }
                
                sessionManager.saveLocation(city, country, lat, lon)
                loadPrayerTimes(lat, lon)
            } catch (e: Exception) {
                println("Error updating manual location: ${e.message}")
            }
        }
    }

    // ── Pure logic (tidak ada Composable dependency) ──────────────────────────

    private fun generatePrayerUi(prayers: List<PrayerTimeUi>): List<PrayerTimeUi> {
        val activePrayer = getCurrentPrayerName(prayers)
        return prayers.map { it.copy(isOnTime = it.name == activePrayer) }
    }

    private fun getCurrentPrayerName(prayers: List<PrayerTimeUi>): String? {
        val currentTotalMinute = getCurrentMinutes()
        val prayerMinutes = prayers.map { prayer ->
            val (hour, minute) = parseHourMinute(prayer.time)
            prayer.name to ((hour * 60) + minute)
        }
        for (i in prayerMinutes.indices) {
            val currentPrayer = prayerMinutes[i]
            val nextPrayer = prayerMinutes.getOrNull(i + 1)
            val currentPrayerMinute = currentPrayer.second
            val nextPrayerMinute = nextPrayer?.second ?: Int.MAX_VALUE
            if (currentTotalMinute in currentPrayerMinute..<nextPrayerMinute) {
                return currentPrayer.first
            }
        }
        return null
    }

    fun getNextPrayer(prayerList: List<PrayerTimeUi>): PrayerTimeUi {
        require(prayerList.isNotEmpty()) { "Prayer list cannot be empty" }
        val currentMinute = getCurrentMinutes()
        for (prayer in prayerList) {
            val (hour, minute) = parseHourMinute(prayer.time)
            if ((hour * 60) + minute > currentMinute) return prayer
        }
        return prayerList.first()
    }

    fun getRemainingTimeToPrayer(prayer: PrayerTimeUi): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentSecond = (now.hour * 3600) + (now.minute * 60) + now.second
        val (hour, minute) = parseHourMinute(prayer.time)
        val prayerSecond = (hour * 3600) + (minute * 60)
        var diff = prayerSecond - currentSecond
        if (diff < 0) diff += 24 * 3600
        return "${(diff / 3600).toTwoDigits()}:${((diff % 3600) / 60).toTwoDigits()}:${(diff % 60).toTwoDigits()}"
    }

    fun getPrayerProgress(prayerList: List<PrayerTimeUi>): Float {
        if (prayerList.isEmpty()) return 0f
        val currentMinute = getCurrentMinutes()
        val prayerMinutes = prayerList.map { parseToMinutes(it.time) }
        for (i in prayerMinutes.indices) {
            val start = prayerMinutes[i]
            val end = if (i < prayerMinutes.lastIndex) prayerMinutes[i + 1]
                      else prayerMinutes.first() + (24 * 60)
            val nowAdjusted = if (i == prayerMinutes.lastIndex && currentMinute < start)
                currentMinute + (24 * 60) else currentMinute
            if (nowAdjusted >= start && nowAdjusted < end) {
                return ((nowAdjusted - start).toFloat() / (end - start).toFloat()).coerceIn(0f, 1f)
            }
        }
        return 0f
    }

    fun getTodayFormattedDate(): String {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dayName = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val monthName = today.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$dayName, ${today.dayOfMonth} $monthName ${today.year}"
    }

    private fun getCurrentMinutes(): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return (now.hour * 60) + now.minute
    }

    private fun parseHourMinute(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return parts[0].toInt() to parts[1].toInt()
    }

    private fun parseToMinutes(time: String): Int {
        val parts = time.split(":")
        return (parts[0].toInt() * 60) + parts[1].toInt()
    }

    private fun getMethodIdForCountry(country: String): String {
        return when (country.lowercase()) {
            "indonesia", "id" -> "20"
            "malaysia", "my" -> "17"
            "singapore", "sg" -> "11"
            "saudi arabia", "sa" -> "4"
            "egypt", "eg" -> "5"
            "turkey", "tr" -> "13"
            "russia", "ru" -> "14"
            "united arab emirates", "ae" -> "16"
            "qatar", "qa" -> "10"
            "kuwait", "kw" -> "9"
            "france", "fr" -> "12"
            "morocco", "ma" -> "21"
            "tunisia", "tn" -> "18"
            "algeria", "dz" -> "19"
            "jordan", "jo" -> "23"
            "portugal", "pt" -> "22"
            "pakistan", "pk" -> "1"
            "iran", "ir" -> "7"
            "united states", "us", "canada", "ca" -> "2"
            else -> "3" // Default to Muslim World League (MWL)
        }
    }

    private fun Int.toTwoDigits(): String = toString().padStart(2, '0')
}
