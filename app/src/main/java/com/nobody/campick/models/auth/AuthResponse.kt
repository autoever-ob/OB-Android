package com.nobody.campick.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val user: UserDTO? = null,
    val memberId: String? = null,
    val dealerId: String? = null,
    val profileImageUrl: String? = null,
    val profileThumbnailUrl: String? = null,
    val phoneNumber: String? = null,
    val role: String? = null,
    val nickname: String? = null
)