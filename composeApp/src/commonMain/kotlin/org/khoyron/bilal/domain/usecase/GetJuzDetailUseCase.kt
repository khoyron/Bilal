package org.khoyron.bilal.domain.usecase

import org.khoyron.bilal.domain.repository.QuranRepository
import org.khoyron.bilal.ui.quran.detail.SurahDetailUi

class GetJuzDetailUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(juzNumber: Int): SurahDetailUi {
        return repository.getJuzDetail(juzNumber)
    }
}
