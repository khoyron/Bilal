package org.khoyron.bilal.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class SurahResponse(
    val code: Int,
    val status: String,
    val data: List<SurahDto>
)

@Serializable
data class SurahDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@Serializable
data class SurahEditionsResponse(
    val code: Int,
    val status: String,
    val data: List<SurahDetailDto>
)

@Serializable
data class SurahDetailDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val ayahs: List<AyahDto>
)

@Serializable
data class AyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int
)

@Serializable
data class JuzResponse(
    val code: Int,
    val status: String,
    val data: JuzDataDto
)

@Serializable
data class JuzDataDto(
    val number: Int,
    val ayahs: List<JuzAyahDto>
)

@Serializable
data class JuzAyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val surah: SurahDto
)
