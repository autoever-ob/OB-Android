package com.nobody.campick.services

import com.nobody.campick.models.Page
import com.nobody.campick.models.Product
import com.nobody.campick.models.ProfileResponse
import com.nobody.campick.services.network.APIService
import com.nobody.campick.services.network.ApiResult
import com.nobody.campick.services.network.Endpoint

object ProfileService {

    /**
     * 회원 정보 조회
     */
    suspend fun fetchMemberInfo(memberId: String): ApiResult<ProfileResponse> {
        return APIService.get<ProfileResponse>(
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
            endpoint = Endpoint.MemberSignout
        )
    }

    /**
     * 닉네임 변경
     */
    suspend fun updateNickname(nickname: String): ApiResult<Unit> {
        return APIService.put<Unit>(
            endpoint = Endpoint.MemberNickname,
            body = mapOf("nickname" to nickname)
        )
    }

    /**
     * 비밀번호 변경
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): ApiResult<Unit> {
        return APIService.put<Unit>(
            endpoint = Endpoint.ChangePassword,
            body = mapOf(
                "currentPassword" to currentPassword,
                "newPassword" to newPassword
            )
        )
    }

    /**
     * 프로필 이미지 업로드
     */
    suspend fun uploadProfileImage(imageData: ByteArray): ApiResult<Unit> {
        return APIService.multipart<Unit>(
            endpoint = Endpoint.MemberImage,
            parts = mapOf("image" to imageData)
        )
    }
}