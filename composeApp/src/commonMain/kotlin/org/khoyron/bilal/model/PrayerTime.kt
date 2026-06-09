package org.khoyron.bilal.model

data class PrayerTimeUi(
    val name: String,
    val time: String, // format HH:mm
    val isEnabled: Boolean = false,
    val isOnTime: Boolean = false
)