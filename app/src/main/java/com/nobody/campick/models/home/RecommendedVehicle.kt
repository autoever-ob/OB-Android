package com.nobody.campick.models.home

import kotlinx.serialization.Serializable

@Serializable
enum class RecommendedVehicleStatus {
    AVAILABLE,
    SOLD,
    RESERVED
}

@Serializable
data class RecommendedVehicle(
    val productId: Int,
    val title: String,
    val price: String,
    val mileage: String,
    val location: String,
    val createdAt: String,
    val thumbNail: String,
    val status: RecommendedVehicleStatus,
    val isLiked: Boolean,
    val likeCount: Int
) {
    val id: Int
        get() = productId
}