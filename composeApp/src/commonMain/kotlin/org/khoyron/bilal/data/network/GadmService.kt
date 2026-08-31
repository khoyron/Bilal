package org.khoyron.bilal.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.khoyron.bilal.util.GeospatialUtils

class GadmService(private val client: HttpClient) {

    private val baseUrl = "https://raw.githubusercontent.com/khoyron/list_mosque/main/gadm-json"
    private val cache = mutableMapOf<String, FeatureCollection>()
    private val json = Json { ignoreUnknownKeys = true }

    private val iso2ToIso3 = mapOf(
        "AF" to "AFG", "AM" to "ARM", "AZ" to "AZE", "BD" to "BGD", "BN" to "BRN",
        "BT" to "BTN", "CN" to "CHN", "CY" to "CYP", "GE" to "GEO", "ID" to "IDN",
        "IN" to "IND", "IR" to "IRN", "IQ" to "IRQ", "IL" to "ISR", "JO" to "JOR",
        "JP" to "JPN", "KZ" to "KAZ", "KG" to "KGZ", "KH" to "KHM", "KR" to "KOR",
        "KW" to "KWT", "LA" to "LAO", "LB" to "LBN", "LK" to "LKA", "MV" to "MDV",
        "MM" to "MMR", "MN" to "MNG", "MY" to "MYS", "NP" to "NPL", "OM" to "OMN",
        "PK" to "PAK", "PH" to "PHL", "PS" to "PSE", "QA" to "QAT", "RU" to "RUS",
        "SA" to "SAU", "SG" to "SGP", "SY" to "SYR", "TH" to "THA", "TJ" to "TJK",
        "TM" to "TKM", "TL" to "TLS", "TR" to "TUR", "UZ" to "UZB", "VN" to "VNM",
        "YE" to "YEM"
    )

    fun getCountryCodeIso3(iso2: String): String? {
        return iso2ToIso3[iso2.uppercase()]
    }

    suspend fun getAdministrativeArea(lat: Double, lng: Double, iso3: String): GadmProperties? {
        val features = getFeatures(iso3) ?: return null
        
        for (feature in features.features) {
            val geometry = feature.geometry
            val properties = feature.properties
            
            // Check Bounding Box first
            if (GeospatialUtils.isPointInBoundingBox(lat, lng, feature.bbox)) {
                if (geometry.type == "Polygon") {
                    val coords: List<List<List<Double>>> = json.decodeFromJsonElement(geometry.coordinates)
                    if (GeospatialUtils.isPointInPolygon(lat, lng, coords)) {
                        return properties
                    }
                } else if (geometry.type == "MultiPolygon") {
                    val coords: List<List<List<List<Double>>>> = json.decodeFromJsonElement(geometry.coordinates)
                    for (polygon in coords) {
                        if (GeospatialUtils.isPointInPolygon(lat, lng, polygon)) {
                            return properties
                        }
                    }
                }
            }
        }
        return null
    }

    private suspend fun getFeatures(iso3: String): FeatureCollection? {
        if (cache.containsKey(iso3)) return cache[iso3]

        val url = "$baseUrl/$iso3/gadm41_${iso3}_2.json"
        return try {
            val responseText = client.get(url).bodyAsText()
            val response: FeatureCollection = json.decodeFromString(responseText)
            
            // Pre-calculate Bounding Boxes for each feature to speed up PIP
            val processedFeatures = response.features.map { feature ->
                val bbox = if (feature.geometry.type == "Polygon") {
                    val coords: List<List<List<Double>>> = json.decodeFromJsonElement(feature.geometry.coordinates)
                    GeospatialUtils.calculateBoundingBox(listOf(coords))
                } else {
                    val coords: List<List<List<List<Double>>>> = json.decodeFromJsonElement(feature.geometry.coordinates)
                    GeospatialUtils.calculateBoundingBox(coords)
                }
                feature.copy(bbox = bbox)
            }
            
            val collection = response.copy(features = processedFeatures)
            cache[iso3] = collection
            collection
        } catch (e: Exception) {
            println("Error fetching GADM data for $iso3: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

@Serializable
data class FeatureCollection(
    val type: String,
    val features: List<Feature>
)

@Serializable
data class Feature(
    val type: String,
    val properties: GadmProperties,
    val geometry: Geometry,
    val bbox: DoubleArray = doubleArrayOf()
)

@Serializable
data class Geometry(
    val type: String,
    val coordinates: JsonElement
)

@Serializable
data class GadmProperties(
    val NAME_1: String? = null,
    val NAME_2: String? = null,
    val TYPE_2: String? = null
)
