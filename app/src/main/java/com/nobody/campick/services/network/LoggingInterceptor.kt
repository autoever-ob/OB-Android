package com.nobody.campick.services.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

/**
 * API 요청/응답 로깅을 위한 Interceptor
 * iOS의 APILogger.swift와 동일한 역할
 */
class LoggingInterceptor : Interceptor {

    private val utf8 = Charset.forName("UTF-8")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 요청 로깅
        val requestStartTime = System.nanoTime()
        Log.d("API", "🌐 Request: ${request.method} ${request.url}")

        // 요청 헤더 로깅
        request.headers.forEach { (name, value) ->
            // Authorization 헤더는 보안상 일부만 로깅
            if (name.equals("Authorization", ignoreCase = true)) {
                Log.d("API", "📋 $name: ${value.take(20)}...")
            } else {
                Log.d("API", "📋 $name: $value")
            }
        }

        // 요청 바디 로깅
        request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                val bodyString = buffer.readString(utf8)
                if (bodyString.isNotEmpty()) {
                    Log.d("API", "📤 Request Body: $bodyString")
                }
            } catch (e: Exception) {
                Log.d("API", "📤 Request Body: [Binary Data]")
            }
        }

        // 요청 실행
        val response = chain.proceed(request)
        val requestEndTime = System.nanoTime()
        val requestDuration = (requestEndTime - requestStartTime) / 1_000_000 // ms로 변환

        // 응답 로깅
        Log.d("API", "📱 Response: ${response.code} ${response.message} (${requestDuration}ms)")

        // 응답 헤더 로깅
        response.headers.forEach { (name, value) ->
            Log.d("API", "📋 $name: $value")
        }

        // 응답 바디 로깅
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer

            val contentType = responseBody.contentType()
            val charset = contentType?.charset(utf8) ?: utf8

            if (buffer.size > 0) {
                val bodyString = buffer.clone().readString(charset)
                Log.d("API", "📥 Response Body: $bodyString")
            }
        }

        return response
    }
}