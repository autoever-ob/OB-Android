package com.nobody.campick.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginDataDTO(
    val accessToken: String,
    val refreshToken: String? = null,
    val user: UserDTO? = null,
    val memberId: Int? = null,  // 서버에서 숫자로 보내므로 Int로 변경
    val dealerId: Int? = null,  // 서버에서 숫자로 보내므로 Int로 변경
    val profileImageUrl: String? = null,
    val profileThumbnailUrl: String? = null,
    val phoneNumber: String? = null,
    val role: String? = null,
    val nickname: String? = null
) {
    // 문자열 변환을 위한 편의 프로퍼티
    val memberIdString: String?
        get() = memberId?.toString()

    val dealerIdString: String?
        get() = dealerId?.toString()
}