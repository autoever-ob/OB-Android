package com.nobody.campick.services.network

import com.nobody.campick.models.home.RecommendedVehicle

data class RecommendResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: RecommendData
)
data class RecommendData(
    val newVehicle: RecommendedVehicle,
    val hotVehicle: RecommendedVehicle
)