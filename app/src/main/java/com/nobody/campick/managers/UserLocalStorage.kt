package com.nobody.campick.managers

import android.content.Context
import android.content.SharedPreferences

/**
 * 사용자 설정을 저장/조회하는 매니저
 * Android의 SharedPreferences를 활용한 로컬 스토리지
 */
object UserLocalStorage {

    private const val PREFS_NAME = "campick_user_prefs"
    private var prefs: SharedPreferences? = null

    /**
     * 초기화 메서드 - Application에서 호출
     */
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 자주 사용하는 설정 키들
    object Keys {
        const val USER_ID = "user_id"
        const val USER_NICKNAME = "user_nickname"
        const val FIRST_LAUNCH = "first_launch"
        const val THEME_MODE = "theme_mode"
        const val NOTIFICATION_ENABLED = "notification_enabled"
        const val AUTO_LOGIN = "auto_login"
        const val LAST_SYNC_TIME = "last_sync_time"
    }

    /**
     * 문자열 값 저장
     */
    fun putString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }

    /**
     * 문자열 값 조회
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return prefs?.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * 불린 값 저장
     */
    fun putBoolean(key: String, value: Boolean) {
        prefs?.edit()?.putBoolean(key, value)?.apply()
    }

    /**
     * 불린 값 조회
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs?.getBoolean(key, defaultValue) ?: defaultValue
    }

    /**
     * 정수 값 저장
     */
    fun putInt(key: String, value: Int) {
        prefs?.edit()?.putInt(key, value)?.apply()
    }

    /**
     * 정수 값 조회
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs?.getInt(key, defaultValue) ?: defaultValue
    }

    /**
     * 실수 값 저장
     */
    fun putFloat(key: String, value: Float) {
        prefs?.edit()?.putFloat(key, value)?.apply()
    }

    /**
     * 실수 값 조회
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return prefs?.getFloat(key, defaultValue) ?: defaultValue
    }

    /**
     * Long 값 저장
     */
    fun putLong(key: String, value: Long) {
        prefs?.edit()?.putLong(key, value)?.apply()
    }

    /**
     * Long 값 조회
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return prefs?.getLong(key, defaultValue) ?: defaultValue
    }

    /**
     * 특정 키의 값 삭제
     */
    fun remove(key: String) {
        prefs?.edit()?.remove(key)?.apply()
    }

    /**
     * 모든 값 삭제
     */
    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }

    /**
     * 키 존재 여부 확인
     */
    fun contains(key: String): Boolean {
        return prefs?.contains(key) ?: false
    }

    /**
     * 모든 키 목록 반환
     */
    fun getAllKeys(): Set<String> {
        return prefs?.all?.keys ?: emptySet()
    }

    // 편의 메서드들

    /**
     * 사용자 ID 저장
     */
    fun setUserId(userId: String) {
        putString(Keys.USER_ID, userId)
    }

    /**
     * 사용자 ID 조회
     */
    fun getUserId(): String {
        return getString(Keys.USER_ID)
    }

    /**
     * 첫 실행 여부 확인
     */
    fun isFirstLaunch(): Boolean {
        return getBoolean(Keys.FIRST_LAUNCH, true)
    }

    /**
     * 첫 실행 완료 표시
     */
    fun setFirstLaunchCompleted() {
        putBoolean(Keys.FIRST_LAUNCH, false)
    }

    /**
     * 자동 로그인 설정
     */
    fun setAutoLogin(enabled: Boolean) {
        putBoolean(Keys.AUTO_LOGIN, enabled)
    }

    /**
     * 자동 로그인 여부 확인
     */
    fun isAutoLoginEnabled(): Boolean {
        return getBoolean(Keys.AUTO_LOGIN, false)
    }
}