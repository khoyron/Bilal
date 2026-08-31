package org.khoyron.bilal.domain.repository

import org.khoyron.bilal.domain.model.MethodAzan
import org.khoyron.bilal.model.PrayerTimeUi

interface AzanRepository {
    suspend fun getPrayerTimes(latitude: Double, longitude: Double, method: String): List<PrayerTimeUi>
    suspend fun getMethods(): List<MethodAzan>
}
