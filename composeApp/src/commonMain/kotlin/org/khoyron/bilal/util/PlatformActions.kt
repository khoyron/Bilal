package org.khoyron.bilal.util

interface PlatformActions {
    fun shareText(text: String)
    fun openMaps(
        destLat: Double,
        destLon: Double,
        label: String,
        fromLat: Double? = null,
        fromLon: Double? = null
    )
}
