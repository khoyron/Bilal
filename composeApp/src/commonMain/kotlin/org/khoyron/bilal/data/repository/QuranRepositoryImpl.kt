package org.khoyron.bilal.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.khoyron.bilal.data.entity.SurahResponse
import org.khoyron.bilal.data.entity.SurahEditionsResponse
import org.khoyron.bilal.data.entity.JuzResponse
import org.khoyron.bilal.domain.repository.QuranRepository
import org.khoyron.bilal.ui.quran.SurahUi
import org.khoyron.bilal.ui.quran.detail.AyahUi
import org.khoyron.bilal.ui.quran.detail.SurahDetailUi

class QuranRepositoryImpl(private val client: HttpClient) : QuranRepository {
    override suspend fun getSurahList(): List<SurahUi> {
        val response: SurahResponse = client.get("https://api.alquran.cloud/v1/surah").body()
        return response.data.map { dto ->
            SurahUi(
                number = dto.number,
                nameLatn = dto.englishName,
                nameArabic = dto.name,
                translation = dto.englishNameTranslation.uppercase(),
                totalAyah = dto.numberOfAyahs,
                isFavorite = false
            )
        }
    }

    override suspend fun getSurahDetail(surahNumber: Int): SurahDetailUi {
        val response: SurahEditionsResponse = client.get("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.asad").body()

        val arabicEdition = response.data[0]
        val englishEdition = response.data[1]

        val ayahs = arabicEdition.ayahs.zip(englishEdition.ayahs) { arabic, english ->
            AyahUi(
                globalNumber = arabic.number,
                surahNumber = surahNumber,
                ayahNumber = arabic.numberInSurah,
                arabic =
                    if (surahNumber!=1&&arabic.numberInSurah==1){
                        arabic.text.replace("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيم","")
                    }else {
                        arabic.text.replace("۞","")
                    },
                translation = english.text,
                isBookmarked = false
            )
        }

        return SurahDetailUi(
            number = arabicEdition.number,
            nameLatn = arabicEdition.englishName,
            nameArabic = arabicEdition.name,
            translation = arabicEdition.englishNameTranslation.uppercase(),
            totalAyah = arabicEdition.numberOfAyahs,
            ayahs = ayahs
        )
    }

    override suspend fun getJuzDetail(juzNumber: Int): SurahDetailUi {
        val arabicResponse: JuzResponse = client.get("https://api.alquran.cloud/v1/juz/$juzNumber/quran-uthmani").body()
        val englishResponse: JuzResponse = client.get("https://api.alquran.cloud/v1/juz/$juzNumber/en.asad").body()

        val ayahs = arabicResponse.data.ayahs.zip(englishResponse.data.ayahs) { arabic, english ->
            AyahUi(
                globalNumber = arabic.number,
                surahNumber = arabic.surah.number,
                ayahNumber = arabic.numberInSurah,
                arabic = arabic.text.replace("۞",""),
                translation = english.text,
                isBookmarked = false
            )
        }

        return SurahDetailUi(
            number = juzNumber,
            nameLatn = "Juz $juzNumber",
            nameArabic = "الجزء ${changeJuzNumberToArabic(juzNumber)}",
            translation = "JUZ $juzNumber",
            totalAyah = ayahs.size,
            ayahs = ayahs
        )
    }

    fun changeJuzNumberToArabic(number: Int): String {
        return when (number) {
            1 -> "الأول"
            2 -> "الثاني"
            3 -> "الثالث"
            4 -> "الرابع"
            5 -> "الخامس"
            6 -> "السادس"
            7 -> "السابع"
            8 -> "الثامن"
            9 -> "التاسع"
            10 -> "العاشر"
            11 -> "الحادي عشر"
            12 -> "الثاني عشر"
            13 -> "الثالث عشر"
            14 -> "الرابع عشر"
            15 -> "الخامس عشر"
            16 -> "السادس عشر"
            17 -> "السابع عشر"
            18 -> "الثامن عشر"
            19 -> "التاسع عشر"
            20 -> "العشرون"
            21 -> "الحادي والعشرون"
            22 -> "الثاني والعشرون"
            23 -> "الثالث والعشرون"
            24 -> "الرابع والعشرون"
            25 -> "الخامس والعشرون"
            26 -> "السادس والعشرون"
            27 -> "السابع والعشرون"
            28 -> "الثامن والعشرون"
            29 -> "التاسع والعشرون"
            30 -> "الثلاثون"
            else -> number.toString()
        }
    }
}
