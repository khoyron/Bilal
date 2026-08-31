package org.khoyron.bilal.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MethodAzan(
    val id: Int,
    val name: String,
    val key : String
)
