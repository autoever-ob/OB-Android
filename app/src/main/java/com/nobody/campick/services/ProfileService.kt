package com.nobody.campick.services

import com.nobody.campick.models.Page
import com.nobody.campick.models.Product
import com.nobody.campick.models.ProfileData
import com.nobody.campick.services.network.APIService
import com.nobody.campick.services.network.ApiResult
import com.nobody.campick.services.network.Endpoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ProfileService {

    /**
     * 회원 정보 조회
     */
    suspend fun fetchMemberInfo(memberId: String): ApiResult<ProfileData> {
        return APIService.get<ProfileData>(
            endpoint = Endpoint.MemberInfo(memberId)
        )
    }

    /**
     * 회원의 판매중 상품 조회
     */
    suspend fun fetchMemberProducts(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<Page<Product>> {
        return APIService.get<Page<Product>>(
            endpoint = Endpoint.MemberProducts(memberId),
            queryParams = mapOf(
                "page" to page.toString(),
                "size" to size.toString()
            )
        )
    }

    /**
     * 회원의 판매완료 상품 조회
     */
    suspend fun fetchMemberSoldProducts(
        memberId: String,
        page: Int,
        size: Int
    ): ApiResult<Page<Product>> {
        return APIService.get<Page<Product>>(
            endpoint = Endpoint.MemberSoldProducts(memberId),
            queryParams = mapOf(
                "page" to page.toString(),
                "size" to size.toString()
            )
        )
    }

    /**
     * 회원 탈퇴
     */
    suspend fun deleteMemberAccount(): ApiResult<Unit> {
        return APIService.delete<Unit>(
            endpoint = Endpoint.MemberDelete
        )
    }

    /**
     * 닉네임 변경
     */
    suspend fun updateNickname(nickname: String): ApiResult<Unit> {
        // JSON 문자열로 직렬화
        val jsonBody = Json.encodeToString(mapOf("nickname" to nickname))

        return APIService.put<Unit>(
            endpoint = Endpoint.MemberNickname,
            body = jsonBody
        )
    }

    /**
     * 비밀번호 변경
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): ApiResult<Unit> {
        // JSON 문자열로 직렬화
        val jsonBody = Json.encodeToString(
            mapOf(
                "currentPassword" to currentPassword,
                "newPassword" to newPassword
            )
        )

        return APIService.put<Unit>(
            endpoint = Endpoint.ChangePassword,
            body = jsonBody
        )
    }

    /**
     * 프로필 이미지 업로드
     */
    suspend fun uploadProfileImage(imageData: ByteArray): ApiResult<ProfileImageResponse> {
        return APIService.multipart<ProfileImageResponse>(
            endpoint = Endpoint.MemberImage,
            parts = mapOf("file" to imageData)
        )
    }

    /**
     * 회원정보 수정 (닉네임, 핸드폰, 설명)
     */
    suspend fun updateMemberInfo(
        nickname: String,
        mobileNumber: String,
        description: String
    ): ApiResult<String> {
        // iOS와 동일한 JSON 구조로 직렬화
        val jsonBody = Json.encodeToString(
            mapOf(
                "nickname" to nickname,
                "mobileNumber" to mobileNumber,
                "description" to description
            )
        )

        return APIService.patch<String>(
            endpoint = Endpoint.MemberUpdate,
            body = jsonBody
        )
    }

    /**
     * 로그아웃
     */
    suspend fun logout(): ApiResult<String> {
        return APIService.post<String>(
            endpoint = Endpoint.Logout
        )
    }

    /**
     * 회원탈퇴
     */
    suspend fun deleteMember(): ApiResult<String> {
        return APIService.delete<String>(
            endpoint = Endpoint.MemberDelete
        )
    }

    /**
     * 멤버별 판매완료 매물 개수 조회
     */
    suspend fun getProductSoldCount(memberId: String): ApiResult<Int> {
        return APIService.get<Int>(
            endpoint = Endpoint.CountProductSold(memberId)
        )
    }

    /**
     * 멤버별 판매중/예약중 매물 개수 조회
     */
    suspend fun getProductSellOrReserveCount(memberId: String): ApiResult<Int> {
        return APIService.get<Int>(
            endpoint = Endpoint.CountProductSellOrReserve(memberId)
        )
    }

    /**
     * 멤버별 전체 매물 개수 조회
     */
    suspend fun getProductAllCount(memberId: String): ApiResult<Int> {
        return APIService.get<Int>(
            endpoint = Endpoint.CountProductAll(memberId)
        )
    }

    /**
     * 멤버별 판매중/예약중 매물 리스트 조회
     */
    suspend fun fetchMemberSellOrReserveProducts(
        memberId: String,
        page: Int,
        size: Int = 2
    ): ApiResult<Page<Product>> {
        return APIService.get<Page<Product>>(
            endpoint = Endpoint.MemberSellOrReserveProducts(memberId),
            queryParams = mapOf(
                "page" to page.toString(),
                "size" to size.toString()
            )
        )
    }
}

/**
 * 프로필 이미지 업로드 응답
 */
data class ProfileImageResponse(
    val profileImageUrl: String,
    val profileThumbnailUrl: String
)