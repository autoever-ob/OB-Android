package com.nobody.campick.services.network

import com.nobody.campick.models.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 중앙화된 API 요청 서비스
 * iOS의 APIService.swift와 동일한 역할
 */
object APIService {

    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @PublishedApi
    internal val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor())
        .addInterceptor(LoggingInterceptor())
        .build()

    /**
     * GET 요청
     */
    suspend inline fun <reified T> get(
        endpoint: Endpoint,
        queryParams: Map<String, String> = emptyMap()
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = try {
                endpoint.url.toHttpUrl().newBuilder()
            } catch (e: IllegalArgumentException) {
                return@withContext ApiResult.Error("Invalid URL: ${endpoint.url}")
            }

            queryParams.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value)
            }

            val request = Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build()

            executeRequest<T>(request)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * POST 요청
     */
    suspend inline fun <reified T> post(
        endpoint: Endpoint,
        body: Any? = null
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val requestBody = body?.let {
                val jsonString = json.encodeToString(it)
                jsonString.toRequestBody("application/json".toMediaType())
            } ?: "".toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(endpoint.url)
                .post(requestBody)
                .build()

            executeRequest<T>(request)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * PUT 요청
     */
    suspend inline fun <reified T> put(
        endpoint: Endpoint,
        body: Any? = null
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val requestBody = body?.let {
                val jsonString = json.encodeToString(it)
                jsonString.toRequestBody("application/json".toMediaType())
            } ?: "".toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(endpoint.url)
                .put(requestBody)
                .build()

            executeRequest<T>(request)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * DELETE 요청
     */
    suspend inline fun <reified T> delete(
        endpoint: Endpoint
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(endpoint.url)
                .delete()
                .build()

            executeRequest<T>(request)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * 멀티파트 요청 (파일 업로드)
     */
    suspend inline fun <reified T> multipart(
        endpoint: Endpoint,
        parts: Map<String, Any>
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

            parts.forEach { (key, value) ->
                when (value) {
                    is String -> {
                        multipartBuilder.addFormDataPart(key, value)
                    }
                    is ByteArray -> {
                        val requestBody = value.toRequestBody("image/*".toMediaType())
                        multipartBuilder.addFormDataPart(key, "image.jpg", requestBody)
                    }
                    else -> {
                        val jsonString = json.encodeToString(value)
                        multipartBuilder.addFormDataPart(key, jsonString)
                    }
                }
            }

            val request = Request.Builder()
                .url(endpoint.url)
                .post(multipartBuilder.build())
                .build()

            executeRequest<T>(request)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * 요청 실행 및 응답 처리
     */
    suspend inline fun <reified T> executeRequest(request: Request): ApiResult<T> {
        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            try {
                // 서버 응답을 ApiResponse로 파싱
                println("🔍 Parsing response for type: ${T::class.simpleName}")
                println("🔍 Response body: $responseBody")

                val apiResponse = json.decodeFromString<ApiResponse<T>>(responseBody)
                println("✅ Successfully parsed ApiResponse")

                when {
                    apiResponse.success && apiResponse.data != null -> {
                        println("✅ Success with data")
                        ApiResult.Success(apiResponse.data)
                    }
                    apiResponse.success && T::class == Unit::class -> {
                        println("✅ Success without data (Unit)")
                        @Suppress("UNCHECKED_CAST")
                        ApiResult.Success(Unit as T)
                    }
                    !apiResponse.success -> {
                        // 서버에서 success: false로 응답한 경우
                        println("❌ Server returned success: false")
                        if (apiResponse.status == 401) {
                            TokenManager.clearTokens()
                        }
                        ApiResult.Error(apiResponse.message)
                    }
                    else -> {
                        println("❌ No data in successful response")
                        ApiResult.Error("서버 응답에 데이터가 없습니다")
                    }
                }
            } catch (e: Exception) {
                println("💥 JSON parsing failed: ${e.message}")
                e.printStackTrace()
                // JSON 파싱 실패 시 HTTP 상태 코드로 처리
                when {
                    response.isSuccessful -> {
                        ApiResult.Error("응답 파싱 실패: ${e.message}")
                    }
                    response.code == 401 -> {
                        TokenManager.clearTokens()
                        ApiResult.Error("인증이 필요합니다")
                    }
                    response.code in 400..499 -> {
                        ApiResult.Error("클라이언트 오류 (${response.code}): ${response.message}")
                    }
                    response.code in 500..599 -> {
                        ApiResult.Error("서버 오류 (${response.code}): ${response.message}")
                    }
                    else -> {
                        ApiResult.Error("HTTP ${response.code}: ${response.message}")
                    }
                }
            }
        } catch (e: IOException) {
            ApiResult.Error("네트워크 연결 오류: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("알 수 없는 오류: ${e.message}")
        }
    }
}

/**
 * API 응답 결과를 나타내는 sealed class
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }

    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): ApiResult<T> {
        if (this is Error) action(message)
        return this
    }
}