package org.khoyron.bilal.domain.repository

import org.khoyron.bilal.ui.quran.SurahUi
import org.khoyron.bilal.ui.quran.detail.SurahDetailUi

interface QuranRepository {
    suspend fun getSurahList(): List<SurahUi>
    suspend fun getSurahDetail(surahNumber: Int): SurahDetailUi
    suspend fun getJuzDetail(juzNumber: Int): SurahDetailUi
}
