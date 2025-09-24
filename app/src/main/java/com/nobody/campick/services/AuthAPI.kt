package com.nobody.campick.services

import com.nobody.campick.models.auth.AuthResponse
import com.nobody.campick.models.auth.LoginDataDTO
import com.nobody.campick.models.auth.LoginRequest
import com.nobody.campick.services.network.APIService
import com.nobody.campick.services.network.ApiResult
import com.nobody.campick.services.network.Endpoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * iOS의 AuthAPI.swift와 동일한 역할을 하는 Android 구현
 * 인증 관련 API 호출을 담당
 */
object AuthAPI {

    /**
     * 로그인: 이메일과 비밀번호로 인증 후 토큰/유저 정보 수신
     */
    suspend fun login(email: String, password: String): AuthResponse {
        val request = LoginRequest(email = email, password = password)
        // 직접 직렬화하여 String으로 전달
        val jsonRequest = Json.encodeToString(request)

        when (val result = APIService.post<LoginDataDTO>(Endpoint.Login, jsonRequest)) {
            is ApiResult.Success -> {
                val data = result.data
                println("🔐 Login successful - accessToken length: ${data.accessToken.length}")
                println("🔐 Login successful - refreshToken exists: ${data.refreshToken != null}")
                println("🔐 Login successful - memberId: ${data.memberId}")
                println("🔐 Login successful - dealerId: ${data.dealerId}")
                println("🔐 Login successful - role: ${data.role}")

                return AuthResponse(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                    user = data.user,
                    memberId = data.memberIdString,  // Int를 String으로 변환
                    dealerId = data.dealerIdString,  // Int를 String으로 변환
                    profileImageUrl = data.profileImageUrl,
                    profileThumbnailUrl = data.profileThumbnailUrl,
                    phoneNumber = data.phoneNumber,
                    role = data.role,
                    nickname = data.nickname
                )
            }
            is ApiResult.Error -> {
                throw Exception("Login failed: ${result.message}")
            }
        }
    }

    /**
     * 로그아웃: 서버 세션/토큰 무효화 요청
     */
    suspend fun logout() {
        when (val result = APIService.post<Unit>(Endpoint.Logout)) {
            is ApiResult.Success -> {
                println("🚪 Logout successful")
            }
            is ApiResult.Error -> {
                throw Exception("Logout failed: ${result.message}")
            }
        }
    }

    /**
     * 토큰 재발급: 저장된 자격으로 액세스 토큰 재요청
     */
    suspend fun reissueAccessToken(): String {
        when (val result = APIService.post<LoginDataDTO>(Endpoint.TokenReissue)) {
            is ApiResult.Success -> {
                return result.data.accessToken
            }
            is ApiResult.Error -> {
                throw Exception("Token reissue failed: ${result.message}")
            }
        }
    }

    /**
     * 이메일 인증코드 발송: 회원가입/검증용 이메일 코드 전송
     * Swift AuthAPI.sendEmailCode와 동일
     */
    suspend fun sendEmailCode(email: String): ApiResult<Unit> {
        val json = """{"email":"$email"}"""
        return APIService.post(Endpoint.EmailSend, json)
    }

    /**
     * 이메일 인증코드 확인: 수신한 코드 검증
     * Swift AuthAPI.confirmEmailCode와 동일 (code만 전달)
     */
    suspend fun confirmEmailCode(code: String): ApiResult<Unit> {
        val json = """{"code":"$code"}"""
        return APIService.post(Endpoint.EmailVerify, json)
    }

    /**
     * 회원가입: 필수 정보로 회원 생성 요청
     * Swift AuthAPI.signup와 동일
     */
    suspend fun signup(
        email: String,
        password: String,
        checkedPassword: String,
        nickname: String,
        mobileNumber: String,
        role: String,
        dealershipName: String = "",
        dealershipRegistrationNumber: String = ""
    ): ApiResult<Unit> {
        val json = buildString {
            append("{")
            append("\"email\":\"$email\",")
            append("\"password\":\"$password\",")
            append("\"checkedPassword\":\"$checkedPassword\",")
            append("\"nickname\":\"$nickname\",")
            append("\"mobileNumber\":\"$mobileNumber\",")
            append("\"role\":\"$role\",")
            append("\"dealershipName\":\"$dealershipName\",")
            append("\"dealershipRegistrationNumber\":\"$dealershipRegistrationNumber\"")
            append("}")
        }

        return APIService.post(Endpoint.Signup, json)
    }
}