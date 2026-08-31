package org.khoyron.bilal.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.khoyron.bilal.data.network.GadmService
import org.khoyron.bilal.domain.model.Mosque
import org.khoyron.bilal.domain.repository.MosqueRepository

class MosqueRepositoryImpl(
    private val client: HttpClient,
    private val gadmService: GadmService
) : MosqueRepository {

    private val baseUrl = "https://raw.githubusercontent.com/khoyron/list_mosque/main/data"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getMosques(country: String, province: String, city: String): List<Mosque> {
        val countrySlug = slugify(country)
        val provinceSlug = slugify(province)
        val citySlug = slugify(city)

        // Try city-specific file
        val url = "$baseUrl/$countrySlug/$provinceSlug/$citySlug.json"
        println("GADM: Fetching mosques from $url")
        return fetchMosques(url)
    }

    override suspend fun getMosquesByLocation(lat: Double, lng: Double, iso2CountryCode: String): List<Mosque> {
        val iso3 = gadmService.getCountryCodeIso3(iso2CountryCode) ?: return emptyList()
        val props = gadmService.getAdministrativeArea(lat, lng, iso3) ?: run {
            println("GADM: Area not found for lat=$lat, lng=$lng in $iso3")
            return emptyList()
        }

        println("GADM: Detected area: ${props.NAME_1}, ${props.NAME_2} (${props.TYPE_2})")

        val isIndonesia = iso3 == "IDN"
        
        val countrySlug = if (isIndonesia) "indonesia" else slugify(iso3)
        val provinceSlug = slugify(props.NAME_1, !isIndonesia)
        val citySlug = slugify(props.NAME_2, !isIndonesia)

        val url = "$baseUrl/$countrySlug/$provinceSlug/$citySlug.json"
        println("GADM: Fetching mosques from $url")
        return fetchMosques(url)
    }

    private suspend fun fetchMosques(url: String): List<Mosque> {
        return try {
            val responseText = client.get(url).bodyAsText()
            val response: List<MosqueApiEntity> = json.decodeFromString(responseText)
            response.map { it.toDomain() }
        } catch (e: Exception) {
            println("Error fetching mosques from $url: ${e.message}")
            emptyList()
        }
    }

    override fun slugify(text: String?): String = slugify(text, true)

    private fun slugify(text: String?, useHyphen: Boolean): String {
        if (text.isNullOrBlank()) return "lainnya"
        
        val replacement = if (useHyphen) "-" else ""
        return text.lowercase()
            .trim()
            .replace(Regex("[^a-z0-9]+"), replacement)
            .replace(Regex("^-+|-+$"), "")
            .ifBlank { "lainnya" }
    }
}

@Serializable
data class MosqueApiEntity(
    val id: String,
    val nama: String,
    val lat: Double,
    val lng: Double,
    val alamat: String? = null,
    val osm_id: Long? = null,
    val osm_type: String? = null
) {
    fun toDomain() = Mosque(
        id = id,
        name = nama,
        latitude = lat,
        longitude = lng,
        address = alamat ?: "",
        isOpen = true,
        hasParking = true
    )
}
