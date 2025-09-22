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
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }
        }
    }
}