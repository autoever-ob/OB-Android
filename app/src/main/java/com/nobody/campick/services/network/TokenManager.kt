package com.nobody.campick.services.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * JWT 토큰 관리 클래스
 * iOS의 TokenManager.swift와 동일한 역할
 */
object TokenManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private var sharedPreferences: SharedPreferences? = null
    private val refreshMutex = Mutex() // 토큰 재발급 동시성 제어

    /**
     * TokenManager 초기화
     * Application 클래스에서 호출해야 함
     */
    fun initialize(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // EncryptedSharedPreferences 초기화 실패 시 일반 SharedPreferences 사용
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * 액세스 토큰 저장
     */
    fun saveAccessToken(token: String) {
        sharedPreferences?.edit()
            ?.putString(KEY_ACCESS_TOKEN, token)
            ?.apply()
    }

    /**
     * 리프레시 토큰 저장
     */
    fun saveRefreshToken(token: String) {
        sharedPreferences?.edit()
            ?.putString(KEY_REFRESH_TOKEN, token)
            ?.apply()
    }

    /**
     * 액세스 토큰 반환
     */
    fun getAccessToken(): String {
        return sharedPreferences?.getString(KEY_ACCESS_TOKEN, "") ?: ""
    }

    /**
     * 리프레시 토큰 반환
     */
    fun getRefreshToken(): String {
        return sharedPreferences?.getString(KEY_REFRESH_TOKEN, "") ?: ""
    }

    /**
     * 모든 토큰 삭제
     */
    fun clearTokens() {
        sharedPreferences?.edit()
            ?.remove(KEY_ACCESS_TOKEN)
            ?.remove(KEY_REFRESH_TOKEN)
            ?.apply()
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken().isNotEmpty()
    }

    /**
     * 토큰 재발급 요청
     */
    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = getRefreshToken()
        if (refreshToken.isEmpty()) {
            return false
        }

        return try {
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }

            val requestBody = mapOf("refreshToken" to refreshToken)
            val jsonString = json.encodeToString(requestBody)
            val body = jsonString.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://campick.shop/api/member/reissue")
                .post(body)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                // 서버 응답 파싱
                val apiResponse = json.decodeFromString<com.nobody.campick.models.ApiResponse<TokenResponse>>(responseBody)

                if (apiResponse.success && apiResponse.data != null) {
                    saveAccessToken(apiResponse.data.accessToken)
                    saveRefreshToken(apiResponse.data.refreshToken)
                    true
                } else {
                    clearTokens()
                    false
                }
            } else {
                clearTokens()
                false
            }
        } catch (e: Exception) {
            clearTokens()
            false
        }
    }

    @kotlinx.serialization.Serializable
    private data class TokenResponse(
        val accessToken: String,
        val refreshToken: String
    )
}