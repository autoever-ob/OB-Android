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
    val description: String? = null
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
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean
)

@Serializable
data class Product(
    val productId: String,
    val title: String,
    val cost: String,
    val generation: Int,
    val mileage: Int,
    val location: String,
    @SerialName("createdAt")
    val createdAtString: String,
    val thumbNailUrl: String,
    val status: String
)

// 기존 UserProfile 구조체 (하위 호환성을 위해 유지)
data class UserProfile(
    val id: String,
    val name: String,
    val avatar: String,
    val joinDate: String,
    val rating: Double,
    val totalListings: Int,
    val activeListing: Int,
    val totalSales: Int,
    val isDealer: Boolean,
    val location: String,
    val phone: String? = null,
    val email: String? = null,
    val bio: String? = null
)