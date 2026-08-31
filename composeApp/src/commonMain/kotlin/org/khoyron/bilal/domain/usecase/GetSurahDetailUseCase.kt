package org.khoyron.bilal.domain.usecase

import org.khoyron.bilal.domain.repository.QuranRepository
import org.khoyron.bilal.ui.quran.detail.SurahDetailUi

class GetSurahDetailUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(surahNumber: Int): SurahDetailUi {
        return repository.getSurahDetail(surahNumber)
    }
}
