package com.nobody.campick.utils

import kotlinx.coroutines.flow.*

/**
 * 앱 내부 이벤트/알림을 관리하는 시스템
 * iOS의 Notification.Name 확장과 동일한 역할
 */
object Notifications {

    /**
     * 이벤트 타입 정의
     */
    sealed class Event {
        /** 토큰 재발급 실패 시 사용자에게 재로그인을 요청하기 위한 알림 */
        object TokenReissueFailed : Event()

        /** 네트워크 연결 상태 변경 */
        data class NetworkStateChanged(val isConnected: Boolean) : Event()

        /** 사용자 로그아웃 */
        object UserLoggedOut : Event()

        /** 사용자 로그인 */
        data class UserLoggedIn(val userId: String) : Event()

        /** 프로필 업데이트 */
        object ProfileUpdated : Event()

        /** 새로운 메시지 수신 */
        data class NewMessageReceived(val messageId: String) : Event()

        /** 커스텀 이벤트 */
        data class Custom(val name: String, val data: Map<String, Any> = emptyMap()) : Event()
    }

    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 10
    )

    /**
     * 이벤트 스트림 - 여러 곳에서 구독 가능
     */
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /**
     * 이벤트 발송
     */
    fun post(event: Event) {
        _events.tryEmit(event)
    }

    /**
     * 특정 타입의 이벤트만 구독
     */
    inline fun <reified T : Event> subscribe(): Flow<T> {
        return events
            .filter { it is T }
            .map { it as T }
    }

    // MARK: - Convenience methods

    /**
     * 토큰 재발급 실패 알림 발송
     */
    fun postTokenReissueFailed() {
        post(Event.TokenReissueFailed)
    }

    /**
     * 네트워크 상태 변경 알림 발송
     */
    fun postNetworkStateChanged(isConnected: Boolean) {
        post(Event.NetworkStateChanged(isConnected))
    }

    /**
     * 사용자 로그아웃 알림 발송
     */
    fun postUserLoggedOut() {
        post(Event.UserLoggedOut)
    }

    /**
     * 사용자 로그인 알림 발송
     */
    fun postUserLoggedIn(userId: String) {
        post(Event.UserLoggedIn(userId))
    }

    /**
     * 프로필 업데이트 알림 발송
     */
    fun postProfileUpdated() {
        post(Event.ProfileUpdated)
    }

    /**
     * 새로운 메시지 수신 알림 발송
     */
    fun postNewMessageReceived(messageId: String) {
        post(Event.NewMessageReceived(messageId))
    }

    /**
     * 커스텀 이벤트 발송
     */
    fun postCustom(name: String, data: Map<String, Any> = emptyMap()) {
        post(Event.Custom(name, data))
    }
}

/**
 * Flow 확장 함수들
 */

/**
 * 특정 이벤트 타입만 필터링하는 확장 함수
 */
inline fun <reified T : Notifications.Event> SharedFlow<Notifications.Event>.filterIsInstance(): Flow<T> {
    return filter { it is T }.map { it as T }
}

/**
 * 이벤트 구독을 위한 편의 함수
 */
suspend fun SharedFlow<Notifications.Event>.onTokenReissueFailed(action: suspend () -> Unit) {
    this.filterIsInstance<Notifications.Event.TokenReissueFailed>()
        .collect { action() }
}

suspend fun SharedFlow<Notifications.Event>.onNetworkStateChanged(action: suspend (Boolean) -> Unit) {
    this.filterIsInstance<Notifications.Event.NetworkStateChanged>()
        .collect { action(it.isConnected) }
}

suspend fun SharedFlow<Notifications.Event>.onUserLoggedOut(action: suspend () -> Unit) {
    this.filterIsInstance<Notifications.Event.UserLoggedOut>()
        .collect { action() }
}

suspend fun SharedFlow<Notifications.Event>.onProfileUpdated(action: suspend () -> Unit) {
    this.filterIsInstance<Notifications.Event.ProfileUpdated>()
        .collect { action() }
}