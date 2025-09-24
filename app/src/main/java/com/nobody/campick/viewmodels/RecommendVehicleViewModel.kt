package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.home.RecommendedVehicle
import com.nobody.campick.models.home.RecommendedVehicleStatus
import com.nobody.campick.services.ProductApi
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendVehicleViewModel(
    private val service: ProductApi = ProductApi
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<RecommendedVehicle>>(emptyList())
    val vehicles: StateFlow<List<RecommendedVehicle>> = _vehicles

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadRecommendations() {
        viewModelScope.launch {
            when (val result = service.getRecommendedVehicles()) {
                is ApiResult.Success -> {
                    _vehicles.value = result.data
                    android.util.Log.d("추천매물 조회", "Loaded vehicles: ${result.data}")
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    android.util.Log.e("추천매물 조회", "Error: ${result.message}")
                }
            }
        }
    }


    fun toggleLike(productId: Int) {
        viewModelScope.launch {
            when (val result = service.toggleLike(productId.toString())) {
                is ApiResult.Success -> {
                    _vehicles.value = _vehicles.value.map { vehicle ->
                        if (vehicle.productId == productId) {
                            val wasLiked = vehicle.isLiked
                            vehicle.copy(
                                isLiked = !wasLiked,
                                likeCount = if (wasLiked) vehicle.likeCount - 1 else vehicle.likeCount + 1
                            )
                        } else vehicle
                    }
                    android.util.Log.d("추천매물 좋아요", "Toggled like for productId: $productId")
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    android.util.Log.e("추천매물 좋아요", "Error toggling like for productId $productId: ${result.message}")
                }
            }
        }
    }
}