package org.khoyron.bilal.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SessionManager(private val settings: Settings) {

    companion object {
        private const val KEY_CITY = "city_name"
        private const val KEY_COUNTRY = "country_name"
        private const val KEY_LAT = "latitude"
        private const val KEY_LON = "longitude"
        private const val KEY_METHOD = "azan_method"
        private const val KEY_METHODS_LIST = "azan_methods_list"
        private const val KEY_NEXT_PRAYER = "next_prayer"
    }

    fun saveLocation(city: String, country: String, lat: Double, lon: Double) {
        settings[KEY_CITY] = city
        settings[KEY_COUNTRY] = country
        settings[KEY_LAT] = lat
        settings[KEY_LON] = lon
    }

    fun saveMethodsList(methodsJson: String) {
        settings[KEY_METHODS_LIST] = methodsJson
    }

    fun saveNextPrayer(prayer: String) {
        settings[KEY_NEXT_PRAYER] = prayer
    }

    fun getCity(): String? = settings.getStringOrNull(KEY_CITY)
    fun getCountry(): String? = settings.getStringOrNull(KEY_COUNTRY)
    fun getLat(): Double? = settings.getDoubleOrNull(KEY_LAT)
    fun getLon(): Double? = settings.getDoubleOrNull(KEY_LON)

    fun getMethodsList(): String? = settings.getStringOrNull(KEY_METHODS_LIST)
    fun getNextPrayer(): String? = settings.getStringOrNull(KEY_NEXT_PRAYER)

    fun hasLocation(): Boolean {
        return getLat() != null && getLon() != null
    }
}
