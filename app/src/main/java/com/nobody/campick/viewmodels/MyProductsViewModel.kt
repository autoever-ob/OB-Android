package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.Product
import com.nobody.campick.services.ProfileService
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyProductsViewModel : ViewModel() {

    private val _vehicles = MutableStateFlow<List<Product>>(emptyList())
    val vehicles: StateFlow<List<Product>> = _vehicles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentPage = 0
    private var hasMore = true

    fun loadProducts(memberId: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = ProfileService.fetchMemberProducts(memberId, currentPage, 20)) {
                is ApiResult.Success -> {
                    val products = result.data.content

                    if (currentPage == 0) {
                        _vehicles.value = products
                    } else {
                        _vehicles.value = _vehicles.value + products
                    }

                    hasMore = !result.data.last
                    currentPage++
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun loadMore(memberId: String) {
        if (hasMore && !_isLoading.value) {
            loadProducts(memberId)
        }
    }
}