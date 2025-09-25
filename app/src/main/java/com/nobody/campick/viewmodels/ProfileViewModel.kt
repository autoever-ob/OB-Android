package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.managers.UserState
import com.nobody.campick.models.Page
import com.nobody.campick.models.Product
import com.nobody.campick.models.ProfileData
import com.nobody.campick.services.ProfileService
import com.nobody.campick.services.ProfileImageResponse
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _profileData = MutableStateFlow<ProfileData?>(null)
    val profileData: StateFlow<ProfileData?> = _profileData.asStateFlow()

    private val _sellingProducts = MutableStateFlow<List<Product>>(emptyList())
    val sellingProducts: StateFlow<List<Product>> = _sellingProducts.asStateFlow()

    private val _soldProducts = MutableStateFlow<List<Product>>(emptyList())
    val soldProducts: StateFlow<List<Product>> = _soldProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    private val _activeTab = MutableStateFlow(TabType.SELLING)
    val activeTab: StateFlow<TabType> = _activeTab.asStateFlow()

    // 카운트 관련 StateFlow
    private val _soldProductCount = MutableStateFlow(0)
    val soldProductCount: StateFlow<Int> = _soldProductCount.asStateFlow()

    private val _sellOrReserveProductCount = MutableStateFlow(0)
    val sellOrReserveProductCount: StateFlow<Int> = _sellOrReserveProductCount.asStateFlow()

    private val _allProductCount = MutableStateFlow(0)
    val allProductCount: StateFlow<Int> = _allProductCount.asStateFlow()

    // 프로필 업데이트 관련 StateFlow
    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _profileUpdateSuccess = MutableStateFlow(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess.asStateFlow()

    // 페이지 정보
    private var sellingProductsPage: Page<Product>? = null
    private var soldProductsPage: Page<Product>? = null
    private var currentSellingPage = 0
    private var currentSoldPage = 0

    // 통계 계산 프로퍼티
    val totalListings: Int
        get() {
            val sellingCount = sellingProductsPage?.totalElements ?: 0
            val soldCount = soldProductsPage?.totalElements ?: 0
            return sellingCount + soldCount
        }

    val sellingCount: Int
        get() = sellingProductsPage?.totalElements ?: 0

    val soldCount: Int
        get() = soldProductsPage?.totalElements ?: 0

    enum class TabType(val displayText: String) {
        SELLING("판매중"),
        SOLD("판매완료")
    }

    /**
     * 프로필 로드
     */
    fun loadProfile(memberId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val targetMemberId = memberId?.takeIf { it.isNotBlank() } ?: UserState.memberId.value

            if (targetMemberId.isBlank()) {
                _errorMessage.value = "사용자 정보를 찾을 수 없습니다"
                _isLoading.value = false
                return@launch
            }

            // 프로필 정보 조회
            when (val profileResult = ProfileService.fetchMemberInfo(targetMemberId)) {
                is ApiResult.Success -> {
                    _profileData.value = profileResult.data
                }
                is ApiResult.Error -> {
                    if (isAuthError(profileResult.message)) {
                        _shouldRedirectToLogin.value = true
                        return@launch
                    } else {
                        _errorMessage.value = profileResult.message
                        return@launch
                    }
                }
            }

            // 판매중/예약중 상품 조회 (iOS와 동일한 엔드포인트 사용)
            when (val sellingResult = ProfileService.fetchMemberSellOrReserveProducts(targetMemberId, 0, 2)) {
                is ApiResult.Success -> {
                    sellingProductsPage = sellingResult.data
                    _sellingProducts.value = sellingResult.data.content
                    currentSellingPage = 0
                }
                is ApiResult.Error -> {
                    if (isAuthError(sellingResult.message)) {
                        // 401/403 오류 시 빈 배열로 처리
                        sellingProductsPage = createEmptyPage()
                        _sellingProducts.value = emptyList()
                        currentSellingPage = 0
                    } else {
                        // 기타 오류는 빈 배열로 처리 (기본 프로필은 표시)
                        sellingProductsPage = createEmptyPage()
                        _sellingProducts.value = emptyList()
                        currentSellingPage = 0
                    }
                }
            }

            // 판매완료 상품 조회 (iOS와 동일한 size=2 사용)
            when (val soldResult = ProfileService.fetchMemberSoldProducts(targetMemberId, 0, 2)) {
                is ApiResult.Success -> {
                    soldProductsPage = soldResult.data
                    _soldProducts.value = soldResult.data.content
                    currentSoldPage = 0
                }
                is ApiResult.Error -> {
                    if (isAuthError(soldResult.message)) {
                        // 401/403 오류 시 빈 배열로 처리
                        soldProductsPage = createEmptyPage()
                        _soldProducts.value = emptyList()
                        currentSoldPage = 0
                    } else {
                        // 기타 오류는 빈 배열로 처리 (기본 프로필은 표시)
                        soldProductsPage = createEmptyPage()
                        _soldProducts.value = emptyList()
                        currentSoldPage = 0
                    }
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * 판매중 상품 더 로드 (iOS와 동일한 엔드포인트)
     */
    fun loadMoreSellingProducts(memberId: String? = null) {
        viewModelScope.launch {
            val targetMemberId = memberId?.takeIf { it.isNotBlank() } ?: UserState.memberId.value
            val nextPage = currentSellingPage + 1

            when (val result = ProfileService.fetchMemberSellOrReserveProducts(targetMemberId, nextPage, 2)) {
                is ApiResult.Success -> {
                    if (result.data.content.isNotEmpty()) {
                        sellingProductsPage = result.data
                        _sellingProducts.value = _sellingProducts.value + result.data.content
                        currentSellingPage = nextPage
                    }
                }
                is ApiResult.Error -> {
                    // 더 로드 실패는 무시 (기존 데이터 유지)
                }
            }
        }
    }

    /**
     * 판매완료 상품 더 로드 (iOS와 동일한 size=2 사용)
     */
    fun loadMoreSoldProducts(memberId: String? = null) {
        viewModelScope.launch {
            val targetMemberId = memberId?.takeIf { it.isNotBlank() } ?: UserState.memberId.value
            val nextPage = currentSoldPage + 1

            when (val result = ProfileService.fetchMemberSoldProducts(targetMemberId, nextPage, 2)) {
                is ApiResult.Success -> {
                    if (result.data.content.isNotEmpty()) {
                        soldProductsPage = result.data
                        _soldProducts.value = _soldProducts.value + result.data.content
                        currentSoldPage = nextPage
                    }
                }
                is ApiResult.Error -> {
                    // 더 로드 실패는 무시 (기존 데이터 유지)
                }
            }
        }
    }

    /**
     * 프로필 새로고침
     */
    fun refreshProfile(memberId: String? = null) {
        loadProfile(memberId)
    }

    /**
     * 탭 변경
     */
    fun setActiveTab(tab: TabType) {
        _activeTab.value = tab
    }

    /**
     * 현재 활성 탭의 상품 목록 반환
     */
    fun getCurrentProducts(): List<Product> {
        return when (_activeTab.value) {
            TabType.SELLING -> _sellingProducts.value
            TabType.SOLD -> _soldProducts.value
        }
    }

    /**
     * 더 로드할 상품이 있는지 확인
     */
    fun hasMoreProducts(): Boolean {
        return when (_activeTab.value) {
            TabType.SELLING -> sellingProductsPage?.let { page ->
                page.last == false || (page.number ?: 0) < (page.totalPages - 1)
            } ?: false
            TabType.SOLD -> soldProductsPage?.let { page ->
                page.last == false || (page.number ?: 0) < (page.totalPages - 1)
            } ?: false
        }
    }

    /**
     * 에러 처리
     */
    private fun handleError(errorMessage: String) {
        when {
            isAuthError(errorMessage) -> {
                _shouldRedirectToLogin.value = true
            }
            else -> {
                _errorMessage.value = errorMessage
            }
        }
    }

    /**
     * 인증 에러 확인
     */
    private fun isAuthError(errorMessage: String): Boolean {
        return errorMessage.contains("401") || errorMessage.contains("403") || errorMessage.contains("Unauthorized")
    }

    /**
     * 빈 페이지 생성
     */
    private fun createEmptyPage(): Page<Product> {
        return Page(
            content = emptyList(),
            totalElements = 0,
            totalPages = 0,
            size = 0,
            number = 0,
            numberOfElements = 0,
            first = true,
            last = true
        )
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 로그인 리다이렉트 플래그 클리어
     */
    fun clearRedirectToLogin() {
        _shouldRedirectToLogin.value = false
    }

    /**
     * 매물 카운트 로드
     */
    fun loadProductCounts(memberId: String) {
        viewModelScope.launch {
            // 판매완료 매물 개수
            when (val result = ProfileService.getProductSoldCount(memberId)) {
                is ApiResult.Success -> _soldProductCount.value = result.data
                is ApiResult.Error -> _soldProductCount.value = 0
            }

            // 판매중/예약중 매물 개수
            when (val result = ProfileService.getProductSellOrReserveCount(memberId)) {
                is ApiResult.Success -> _sellOrReserveProductCount.value = result.data
                is ApiResult.Error -> _sellOrReserveProductCount.value = 0
            }

            // 전체 매물 개수
            when (val result = ProfileService.getProductAllCount(memberId)) {
                is ApiResult.Success -> _allProductCount.value = result.data
                is ApiResult.Error -> _allProductCount.value = 0
            }
        }
    }

    /**
     * 프로필 이미지 업로드
     */
    fun uploadProfileImage(imageData: ByteArray, onSuccess: (ProfileImageResponse) -> Unit) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            _errorMessage.value = null

            when (val result = ProfileService.uploadProfileImage(imageData)) {
                is ApiResult.Success -> {
                    _isUploadingImage.value = false
                    onSuccess(result.data)
                    // 프로필 데이터 새로고침
                    _profileData.value?.let { currentProfile ->
                        _profileData.value = currentProfile.copy(
                            profileImage = result.data.profileImageUrl
                        )
                    }
                }
                is ApiResult.Error -> {
                    _isUploadingImage.value = false
                    _errorMessage.value = "프로필 이미지 업로드에 실패했습니다: ${result.message}"
                }
            }
        }
    }

    /**
     * 프로필 정보 업데이트
     */
    fun updateProfileInfo(
        nickname: String,
        mobileNumber: String,
        description: String
    ) {
        viewModelScope.launch {
            _isUpdatingProfile.value = true
            _errorMessage.value = null

            when (val result = ProfileService.updateMemberInfo(nickname, mobileNumber, description)) {
                is ApiResult.Success -> {
                    _isUpdatingProfile.value = false
                    _profileUpdateSuccess.value = true
                    // 프로필 데이터 새로고침
                    _profileData.value?.let { currentProfile ->
                        _profileData.value = currentProfile.copy(
                            nickname = nickname,
                            description = description
                        )
                    }
                }
                is ApiResult.Error -> {
                    _isUpdatingProfile.value = false
                    _errorMessage.value = "프로필 정보 업데이트에 실패했습니다: ${result.message}"
                }
            }
        }
    }

    /**
     * 로그아웃
     */
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = ProfileService.logout()) {
                is ApiResult.Success -> {
                    // 서버 로그아웃 성공 시 전역 로그아웃 처리
                    println("🎉 서버 로그아웃 성공 - 전역 로그아웃 실행")
                    UserState.logout()

                    _isLoading.value = false
                    onSuccess()
                }
                is ApiResult.Error -> {
                    // 서버 로그아웃 실패해도 로컬 로그아웃은 실행 (iOS와 동일)
                    println("⚠️ 서버 로그아웃 실패하지만 로컬 로그아웃 실행: ${result.message}")
                    UserState.logout()

                    _isLoading.value = false
                    onSuccess() // 로컬 로그아웃은 항상 성공으로 처리
                }
            }
        }
    }

    /**
     * 회원탈퇴
     */
    fun deleteMember(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = ProfileService.deleteMember()) {
                is ApiResult.Success -> {
                    _isLoading.value = false
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = "회원탈퇴에 실패했습니다: ${result.message}"
                }
            }
        }
    }

    /**
     * 프로필 업데이트 성공 플래그 클리어
     */
    fun clearProfileUpdateSuccess() {
        _profileUpdateSuccess.value = false
    }
}