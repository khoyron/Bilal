package org.khoyron.bilal.domain.model

data class Mosque(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val imageUrl: String? = null,
    val isOpen: Boolean = true,
    val distance: String? = null,
    val nextPrayer: String? = null,
    val capacity: String? = null,
    val hasParking: Boolean = false,
    val rating: Double = 0.0
)
