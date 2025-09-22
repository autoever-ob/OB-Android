package com.nobody.campick.services.network

import kotlinx.serialization.Serializable

@Serializable
data class ProductItemDTO(
    val productId: Int,
    val title: String,
    val price: String,
    val generation: Int?,
    val fuelType: String,
    val transmission: String,
    val mileage: String,
    val location: String,
    val createdAt: String,
    val thumbNail: String,
    val isLiked: Boolean,
    val likeCount: Int,
    val status: String
)

@Serializable
data class ProductDetailDTO(
    val productId: Int,
    val title: String,
    val price: String,
    val generation: Int?,
    val fuelType: String,
    val transmission: String,
    val mileage: String,
    val location: String,
    val createdAt: String,
    val images: List<String>,
    val description: String,
    val isLiked: Boolean,
    val likeCount: Int,
    val status: String,
    val seller: ProductSellerDTO,
    val options: List<ProductOptionDTO>
)

@Serializable
data class ProductLocationDTO(
    val province: String,
    val city: String
)

@Serializable
data class ProductOptionDTO(
    val optionName: String,
    val isInclude: Boolean
)

@Serializable
data class ProductSellerDTO(
    val nickName: String,
    val role: String,
    val rating: Double,
    val sellingCount: Int,
    val completeCount: Int,
    val userId: Int
)

@Serializable
data class ProductInfoResponse(
    val vehicleTypes: List<String>,
    val fuelTypes: List<String>,
    val transmissions: List<String>,
    val options: List<String>
)

// Response wrappers
@Serializable
data class ProductInfoApiResponse(
    val success: Boolean,
    val data: ProductInfoResponse?,
    val message: String
)

@Serializable
data class ProductDetailResponse(
    val success: Boolean,
    val data: ProductDetailDTO?,
    val message: String?
)