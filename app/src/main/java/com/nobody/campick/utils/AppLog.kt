package com.nobody.campick.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * 로그 레벨 정의
 */
enum class AppLogLevel(val value: String) {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR")
}

/**
 * 앱 전체 로그 관리 유틸리티
 * iOS의 AppLog와 동일한 역할
 */
object AppLog {

    // 디버그 모드에서만 로그 활성화
    private val enabled = true // Debug 빌드에서만 활성화

    private const val DEFAULT_TAG = "CAMPICK"

    /**
     * 디버그 로그
     */
    fun debug(message: String, category: String = "APP") {
        log(AppLogLevel.DEBUG, message, category)
    }

    /**
     * 정보 로그
     */
    fun info(message: String, category: String = "APP") {
        log(AppLogLevel.INFO, message, category)
    }

    /**
     * 경고 로그
     */
    fun warn(message: String, category: String = "APP") {
        log(AppLogLevel.WARN, message, category)
    }

    /**
     * 에러 로그
     */
    fun error(message: String, category: String = "APP", throwable: Throwable? = null) {
        log(AppLogLevel.ERROR, message, category, throwable)
    }

    /**
     * 기본 로그 함수
     */
    fun log(level: AppLogLevel, message: String, category: String = "APP", throwable: Throwable? = null) {
        if (!enabled) return

        val tag = "[$category]"
        val logMessage = "${level.value} $message"

        when (level) {
            AppLogLevel.DEBUG -> Log.d(tag, logMessage, throwable)
            AppLogLevel.INFO -> Log.i(tag, logMessage, throwable)
            AppLogLevel.WARN -> Log.w(tag, logMessage, throwable)
            AppLogLevel.ERROR -> Log.e(tag, logMessage, throwable)
        }
    }

    // MARK: - Network helpers

    /**
     * 네트워크 요청 로그
     */
    fun logRequest(method: String, url: String, body: String? = null) {
        if (!enabled) return

        var logMessage = "[REQUEST] $method $url"
        if (!body.isNullOrEmpty()) {
            val maskedBody = maskSensitiveInJson(body)
            logMessage += "\n   body: $maskedBody"
        }
        Log.d("[NETWORK]", logMessage)
    }

    /**
     * 네트워크 응답 로그
     */
    fun logResponse(
        status: Int,
        method: String,
        url: String,
        data: String? = null,
        error: String? = null
    ) {
        if (!enabled) return

        val logMessage = if (error != null) {
            var msg = "[RESPONSE] ($status) $method $url - error: $error"
            if (!data.isNullOrEmpty()) {
                msg += "\n   body: $data"
            }
            msg
        } else {
            "[RESPONSE] ($status) $method $url"
        }

        if (error != null) {
            Log.e("[NETWORK]", logMessage)
        } else {
            Log.d("[NETWORK]", logMessage)
        }
    }

    /**
     * API 에러 로그
     */
    fun logApiError(method: String, url: String, exception: Throwable) {
        if (!enabled) return
        Log.e("[API_ERROR]", "[REQUEST] $method $url", exception)
    }

    // MARK: - Masking utilities

    /**
     * JSON 문자열에서 민감한 정보 마스킹
     */
    fun maskSensitiveInJson(jsonString: String): String {
        return try {
            when {
                jsonString.trimStart().startsWith("{") -> {
                    val jsonObject = JSONObject(jsonString)
                    maskSensitiveInJsonObject(jsonObject).toString(2)
                }
                jsonString.trimStart().startsWith("[") -> {
                    val jsonArray = JSONArray(jsonString)
                    maskSensitiveInJsonArray(jsonArray).toString(2)
                }
                else -> jsonString
            }
        } catch (e: Exception) {
            jsonString
        }
    }

    /**
     * JSONObject에서 민감한 정보 마스킹
     */
    private fun maskSensitiveInJsonObject(jsonObject: JSONObject): JSONObject {
        val sensitiveKeys = setOf("password", "checkedPassword", "code", "token", "refreshToken", "accessToken")
        val result = JSONObject()

        jsonObject.keys().forEach { key ->
            val value = jsonObject.get(key)
            result.put(key, when {
                sensitiveKeys.contains(key.lowercase()) -> "***"
                value is JSONObject -> maskSensitiveInJsonObject(value)
                value is JSONArray -> maskSensitiveInJsonArray(value)
                else -> value
            })
        }

        return result
    }

    /**
     * JSONArray에서 민감한 정보 마스킹
     */
    private fun maskSensitiveInJsonArray(jsonArray: JSONArray): JSONArray {
        val result = JSONArray()

        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            result.put(when (value) {
                is JSONObject -> maskSensitiveInJsonObject(value)
                is JSONArray -> maskSensitiveInJsonArray(value)
                else -> value
            })
        }

        return result
    }

    // MARK: - Convenience methods

    /**
     * 앱 생명주기 로그
     */
    fun lifecycle(event: String, screen: String = "") {
        info("$event${if (screen.isNotEmpty()) " - $screen" else ""}", "LIFECYCLE")
    }

    /**
     * 데이터베이스 작업 로그
     */
    fun database(operation: String, table: String = "", details: String = "") {
        debug("$operation${if (table.isNotEmpty()) " - $table" else ""}${if (details.isNotEmpty()) " - $details" else ""}", "DATABASE")
    }

    /**
     * 캐시 작업 로그
     */
    fun cache(operation: String, key: String = "", details: String = "") {
        debug("$operation${if (key.isNotEmpty()) " - $key" else ""}${if (details.isNotEmpty()) " - $details" else ""}", "CACHE")
    }

    /**
     * 사용자 액션 로그
     */
    fun userAction(action: String, screen: String = "", details: String = "") {
        info("$action${if (screen.isNotEmpty()) " - $screen" else ""}${if (details.isNotEmpty()) " - $details" else ""}", "USER_ACTION")
    }
}