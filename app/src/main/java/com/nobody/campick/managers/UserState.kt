package com.nobody.campick.managers

import com.nobody.campick.models.auth.UserDTO
import com.nobody.campick.services.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS의 UserState.swift와 동일한 역할을 하는 Android 구현
 * 사용자 로그인 상태와 정보를 관리하는 Singleton 클래스
 */
object UserState {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _nickName = MutableStateFlow("")
    val nickName: StateFlow<String> = _nickName.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _memberId = MutableStateFlow("")
    val memberId: StateFlow<String> = _memberId.asStateFlow()

    private val _dealerId = MutableStateFlow("")
    val dealerId: StateFlow<String> = _dealerId.asStateFlow()

    private val _role = MutableStateFlow("")
    val role: StateFlow<String> = _role.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _profileImageUrl = MutableStateFlow("")
    val profileImageUrl: StateFlow<String> = _profileImageUrl.asStateFlow()

    private val _joinDate = MutableStateFlow("")
    val joinDate: StateFlow<String> = _joinDate.asStateFlow()

    fun initialize() {
        loadUserData()

        val hasToken = TokenManager.getAccessToken().isNotEmpty()
        val hasUserData = _memberId.value.isNotEmpty()
        _isLoggedIn.value = hasToken && hasUserData

        println("🔍 UserState.init - token: ${if (hasToken) "있음" else "없음"}, memberId: ${if (_memberId.value.isEmpty()) "없음" else _memberId.value}, isLoggedIn: ${_isLoggedIn.value}")
    }

    fun loadUserData() {
        _name.value = UserPreferences.getString("name") ?: ""
        _nickName.value = UserPreferences.getString("nickName") ?: ""
        _phoneNumber.value = UserPreferences.getString("phoneNumber") ?: ""
        _memberId.value = UserPreferences.getString("memberId") ?: ""
        _dealerId.value = UserPreferences.getString("dealerId") ?: ""
        _role.value = UserPreferences.getString("role") ?: ""
        _email.value = UserPreferences.getString("email") ?: ""
        _profileImageUrl.value = UserPreferences.getString("profileImageUrl") ?: ""
        _joinDate.value = UserPreferences.getString("joinDate") ?: ""
    }

    fun saveUserData(
        name: String,
        nickName: String,
        phoneNumber: String,
        memberId: String,
        dealerId: String,
        role: String,
        email: String = "",
        profileImageUrl: String = "",
        joinDate: String = ""
    ) {
        _name.value = name
        _nickName.value = nickName
        _phoneNumber.value = phoneNumber
        _memberId.value = memberId
        _dealerId.value = dealerId
        _role.value = role
        _email.value = email
        _profileImageUrl.value = profileImageUrl
        _joinDate.value = joinDate

        UserPreferences.setString(name, "name")
        UserPreferences.setString(nickName, "nickName")
        UserPreferences.setString(phoneNumber, "phoneNumber")
        UserPreferences.setString(memberId, "memberId")
        UserPreferences.setString(dealerId, "dealerId")
        UserPreferences.setString(role, "role")
        UserPreferences.setString(email, "email")
        UserPreferences.setString(profileImageUrl, "profileImageUrl")
        UserPreferences.setString(joinDate, "joinDate")

        _isLoggedIn.value = true
    }

    fun applyUserDTO(dto: UserDTO?) {
        dto ?: return

        val nameValue = dto.name ?: dto.nickname ?: ""
        val nickValue = dto.nickname ?: dto.name ?: ""
        val phoneValue = dto.mobileNumber ?: ""
        val memberIdValue = dto.memberId ?: dto.id ?: ""
        val dealerValue = dto.dealerId ?: ""
        val roleValue = dto.role ?: ""
        val emailValue = dto.email ?: ""
        val profileImageValue = dto.resolvedProfileImageURL ?: ""
        val joinValue = dto.createdAt ?: ""

        saveUserData(
            name = nameValue,
            nickName = nickValue,
            phoneNumber = phoneValue,
            memberId = memberIdValue,
            dealerId = dealerValue,
            role = roleValue,
            email = emailValue,
            profileImageUrl = profileImageValue,
            joinDate = joinValue
        )
    }

    fun saveToken(accessToken: String) {
        TokenManager.saveAccessToken(accessToken)

        if (_memberId.value.isNotEmpty()) {
            _isLoggedIn.value = true
        }
    }

    fun updateProfileImage(url: String) {
        _profileImageUrl.value = url
        UserPreferences.setString(url, "profileImageUrl")
    }

    fun logout() {
        // Clear keychain token
        TokenManager.clearAll()

        // Clear local storage
        UserPreferences.removeValue("name")
        UserPreferences.removeValue("nickName")
        UserPreferences.removeValue("phoneNumber")
        UserPreferences.removeValue("memberId")
        UserPreferences.removeValue("dealerId")
        UserPreferences.removeValue("role")
        UserPreferences.removeValue("email")
        UserPreferences.removeValue("profileImageUrl")
        UserPreferences.removeValue("joinDate")

        // Clear state
        _name.value = ""
        _nickName.value = ""
        _phoneNumber.value = ""
        _memberId.value = ""
        _dealerId.value = ""
        _role.value = ""
        _email.value = ""
        _profileImageUrl.value = ""
        _joinDate.value = ""
        _isLoggedIn.value = false
    }
}