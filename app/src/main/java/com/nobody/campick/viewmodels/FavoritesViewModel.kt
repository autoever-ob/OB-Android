package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import com.nobody.campick.models.vehicle.Vehicle
import com.nobody.campick.models.vehicle.VehicleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesViewModel : ViewModel() {

    private val _favorites = MutableStateFlow<List<Vehicle>>(emptyList())
    val favorites: StateFlow<List<Vehicle>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
        // Mock data matching Swift implementation
        _favorites.value = listOf(
            Vehicle(
                id = "f1",
                imageName = "testImage1",
                thumbnailURL = null,
                title = "현대 포레스트",
                price = "8,900만원",
                year = "2022년",
                mileage = "15,000km",
                fuelType = "-",
                transmission = "-",
                location = "서울",
                status = VehicleStatus.ACTIVE,
                postedDate = null,
                isOnSale = true,
                isFavorite = true
            ),
            Vehicle(
                id = "f2",
                imageName = "testImage2",
                thumbnailURL = null,
                title = "기아 봉고 캠퍼",
                price = "4,200만원",
                year = "2021년",
                mileage = "32,000km",
                fuelType = "-",
                transmission = "-",
                location = "부산",
                status = VehicleStatus.RESERVED,
                postedDate = null,
                isOnSale = true,
                isFavorite = true
            ),
            Vehicle(
                id = "f3",
                imageName = "testImage3",
                thumbnailURL = null,
                title = "스타리아 캠퍼",
                price = "7,200만원",
                year = "2023년",
                mileage = "8,000km",
                fuelType = "-",
                transmission = "-",
                location = "인천",
                status = VehicleStatus.ACTIVE,
                postedDate = null,
                isOnSale = true,
                isFavorite = true
            )
        )
    }
}