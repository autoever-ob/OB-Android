package com.nobody.campick.services.network

import com.nobody.campick.managers.UserState
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 인증 토큰을 자동으로 추가하고 401 오류 시 토큰 재발급을 시도하는 OkHttp Interceptor
 * iOS의 AuthInterceptor.swift와 동일한 역할 + 자동 토큰 재발급
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // 인증이 필요없는 엔드포인트 체크
        val isAuthEndpoint = url.contains("/api/member/login") ||
                            url.contains("/api/member/signup") ||
                            url.contains("/api/member/email/") ||
                            url.contains("/api/member/reissue")

        val requestBuilder = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")

        // 인증이 필요한 엔드포인트에 토큰 추가
        if (!isAuthEndpoint) {
            val accessToken = TokenManager.getAccessToken()
            println("🔐 토큰 상태 확인: ${if (accessToken.isNotEmpty()) "토큰 있음 (${accessToken.take(20)}...)" else "토큰 없음"}")

            if (accessToken.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $accessToken")
                println("✅ Authorization 헤더 추가됨")
            } else {
                println("❌ 토큰이 없어서 Authorization 헤더 추가하지 않음 - 로그인 필요")
            }
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // 401 오류 시 토큰 재발급 시도
        if (response.code == 401 && !isAuthEndpoint && TokenManager.getRefreshToken().isNotEmpty()) {

            response.close() // 기존 응답 닫기

            return runBlocking {
                val refreshSuccess = TokenManager.refreshAccessToken()

                if (refreshSuccess) {
                    // 새 토큰으로 요청 재시도
                    val newToken = TokenManager.getAccessToken()
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .header("Accept", "application/json")
                        .build()

                    chain.proceed(retryRequest)
                } else {
                    // 재발급 실패 시 전역 로그아웃 처리
                    println("🚪 토큰 재발급 실패 - 전역 로그아웃 실행")
                    UserState.logout()

                    val retryResponse = chain.proceed(request)
                    retryResponse
                }
            }
        }

        return response
    }
}