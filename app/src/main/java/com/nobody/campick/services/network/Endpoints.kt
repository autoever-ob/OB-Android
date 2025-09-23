package com.nobody.campick.services.network

/**
 * API 엔드포인트 정의
 * iOS의 Endpoints.swift와 동일한 역할
 */
sealed class Endpoint(val path: String) {

    // 인증 관련
    object Login : Endpoint("/api/member/login")
    object Signup : Endpoint("/api/member/signup")
    object EmailSend : Endpoint("/api/member/email/send")
    object EmailVerify : Endpoint("/api/member/email/verify")
    object Logout : Endpoint("/api/member/logout")
    object TokenReissue : Endpoint("/api/member/reissue") // 토큰 재발급 요청

    // 상품 관련
    object UploadImage : Endpoint("/api/product/image")
    object RegisterProduct : Endpoint("/api/product")
    object CarRecommend : Endpoint("/api/product/recommend")
    object Products : Endpoint("/api/product")
    object ProductInfo : Endpoint("/api/product/info")
    data class ProductDetail(val productId: String) : Endpoint("/api/product/$productId")
    data class ProductLike(val productId: String) : Endpoint("/api/product/$productId/like")

    // 채팅 관련
    object ChatList : Endpoint("/api/chat/my")

    // 멤버 관련
    data class MemberInfo(val memberId: String) : Endpoint("/api/member/info/$memberId")
    data class MemberProducts(val memberId: String) : Endpoint("/api/member/product/all/$memberId")
    data class MemberSoldProducts(val memberId: String) : Endpoint("/api/member/sold/$memberId")
    data class MemberMyProductList(val memberId: String) : Endpoint("/api/member/product/sold/$memberId")
    object MemberSignout : Endpoint("/api/member/signout")
    object MemberNickname : Endpoint("/api/member/nickname")
    object MemberImage : Endpoint("/api/member/image")
    object ChangePassword : Endpoint("/api/member/password")

    companion object {
        const val BASE_URL = "https://campick.shop"

        /**
         * 전체 URL 반환
         */
        fun getFullUrl(endpoint: Endpoint): String {
            return BASE_URL + endpoint.path
        }
    }

    /**
     * 전체 URL 프로퍼티
     */
    val url: String
        get() = BASE_URL + path

    /**
     * HTTP 메서드 정의 (필요시 확장 가능)
     */
    enum class Method {
        GET, POST, PUT, DELETE, PATCH
    }

    /**
     * 각 엔드포인트의 기본 HTTP 메서드
     */
    val defaultMethod: Method
        get() = when (this) {
            is Login, is Signup, is EmailSend, is EmailVerify,
            is UploadImage, is RegisterProduct, is TokenReissue -> Method.POST

            is Logout, is MemberSignout -> Method.DELETE

            is MemberNickname, is MemberImage, is ChangePassword -> Method.PUT

            is CarRecommend, is ChatList, is Products, is ProductInfo, is ProductDetail,
            is MemberInfo, is MemberProducts, is MemberSoldProducts, is MemberMyProductList -> Method.GET
        }

    /**
     * 인증이 필요한 엔드포인트인지 확인
     */
    val requiresAuthentication: Boolean
        get() = when (this) {
            is Login, is Signup, is EmailSend, is EmailVerify, is TokenReissue -> false
            else -> true
        }

    /**
     * 멀티파트 업로드가 필요한 엔드포인트인지 확인
     */
    val isMultipart: Boolean
        get() = when (this) {
            is UploadImage, is RegisterProduct, is MemberImage -> true
            else -> false
        }
}