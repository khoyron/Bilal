package org.khoyron.bilal.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class MethodsResponse(
    val code: Int,
    val status: String,
    val data: Map<String, MethodDto>
)

@Serializable
data class MethodDto(
    val id: Int,
    val name: String? = null
)
