package com.nobody.campick.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailStepViewModel : ViewModel() {

    var remainingSeconds by mutableStateOf(0)
        private set

    var timerActive by mutableStateOf(false)
        private set

    var showExpiredNotice by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    /** 타이머 시작 (기본 180초) */
    fun startTimer(duration: Int = 180) {
        remainingSeconds = duration
        timerActive = true
        showExpiredNotice = false

        // 기존 실행 중인 타이머 취소
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0 && timerActive) {
                delay(1000L)
                tick()
            }
        }
    }

    /** 타이머 중지 */
    fun stopTimer() {
        timerActive = false
        remainingSeconds = 0
        timerJob?.cancel()
    }

    /** 1초 줄이기 */
    private fun tick() {
        if (timerActive && remainingSeconds > 0) {
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                timerActive = false
            }
        }
    }

    /** 만료 상태 표시 */
    fun markExpired() {
        showExpiredNotice = true
    }

    /** 만료 표시 초기화 */
    fun resetExpiredNotice() {
        showExpiredNotice = false
    }

    /** 00:00 형태의 문자열 반환 */
    fun timeString(): String {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}