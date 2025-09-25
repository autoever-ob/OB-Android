package com.nobody.campick.models.vehicle

import kotlinx.serialization.Serializable

enum class SortOption(val displayName: String) {
    RECENTLY_ADDED("최근 등록순"),
    LOW_PRICE("낮은 가격순"),
    HIGH_PRICE("높은 가격순"),
    LOW_MILEAGE("주행거리 짧은순"),
    NEWEST_YEAR("최신 연식순")
}

@Serializable
data class FilterOptions(
    val priceRange: ClosedFloatingPointRange<Double> = 0.0..10000.0,
    val mileageRange: ClosedFloatingPointRange<Double> = 0.0..100000.0,
    val yearRange: ClosedFloatingPointRange<Double> = 1990.0..2024.0,
    val selectedVehicleTypes: Set<String> = emptySet(),
    val selectedOptions: Set<String> = emptySet(),
    val availableOptions: List<String> = emptyList() // API에서 받아온 옵션 목록
) {
    companion object {
        val vehicleTypes = listOf("모터홈", "카라반", "트레일러", "픽업캠퍼")
    }
}