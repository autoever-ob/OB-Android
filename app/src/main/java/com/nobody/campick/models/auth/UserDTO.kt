package com.nobody.campick.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String? = null,
    val memberId: String? = null,
    val dealerId: String? = null,
    val name: String? = null,
    val nickname: String? = null,
    val mobileNumber: String? = null,
    val role: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val profileImage: String? = null,
    val createdAt: String? = null
) {
    // iOS와 동일한 로직: 서버가 내려준 프로필 이미지 URL 중 하나를 반환
    val resolvedProfileImageURL: String?
        get() = profileImageUrl ?: profileImage
}