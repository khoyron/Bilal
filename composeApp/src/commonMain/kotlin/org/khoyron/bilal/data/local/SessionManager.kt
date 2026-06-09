package org.khoyron.bilal.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SessionManager(private val settings: Settings) {

    companion object {
        private const val KEY_CITY = "city_name"
        private const val KEY_COUNTRY = "country_name"
        private const val KEY_LAT = "latitude"
        private const val KEY_LON = "longitude"
    }

    fun saveLocation(city: String, country: String, lat: Double, lon: Double) {
        settings[KEY_CITY] = city
        settings[KEY_COUNTRY] = country
        settings[KEY_LAT] = lat
        settings[KEY_LON] = lon
    }

    fun getCity(): String? = settings.getStringOrNull(KEY_CITY)
    fun getCountry(): String? = settings.getStringOrNull(KEY_COUNTRY)
    fun getLat(): Double? = settings.getDoubleOrNull(KEY_LAT)
    fun getLon(): Double? = settings.getDoubleOrNull(KEY_LON)

    fun hasLocation(): Boolean {
        return getLat() != null && getLon() != null
    }
}
