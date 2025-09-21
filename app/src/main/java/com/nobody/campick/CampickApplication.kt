package com.nobody.campick

import android.app.Application
import com.nobody.campick.managers.KeychainManager
import com.nobody.campick.managers.NetworkMonitor
import com.nobody.campick.managers.UserLocalStorage
import com.nobody.campick.services.network.TokenManager
import com.nobody.campick.utils.AppLog

/**
 * Campick 애플리케이션 클래스
 * iOS의 앱 초기화와 동일한 역할
 */
class CampickApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 앱 로그 기록
        AppLog.lifecycle("Application onCreate")

        // 매니저들 초기화
        initializeManagers()

        AppLog.lifecycle("Application initialized successfully")
    }

    /**
     * 각종 매니저들 초기화
     */
    private fun initializeManagers() {
        // KeychainManager 초기화
        try {
            KeychainManager.initialize(this)
            AppLog.info("KeychainManager initialized", "INIT")
        } catch (e: Exception) {
            AppLog.error("Failed to initialize KeychainManager: ${e.message}", "INIT", e)
        }

        // UserLocalStorage 초기화
        try {
            UserLocalStorage.initialize(this)
            AppLog.info("UserLocalStorage initialized", "INIT")
        } catch (e: Exception) {
            AppLog.error("Failed to initialize UserLocalStorage: ${e.message}", "INIT", e)
        }

        // NetworkMonitor 초기화
        try {
            NetworkMonitor.getInstance(this)
            AppLog.info("NetworkMonitor initialized", "INIT")
        } catch (e: Exception) {
            AppLog.error("Failed to initialize NetworkMonitor: ${e.message}", "INIT", e)
        }

        // TokenManager 초기화
        try {
            TokenManager.initialize(this)
            AppLog.info("TokenManager initialized", "INIT")
        } catch (e: Exception) {
            AppLog.error("Failed to initialize TokenManager: ${e.message}", "INIT", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        // NetworkMonitor 정리
        try {
            NetworkMonitor.shared.stopMonitoring()
            AppLog.lifecycle("Application onTerminate - NetworkMonitor stopped")
        } catch (e: Exception) {
            AppLog.error("Error stopping NetworkMonitor: ${e.message}", "LIFECYCLE", e)
        }

        AppLog.lifecycle("Application terminated")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        AppLog.warn("Application onLowMemory", "LIFECYCLE")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AppLog.warn("Application onTrimMemory - level: $level", "LIFECYCLE")
    }
}