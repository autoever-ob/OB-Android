package com.nobody.campick.models.vehicle

import kotlinx.serialization.Serializable

@Serializable
data class ProductInfoResponse(
    val option: List<String>,
    val model: List<String>,
    val type: List<String>
)