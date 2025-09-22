package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                // TODO: 실제 API 연동 부분 (Retrofit 등 사용)
                // val response = AuthApi.login(email.value, password.value)

                // 예시: 성공 케이스
                // TokenManager.saveAccessToken(response.accessToken)
                // UserState.applyUserDTO(response.user)

            } catch (e: Exception) {
                // TODO: Swift 코드의 AppError 매핑에 해당하는 부분
                // 여기선 단순 처리 예시
                _errorMessage.value = e.localizedMessage ?: "알 수 없는 오류가 발생했습니다."
                _showServerAlert.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}