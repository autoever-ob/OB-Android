package com.nobody.campick.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SignupStep { Email, Password, Phone, Nickname, Complete }
enum class UserType { Normal, Dealer }

class SignupFlowViewModel : ViewModel() {

    // Navigation / Progress
    var step by mutableStateOf(SignupStep.Email)
        private set
    var prevProgress by mutableStateOf(0f)

    val progress: Float
        get() = when (step) {
            SignupStep.Email -> 0.25f
            SignupStep.Password -> 0.5f
            SignupStep.Phone -> 0.75f
            SignupStep.Nickname -> 0.9f
            SignupStep.Complete -> 1f
        }

    fun go(to: SignupStep) {
        prevProgress = progress
        step = to
    }

    fun title(): String = when (step) {
        SignupStep.Email -> "회원가입"
        SignupStep.Password -> "비밀번호 설정"
        SignupStep.Phone -> "휴대폰 인증"
        SignupStep.Nickname -> "닉네임 설정"
        SignupStep.Complete -> "가입 완료"
    }

    fun goBack(dismiss: () -> Unit) {
        when (step) {
            SignupStep.Email -> dismiss()
            SignupStep.Password -> go(SignupStep.Email)
            SignupStep.Phone -> go(SignupStep.Password)
            SignupStep.Nickname -> go(SignupStep.Phone)
            SignupStep.Complete -> go(SignupStep.Nickname)
        }
    }

    // MARK: - Shared inputs
    var userType by mutableStateOf<UserType?>(null)

    // Email
    var email by mutableStateOf("")
    var showEmailCodeField by mutableStateOf(false)
    var emailCode by mutableStateOf("")
    var emailError by mutableStateOf<String?>(null)
    var isEmailSending by mutableStateOf(false)
    var showEmailMismatchModal by mutableStateOf(false)
    var showEmailDuplicateModal by mutableStateOf(false)
    var termsAgreed by mutableStateOf(false)
    var privacyAgreed by mutableStateOf(false)
    var emailVerified by mutableStateOf(false)
    var shouldNavigateHome by mutableStateOf(false)

    fun onEmailCodeChange(value: String) {
        emailCode = value.filter { it.isDigit() }
    }

    fun onUserTypeChange(type: UserType) {
        userType = type
    }

    fun onEmailChange(value: String) {
        email = value
    }

    fun onTermsChange(checked: Boolean) {
        termsAgreed = checked
    }

    fun onPrivacyChange(checked: Boolean) {
        privacyAgreed = checked
    }

    fun emailNext() {
        if (emailCode.isNotEmpty() && termsAgreed && privacyAgreed) {
            showEmailMismatchModal = false
            showEmailDuplicateModal = false
            confirmEmail()
        }
    }

    // Password
    var password by mutableStateOf("")
    var confirm by mutableStateOf("")
    var passwordError by mutableStateOf<String?>(null)

    fun passwordNext() {
        if (password.length >= 8 && confirm == password) {
            passwordError = null
            go(SignupStep.Phone)
        } else {
            passwordError = "비밀번호가 일치하지 않거나 8자 미만입니다."
        }
    }

    // Phone
    var phone by mutableStateOf("")
    var showPhoneCodeField by mutableStateOf(false)
    var phoneCode by mutableStateOf("")
    var phoneError by mutableStateOf<String?>(null)
    var codeVerified by mutableStateOf(false)
    var showDealerField by mutableStateOf(false)
    var dealerNumber by mutableStateOf("")

    fun phoneOnChangePhone(value: String) {
        phone = value.filter { it.isDigit() }
    }

    fun phoneOnChangeCode(value: String) {
        phoneCode = value.filter { it.isDigit() }
    }

    fun phoneNext() {
        val hasPhone = phone.isNotEmpty()
        val codeOK = (phoneCode == "0000")
        if (hasPhone && codeOK) {
            phoneError = null
            codeVerified = true
            if (userType == UserType.Dealer) {
                showDealerField = true
            } else {
                go(SignupStep.Nickname)
            }
        } else {
            phoneError = "인증번호 또는 휴대폰 번호를 확인하세요."
        }
    }

    fun onDealerVerified() {
        if (dealerNumber == "0000") {
            phoneError = null
            go(SignupStep.Nickname)
        } else {
            phoneError = "딜러 번호가 올바르지 않습니다."
        }
    }

    // Nickname
    var nickname by mutableStateOf("")
    var selectedImage by mutableStateOf("") // Android에서는 Bitmap/Uri 사용
    var showCamera by mutableStateOf(false)
    var showGallery by mutableStateOf(false)
    val nicknameValid: Boolean
        get() = nickname.trim().length >= 2
    var isSubmitting by mutableStateOf(false)
    var submitError by mutableStateOf<String?>(null)
    var showServerAlert by mutableStateOf(false)

    fun nicknameNext() {
        if (nicknameValid) {
            submitSignup()
        }
    }

    fun startPhoneCodeFlow() {
        // 필요 시 서버로 코드 발송 API 호출 전/후 상태 처리
        showPhoneCodeField = true
        phoneCode = ""
        phoneError = null
    }

    private fun phoneDashed(): String {
        val digits = phone.filter { it.isDigit() }
        return when (digits.length) {
            11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
            10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
            else -> phone
        }
    }

    // --- 네트워크 통신 부분은 실제 Retrofit API 호출로 교체 필요 ---
    private fun submitSignup() {
        if (userType == null) return
        viewModelScope.launch {
            isSubmitting = true
            submitError = null
            delay(1000) // 네트워크 시뮬레이션
            // 성공 가정
            go(SignupStep.Complete)
            isSubmitting = false
        }
    }

    fun sendEmailCode() {
        if (email.isEmpty() || !termsAgreed || !privacyAgreed) return
        viewModelScope.launch {
            isEmailSending = true
            emailError = null
            delay(1000) // 네트워크 시뮬레이션
            showEmailCodeField = true
            showEmailMismatchModal = false
            showEmailDuplicateModal = false
            isEmailSending = false
        }
    }

    private fun confirmEmail() {
        if (emailCode.isEmpty()) return
        viewModelScope.launch {
            delay(500) // 네트워크 시뮬레이션
            if (emailCode == "0000") {
                emailVerified = true
                showEmailMismatchModal = false
                go(SignupStep.Password)
            } else {
                emailVerified = false
                showEmailMismatchModal = true
                emailCode = ""
                showEmailDuplicateModal = false
            }
        }
    }

    fun autoLoginAfterSignup() {
        viewModelScope.launch {
            delay(1000) // 네트워크 시뮬레이션
            shouldNavigateHome = true
        }
    }
}