package org.khoyron.bilal.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

@Serializable
data class PrayerData(
    val timings: Timings,
    val date: DateInfo,
    val meta: MetaInfo
)

@Serializable
data class Timings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

@Serializable
data class DateInfo(
    val readable: String,
    val timestamp: String
)

@Serializable
data class MetaInfo(
    val timezone: String
)