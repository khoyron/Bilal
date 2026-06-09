package org.khoyron.bilal.domain.repository

import org.khoyron.bilal.model.PrayerTimeUi

interface AzanRepository {
    suspend fun getPrayerTimes(latitude: Double, longitude: Double): List<PrayerTimeUi>
}
