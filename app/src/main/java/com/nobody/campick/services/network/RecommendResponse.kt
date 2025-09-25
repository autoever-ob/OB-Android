package com.nobody.campick.services.network

import com.nobody.campick.models.home.RecommendedVehicle
import kotlinx.serialization.Serializable

@Serializable
data class RecommendResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: RecommendData
)

@Serializable
data class RecommendData(
    val newVehicle: RecommendedVehicle,
    val hotVehicle: RecommendedVehicle
)