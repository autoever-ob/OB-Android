package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.managers.UserState
import com.nobody.campick.services.AuthAPI
import com.nobody.campick.services.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // Inputs
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _keepLoggedIn = MutableStateFlow(false)
    val keepLoggedIn: StateFlow<Boolean> = _keepLoggedIn

    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _showServerAlert = MutableStateFlow(false)
    val showServerAlert: StateFlow<Boolean> = _showServerAlert

    private val _showSignupPrompt = MutableStateFlow(false)
    val showSignupPrompt: StateFlow<Boolean> = _showSignupPrompt

    val isLoginDisabled: Boolean
        get() = email.value.isEmpty() || password.value.isEmpty() || isLoading.value

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun toggleKeepLoggedIn() {
        _keepLoggedIn.value = !_keepLoggedIn.value
    }

    fun dismissServerAlert() {
        _showServerAlert.value = false
    }

    fun dismissSignupPrompt() {
        _showSignupPrompt.value = false
    }

    fun login() {
        if (isLoginDisabled) return

        _isLoading.value = true
        _errorMessage.value = null
        _showServerAlert.value = false
        _showSignupPrompt.value = false

        viewModelScope.launch {
            try {
                // AuthAPI를 사용하여 로그인 시도
                val response = AuthAPI.login(email.value, password.value)

                // 토큰 저장
                TokenManager.saveAccessToken(response.accessToken)
                response.refreshToken?.let { TokenManager.saveRefreshToken(it) }

                // 사용자 데이터 저장
                UserState.applyUserDTO(response.user)
                UserState.saveToken(response.accessToken)

                // 추가 사용자 정보 저장 (AuthResponse에서 직접 받은 데이터)
                if (response.memberId != null) {
                    UserState.saveUserData(
                        name = response.user?.name ?: response.nickname ?: "",
                        nickName = response.nickname ?: response.user?.nickname ?: "",
                        phoneNumber = response.phoneNumber ?: response.user?.mobileNumber ?: "",
                        memberId = response.memberId,
                        dealerId = response.dealerId ?: "",
                        role = response.role ?: "",
                        email = email.value,
                        profileImageUrl = response.profileImageUrl ?: "",
                        joinDate = response.user?.createdAt ?: ""
                    )
                }

                println("🎉 Login successful - User logged in: ${UserState.isLoggedIn.value}")

            } catch (e: Exception) {
                // 에러 처리 - iOS의 AppError 매핑과 유사
                val errorMessage = when {
                    e.message?.contains("404") == true || e.message?.contains("User not found") == true -> {
                        _showSignupPrompt.value = true
                        "등록되지 않은 이메일입니다. 회원가입을 해주세요."
                    }
                    e.message?.contains("401") == true || e.message?.contains("Invalid") == true -> {
                        "이메일 또는 비밀번호가 올바르지 않습니다."
                    }
                    e.message?.contains("Network") == true || e.message?.contains("timeout") == true -> {
                        _showServerAlert.value = true
                        "네트워크 연결을 확인해주세요."
                    }
                    else -> {
                        _showServerAlert.value = true
                        e.message ?: "알 수 없는 오류가 발생했습니다."
                    }
                }
                _errorMessage.value = errorMessage
                println("❌ Login failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}