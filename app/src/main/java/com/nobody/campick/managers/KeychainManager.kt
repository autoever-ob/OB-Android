package com.nobody.campick.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 토큰을 안전하게 저장/조회하는 매니저
 * iOS의 KeychainManager와 동일한 역할
 */
object KeychainManager {

    private const val PREFS_NAME = "campick_secure_prefs"
    private var encryptedPrefs: SharedPreferences? = null

    /**
     * 초기화 메서드 - Application에서 호출
     */
    fun initialize(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // 암호화 실패 시 일반 SharedPreferences 사용 (fallback)
            encryptedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * 토큰 저장
     */
    fun saveToken(token: String, forKey: String) {
        try {
            encryptedPrefs?.edit()?.putString(forKey, token)?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 토큰 조회
     */
    fun getToken(forKey: String): String? {
        return try {
            encryptedPrefs?.getString(forKey, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 특정 토큰 삭제
     */
    fun deleteToken(forKey: String) {
        try {
            encryptedPrefs?.edit()?.remove(forKey)?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 모든 토큰 삭제
     */
    fun deleteAllTokens() {
        try {
            encryptedPrefs?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 토큰 존재 여부 확인
     */
    fun hasToken(forKey: String): Boolean {
        return try {
            encryptedPrefs?.contains(forKey) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}