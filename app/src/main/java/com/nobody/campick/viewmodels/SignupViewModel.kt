package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.auth.UserType
import com.nobody.campick.services.AuthAPI
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {

    enum class Step { EMAIL, PASSWORD, PHONE, NICKNAME, COMPLETE }

    private val _step = MutableStateFlow(Step.EMAIL)
    val step: StateFlow<Step> = _step.asStateFlow()

    private val _prevProgress = MutableStateFlow(0.0f)
    val prevProgress: StateFlow<Float> = _prevProgress.asStateFlow()

    // 공통 입력
    private val _userType = MutableStateFlow<UserType?>(null)
    val userType: StateFlow<UserType?> = _userType.asStateFlow()

    fun setUserType(type: UserType) {
        _userType.value = type
    }

    // Email Step
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _showEmailCodeField = MutableStateFlow(false)
    val showEmailCodeField: StateFlow<Boolean> = _showEmailCodeField.asStateFlow()

    private val _emailCode = MutableStateFlow("")
    val emailCode: StateFlow<String> = _emailCode.asStateFlow()

    private val _termsAgreed = MutableStateFlow(false)
    val termsAgreed: StateFlow<Boolean> = _termsAgreed.asStateFlow()

    private val _privacyAgreed = MutableStateFlow(false)
    val privacyAgreed: StateFlow<Boolean> = _privacyAgreed.asStateFlow()

    private val _showEmailMismatchModal = MutableStateFlow(false)
    val showEmailMismatchModal: StateFlow<Boolean> = _showEmailMismatchModal.asStateFlow()

    private val _showEmailDuplicateModal = MutableStateFlow(false)
    val showEmailDuplicateModal: StateFlow<Boolean> = _showEmailDuplicateModal.asStateFlow()

    fun setEmail(value: String) { _email.value = value }
    fun setEmailCode(value: String) { _emailCode.value = value.filter { it.isDigit() } }
    fun setTermsAgreed(value: Boolean) { _termsAgreed.value = value }
    fun setPrivacyAgreed(value: Boolean) { _privacyAgreed.value = value }
    fun showEmailCodeField() { _showEmailCodeField.value = true }

    suspend fun sendEmailCode() {
        when (val result = AuthAPI.sendEmailCode(_email.value)) {
            is ApiResult.Success -> {
                // 인증 코드 전송 성공
            }
            is ApiResult.Error -> {
                if (result.message.contains("이미 가입된") || result.message.contains("400")) {
                    _showEmailDuplicateModal.value = true
                }
            }
        }
    }

    suspend fun confirmEmail() {
        when (val result = AuthAPI.confirmEmailCode(_emailCode.value)) {
            is ApiResult.Success -> {
                goTo(Step.PASSWORD)
            }
            is ApiResult.Error -> {
                _showEmailMismatchModal.value = true
            }
        }
    }

    fun emailNext() {
        viewModelScope.launch {
            confirmEmail()
        }
    }

    fun hideEmailMismatchModal() { _showEmailMismatchModal.value = false }
    fun hideEmailDuplicateModal() { _showEmailDuplicateModal.value = false }

    // Password Step
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    fun setPassword(value: String) { _password.value = value }
    fun setConfirmPassword(value: String) { _confirmPassword.value = value }

    fun passwordNext() {
        if (_password.value.length >= 8 && _confirmPassword.value == _password.value) {
            _passwordError.value = null
            goTo(Step.PHONE)
        } else {
            _passwordError.value = "비밀번호가 일치하지 않거나 8자 미만입니다."
        }
    }

    // Phone Step
    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _phoneCode = MutableStateFlow("")
    val phoneCode: StateFlow<String> = _phoneCode.asStateFlow()

    private val _showPhoneCodeField = MutableStateFlow(false)
    val showPhoneCodeField: StateFlow<Boolean> = _showPhoneCodeField.asStateFlow()

    private val _phoneCodeVerified = MutableStateFlow(false)
    val phoneCodeVerified: StateFlow<Boolean> = _phoneCodeVerified.asStateFlow()

    private val _showDealerField = MutableStateFlow(false)
    val showDealerField: StateFlow<Boolean> = _showDealerField.asStateFlow()

    private val _dealerNumber = MutableStateFlow("")
    val dealerNumber: StateFlow<String> = _dealerNumber.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()

    fun setPhone(value: String) { _phone.value = value }
    fun setPhoneCode(value: String) { _phoneCode.value = value.filter { it.isDigit() } }
    fun setDealerNumber(value: String) { _dealerNumber.value = value.filter { it.isDigit() } }
    fun showPhoneCodeField() { _showPhoneCodeField.value = true }

    fun phoneNext() {
        val hasPhone = _phone.value.filter { it.isDigit() }.let { it.length == 10 || it.length == 11 }
        val codeOK = _phoneCode.value == "0000"

        if (hasPhone && codeOK) {
            _phoneError.value = null
            _phoneCodeVerified.value = true
            if (_userType.value == UserType.DEALER) {
                _showDealerField.value = true
            } else {
                goTo(Step.NICKNAME)
            }
        } else {
            _phoneError.value = "인증번호 또는 휴대폰 번호를 확인하세요."
        }
    }

    fun dealerNext() {
        if (_dealerNumber.value == "0000") {
            _phoneError.value = null
            goTo(Step.NICKNAME)
        } else {
            _phoneError.value = "딜러 번호가 올바르지 않습니다. (0000)"
        }
    }

    // Nickname Step
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<android.net.Uri?>(null)
    val selectedImageUri: StateFlow<android.net.Uri?> = _selectedImageUri.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    fun setNickname(value: String) { _nickname.value = value }
    fun setSelectedImageUri(uri: android.net.Uri?) { _selectedImageUri.value = uri }

    fun nicknameNext() {
        if (_nickname.value.trim().length < 2) {
            _submitError.value = "닉네임은 2자 이상이어야 합니다."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _submitError.value = null

            val roleValue = when (_userType.value) {
                UserType.DEALER -> "DEALER"
                else -> "USER"
            }

            val dealershipName = if (_userType.value == UserType.DEALER) "캠픽딜러" else ""
            val dealershipRegNo = if (_userType.value == UserType.DEALER) _dealerNumber.value else ""

            val mobileNumber = formatPhoneWithDash(_phone.value)

            val result = AuthAPI.signup(
                email = _email.value,
                password = _password.value,
                checkedPassword = _password.value,
                nickname = _nickname.value,
                mobileNumber = mobileNumber,
                role = roleValue,
                dealershipName = dealershipName,
                dealershipRegistrationNumber = dealershipRegNo
            )

            when (result) {
                is ApiResult.Success -> {
                    _isSubmitting.value = false
                    goTo(Step.COMPLETE)
                }
                is ApiResult.Error -> {
                    _isSubmitting.value = false
                    _submitError.value = result.message
                }
            }
        }
    }

    private fun formatPhoneWithDash(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when (digits.length) {
            11 -> {
                val a = digits.substring(0, 3)
                val b = digits.substring(3, 7)
                val c = digits.substring(7)
                "$a-$b-$c"
            }
            10 -> {
                val a = digits.substring(0, 3)
                val b = digits.substring(3, 6)
                val c = digits.substring(6)
                "$a-$b-$c"
            }
            else -> phone
        }
    }

    // Navigation
    private fun goTo(nextStep: Step) {
        _prevProgress.value = when (_step.value) {
            Step.EMAIL -> 0.25f
            Step.PASSWORD -> 0.5f
            Step.PHONE -> 0.75f
            Step.NICKNAME -> 0.9f
            Step.COMPLETE -> 1.0f
        }
        _step.value = nextStep
    }

    fun goBack(dismiss: () -> Unit) {
        when (_step.value) {
            Step.EMAIL -> dismiss()
            Step.PASSWORD -> goTo(Step.EMAIL)
            Step.PHONE -> goTo(Step.PASSWORD)
            Step.NICKNAME -> goTo(Step.PHONE)
            Step.COMPLETE -> goTo(Step.NICKNAME)
        }
    }
}