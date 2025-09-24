package com.nobody.campick.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileApiResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: ProfileData
)

@Serializable
data class ProfileData(
    val id: Int,
    val nickname: String,
    val rating: Double? = null,
    val reviews: List<Review> = emptyList(),
    @SerialName("createdAt")
    val createdAtString: String,
    val profileImage: String? = null,
    val description: String? = null,
    val mobileNumber: String? = null
)

// 하위 호환성을 위한 별칭
typealias ProfileResponse = ProfileData

@Serializable
data class Review(
    val nickName: String,
    val profileImage: String,
    val rating: Double,
    val content: String,
    @SerialName("createdAt")
    val createdAtString: String
)

@Serializable
data class ProductPage(
    val product: Page<Product>
)

@Serializable
data class Page<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    @SerialName("page")
    val number: Int? = null,
    val numberOfElements: Int? = null,
    val first: Boolean? = null,
    val last: Boolean
)

@Serializable
data class Product(
    val productId: Int,
    val title: String,
    val cost: Int,
    val generation: Int,
    val mileage: Int,
    val location: String,
    @SerialName("createdAt")
    val createdAtString: String,
    @SerialName("productImageUrl")
    val thumbNailUrl: String? = null,
    val status: String
)

