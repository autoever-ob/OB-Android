package com.nobody.campick.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * iOS의 UserDefaultsManager와 동일한 역할을 하는 Android 구현
 * 사용자 데이터를 암호화된 SharedPreferences에 저장
 */
object UserPreferences {

    private const val PREFS_NAME = "user_prefs"
    private var sharedPreferences: SharedPreferences? = null

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

    fun getString(key: String): String? {
        return sharedPreferences?.getString(key, null)
    }

    fun setString(value: String, key: String) {
        sharedPreferences?.edit()
            ?.putString(key, value)
            ?.apply()
    }

    fun removeValue(key: String) {
        sharedPreferences?.edit()
            ?.remove(key)
            ?.apply()
    }

    fun clear() {
        sharedPreferences?.edit()
            ?.clear()
            ?.apply()
    }
}