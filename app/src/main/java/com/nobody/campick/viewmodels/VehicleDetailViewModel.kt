package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.vehicle.VehicleDetailViewData
import com.nobody.campick.services.VehicleService
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDetailViewModel : ViewModel() {

    private val _detail = MutableStateFlow<VehicleDetailViewData?>(null)
    val detail: StateFlow<VehicleDetailViewData?> = _detail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLikeLoading = MutableStateFlow(false)
    val isLikeLoading: StateFlow<Boolean> = _isLikeLoading.asStateFlow()

    fun load(productId: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = VehicleService.fetchVehicleDetail(productId)) {
                    is ApiResult.Success -> {
                        _detail.value = result.data
                    }
                    is ApiResult.Error -> {
                        // API 실패시 mock data로 fallback
                        _detail.value = VehicleDetailViewData.createMockData(productId)
                        _errorMessage.value = "네트워크 오류로 인해 임시 데이터를 표시합니다."
                    }
                }

            } catch (e: Exception) {
                // 예외 발생시에도 mock data로 fallback
                _detail.value = VehicleDetailViewData.createMockData(productId)
                _errorMessage.value = "데이터를 불러오는 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike() {
        val currentDetail = _detail.value ?: return
        if (_isLikeLoading.value) return

        viewModelScope.launch {
            _isLikeLoading.value = true

            try {
                // 즉시 UI 업데이트 (낙관적 업데이트)
                val newLikeStatus = !currentDetail.isLiked
                val newLikeCount = if (currentDetail.isLiked) {
                    maxOf(0, currentDetail.likeCount - 1)
                } else {
                    currentDetail.likeCount + 1
                }

                _detail.value = currentDetail.copy(
                    isLiked = newLikeStatus,
                    likeCount = newLikeCount
                )


                // API 호출
                when (val result = VehicleService.toggleLike(currentDetail.id)) {
                    is ApiResult.Success -> {
                        // API 성공 - UI는 이미 업데이트됨
                    }
                    is ApiResult.Error -> {
                        // API 실패 - 원래 상태로 롤백
                        _detail.value = currentDetail
                        _errorMessage.value = "좋아요 처리 중 오류가 발생했습니다."
                    }
                }

            } catch (e: Exception) {
                // 예외 발생 - 원래 상태로 롤백
                _detail.value = currentDetail
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLikeLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refresh(productId: String) {
        load(productId)
    }
}