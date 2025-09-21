package com.nobody.campick.utils

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 앱에서 발생하는 에러를 정의하는 sealed class
 * iOS의 AppError와 동일한 역할
 */
sealed class AppError(override val message: String) : Exception(message) {
    data class Network(val msg: String) : AppError(msg.ifEmpty { "네트워크 연결에 문제가 발생했습니다." })
    object Timeout : AppError("요청이 시간 초과되었습니다. 네트워크 상태를 확인해주세요.")
    object CannotConnect : AppError("서버에 연결할 수 없습니다. 네트워크 또는 서버 상태를 확인해주세요.")
    object HostNotFound : AppError("서버 주소를 찾을 수 없습니다. 도메인 설정을 확인해주세요.")
    object Cancelled : AppError("요청이 취소되었습니다.")
    object Unauthorized : AppError("인증이 필요합니다. 다시 로그인해주세요.")
    object Forbidden : AppError("접근 권한이 없습니다.")
    object NotFound : AppError("요청한 리소스를 찾을 수 없습니다.")
    object Conflict : AppError("요청이 충돌했습니다. 입력값을 확인해주세요.")
    object TooManyRequests : AppError("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
    data class Server(val code: Int, val msg: String?) : AppError(msg ?: "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
    object Decoding : AppError("응답 처리 중 오류가 발생했습니다.")
    data class Unknown(val msg: String) : AppError(msg)

    /**
     * 에러 코드 반환
     */
    val errorCode: Int
        get() = when (this) {
            is Network -> -1
            is Timeout -> -2
            is CannotConnect -> -3
            is HostNotFound -> -4
            is Cancelled -> -5
            is Unauthorized -> 401
            is Forbidden -> 403
            is NotFound -> 404
            is Conflict -> 409
            is TooManyRequests -> 429
            is Server -> code
            is Decoding -> -6
            is Unknown -> -7
        }

    /**
     * 사용자에게 표시할 메시지
     */
    val userMessage: String get() = message

    /**
     * 에러가 재시도 가능한지 여부
     */
    val isRetryable: Boolean
        get() = when (this) {
            is Network, is Timeout, is CannotConnect, is HostNotFound -> true
            is Server -> code >= 500
            is TooManyRequests -> true
            else -> false
        }

    /**
     * 인증 관련 에러인지 여부
     */
    val isAuthenticationError: Boolean
        get() = this is Unauthorized || this is Forbidden
}

/**
 * 예외를 AppError로 변환하는 매퍼
 */
object ErrorMapper {

    /**
     * 일반 Exception을 AppError로 변환
     */
    fun map(exception: Throwable): AppError {
        return when (exception) {
            is AppError -> exception
            is HttpException -> mapHttpException(exception)
            is SocketTimeoutException -> AppError.Timeout
            is ConnectException -> AppError.CannotConnect
            is UnknownHostException -> AppError.HostNotFound
            is IOException -> AppError.Network(exception.message ?: "네트워크 오류")
            is InterruptedException -> AppError.Cancelled
            else -> AppError.Unknown(exception.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    /**
     * HTTP 예외를 AppError로 변환
     */
    private fun mapHttpException(exception: HttpException): AppError {
        return when (exception.code()) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            409 -> AppError.Conflict
            429 -> AppError.TooManyRequests
            in 400..499 -> AppError.Server(exception.code(), "클라이언트 오류가 발생했습니다.")
            in 500..599 -> AppError.Server(exception.code(), "서버 오류가 발생했습니다.")
            else -> AppError.Server(exception.code(), exception.message)
        }
    }

    /**
     * 에러 메시지에서 AppError 생성
     */
    fun fromMessage(message: String): AppError {
        return AppError.Unknown(message)
    }

    /**
     * HTTP 상태 코드에서 AppError 생성
     */
    fun fromHttpCode(code: Int, message: String? = null): AppError {
        return when (code) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            409 -> AppError.Conflict
            429 -> AppError.TooManyRequests
            else -> AppError.Server(code, message)
        }
    }
}