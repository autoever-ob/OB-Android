package com.nobody.campick.models.vehicle

import kotlinx.serialization.Serializable

@Serializable
data class VehicleRegistrationRequest(
    val generation: Int,
    val mileage: String,
    val vehicleType: String,
    val vehicleModel: String,
    val price: String,
    val location: String,
    val plateHash: String,
    val title: String,
    val description: String,
    val productImageUrl: List<String>,
    val option: List<VehicleOption>,
    val mainProductImageUrl: String
)