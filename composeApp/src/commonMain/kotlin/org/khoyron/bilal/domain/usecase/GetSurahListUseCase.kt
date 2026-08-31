package org.khoyron.bilal.domain.usecase

import org.khoyron.bilal.domain.repository.QuranRepository
import org.khoyron.bilal.ui.quran.SurahUi

class GetSurahListUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(): List<SurahUi> {
        return repository.getSurahList()
    }
}
