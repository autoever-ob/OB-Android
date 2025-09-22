package com.nobody.campick.models.vehicle

import androidx.compose.ui.graphics.Color
import com.nobody.campick.resources.theme.AppColors
import kotlinx.serialization.Serializable

@Serializable
enum class VehicleStatus(val value: String) {
    ACTIVE("active"),
    RESERVED("reserved"),
    SOLD("sold");

    val displayText: String
        get() = when (this) {
            ACTIVE -> "판매중"
            RESERVED -> "예약중"
            SOLD -> "판매완료"
        }

    val color: Color
        get() = when (this) {
            ACTIVE -> AppColors.brandLightGreen
            RESERVED -> AppColors.brandOrange
            SOLD -> AppColors.brandWhite50
        }
}

@Serializable
data class Vehicle(
    val id: String,
    // Images
    val imageName: String? = null,
    val thumbnailURL: String? = null,
    // Basics
    val title: String,
    val price: String,
    val year: String,
    val mileage: String,
    val fuelType: String,
    val transmission: String,
    val location: String,
    // Status
    val status: VehicleStatus,
    val postedDate: String? = null,
    // Flags
    val isOnSale: Boolean,
    val isFavorite: Boolean,
    val likeCount: Int? = null
)