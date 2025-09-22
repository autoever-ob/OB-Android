package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.Page
import com.nobody.campick.models.Product
import com.nobody.campick.models.ProfileData
import com.nobody.campick.services.ProfileService
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

            val targetMemberId = memberId ?: "1" // 기본값으로 1 사용 (실제로는 UserState에서 가져와야 함)

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

            // 판매중 상품 조회
            when (val sellingResult = ProfileService.fetchMemberProducts(targetMemberId, 0, 10)) {
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

            // 판매완료 상품 조회
            when (val soldResult = ProfileService.fetchMemberSoldProducts(targetMemberId, 0, 10)) {
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
     * 판매중 상품 더 로드
     */
    fun loadMoreSellingProducts(memberId: String? = null) {
        viewModelScope.launch {
            val targetMemberId = memberId ?: "1"
            val nextPage = currentSellingPage + 1

            when (val result = ProfileService.fetchMemberProducts(targetMemberId, nextPage, 10)) {
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
     * 판매완료 상품 더 로드
     */
    fun loadMoreSoldProducts(memberId: String? = null) {
        viewModelScope.launch {
            val targetMemberId = memberId ?: "1"
            val nextPage = currentSoldPage + 1

            when (val result = ProfileService.fetchMemberSoldProducts(targetMemberId, nextPage, 10)) {
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
            TabType.SELLING -> sellingProductsPage?.let { !it.last } ?: false
            TabType.SOLD -> soldProductsPage?.let { !it.last } ?: false
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
}