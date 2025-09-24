package com.nobody.campick.models.vehicle

import kotlinx.serialization.Serializable

@Serializable
data class CategoryTypeResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: CategoryTypeData
)

@Serializable
data class CategoryTypeData(
    val models: List<String>
)