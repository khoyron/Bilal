package org.khoyron.bilal.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.khoyron.bilal.data.remote.AladhanResponse
import org.khoyron.bilal.domain.repository.AzanRepository
import org.khoyron.bilal.model.PrayerTimeUi

class AzanRepositoryImpl(private val client: HttpClient) : AzanRepository {
    override suspend fun getPrayerTimes(latitude: Double, longitude: Double): List<PrayerTimeUi> {
        val response: AladhanResponse = client.get("https://api.aladhan.com/v1/timings") {
            url {
                parameters.append("latitude", latitude.toString())
                parameters.append("longitude", longitude.toString())
                parameters.append("method", "20") // ISNA method as default
            }
        }.body()

        val timings = response.data.timings
        return listOf(
            PrayerTimeUi("FAJR", timings.Fajr),
            PrayerTimeUi("DHUHR", timings.Dhuhr),
            PrayerTimeUi("ASR", timings.Asr),
            PrayerTimeUi("MAGHRIB", timings.Maghrib),
            PrayerTimeUi("ISHA", timings.Isha)
        )
    }
}
