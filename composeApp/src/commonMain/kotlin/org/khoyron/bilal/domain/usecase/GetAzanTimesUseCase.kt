package org.khoyron.bilal.domain.usecase

import org.khoyron.bilal.domain.repository.AzanRepository
import org.khoyron.bilal.model.PrayerTimeUi

class GetAzanTimesUseCase(private val repository: AzanRepository) {
    suspend operator fun invoke(latitude: Double, longitude: Double): List<PrayerTimeUi> {
        return repository.getPrayerTimes(latitude, longitude)
    }
}
