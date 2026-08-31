package org.khoyron.bilal.domain.repository

import org.khoyron.bilal.domain.model.Mosque

interface MosqueRepository {
    suspend fun getMosques(country: String, province: String, city: String): List<Mosque>
    
    /**
     * Standardizes strings for GitHub API lookup.
     * Matches the Node.js implementation in khoyron/list_mosque.
     */
    fun slugify(text: String?): String

    /**
     * Fetches mosques by coordinates using GADM PIP for robust city identification.
     */
    suspend fun getMosquesByLocation(lat: Double, lng: Double, iso2CountryCode: String): List<Mosque>
}
