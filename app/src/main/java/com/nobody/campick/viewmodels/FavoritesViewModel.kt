package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.managers.UserState
import com.nobody.campick.models.vehicle.Vehicle
import com.nobody.campick.models.vehicle.VehicleStatus
import com.nobody.campick.services.VehicleService
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class FavoritesViewModel : ViewModel() {

    private val _favorites = MutableStateFlow<List<Vehicle>>(emptyList())
    val favorites: StateFlow<List<Vehicle>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadFavorites()
    }

    fun onAppear() {
        loadFavorites()
    }

    fun removeFavorite(vehicleId: String) {
        _favorites.value = _favorites.value.filter { it.id != vehicleId }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val memberId = UserState.memberId.value
            if (memberId.isBlank()) {
                _errorMessage.value = "로그인이 필요합니다"
                _isLoading.value = false
                return@launch
            }

            when (val result = VehicleService.fetchFavorites(memberId, page = 0, size = 20)) {
                is ApiResult.Success -> {
                    _favorites.value = result.data.content.map { dto ->
                        Vehicle(
                            id = dto.productId.toString(),
                            imageName = "",
                            thumbnailURL = dto.thumbnailUrl,
                            title = dto.title,
                            price = formatPriceFromString(dto.priceValue),
                            year = "${dto.generation ?: 0}년",
                            mileage = formatMileageFromString(dto.mileage),
                            fuelType = dto.fuelType.ifEmpty { "-" },
                            transmission = dto.transmission.ifEmpty { "-" },
                            location = dto.location,
                            status = when (dto.status.uppercase()) {
                                "AVAILABLE", "ACTIVE" -> VehicleStatus.ACTIVE
                                "SOLD", "COMPLETED" -> VehicleStatus.SOLD
                                "RESERVED" -> VehicleStatus.RESERVED
                                else -> VehicleStatus.ACTIVE
                            },
                            postedDate = dto.createdAt,
                            isOnSale = dto.status.uppercase() in listOf("AVAILABLE", "ACTIVE"),
                            isFavorite = dto.isLiked
                        )
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _favorites.value = emptyList()
                }
            }

            _isLoading.value = false
        }
    }

    private fun formatPriceFromString(priceStr: String): String {
        val price = priceStr.toLongOrNull() ?: 0
        return if (price > 0) {
            "${NumberFormat.getNumberInstance(Locale.getDefault()).format(price)}만원"
        } else {
            "가격 정보 없음"
        }
    }

    private fun formatMileageFromString(mileageStr: String): String {
        val mileage = mileageStr.toIntOrNull() ?: 0
        return when {
            mileage >= 10000 -> {
                val manValue = mileage / 10000.0
                val scaled = (manValue * 10).toInt() / 10.0
                if (scaled == scaled.toInt().toDouble()) {
                    "${scaled.toInt()}만km"
                } else {
                    "${String.format("%.1f", scaled)}만km"
                }
            }
            mileage > 0 -> {
                val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
                "${formatter.format(mileage)}km"
            }
            else -> "0km"
        }
    }
}