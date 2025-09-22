package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.vehicle.FilterOptions
import com.nobody.campick.models.vehicle.SortOption
import com.nobody.campick.models.vehicle.Vehicle
import com.nobody.campick.models.vehicle.VehicleStatus
import com.nobody.campick.services.ProductApi
import com.nobody.campick.services.VehicleMapper
import com.nobody.campick.services.VehicleService
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class FindVehicleViewModel : ViewModel() {

    // UI State
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _showingFilter = MutableStateFlow(false)
    val showingFilter: StateFlow<Boolean> = _showingFilter.asStateFlow()

    private val _showingSortView = MutableStateFlow(false)
    val showingSortView: StateFlow<Boolean> = _showingSortView.asStateFlow()

    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.RECENTLY_ADDED)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    // Data
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Load mock data initially
        loadMockData()
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun onSubmitQuery() {
        fetchVehicles()
    }

    fun onChangeFilter(newFilters: FilterOptions) {
        _filterOptions.value = newFilters
        fetchVehicles()
    }

    fun onChangeFilter() {
        fetchVehicles()
    }

    fun onChangeSort(newSort: SortOption) {
        _selectedSort.value = newSort
        fetchVehicles()
    }

    fun onChangeSort() {
        fetchVehicles()
    }

    fun showFilter() {
        _showingFilter.value = true
        fetchAvailableOptions()
    }

    fun hideFilter() {
        _showingFilter.value = false
    }

    fun showSortView() {
        _showingSortView.value = true
    }

    fun hideSortView() {
        _showingSortView.value = false
    }

    fun onAppear() {
        fetchVehicles()
    }

    private fun fetchAvailableOptions() {
        viewModelScope.launch {
            try {
                when (val result = VehicleService.fetchProductInfo()) {
                    is ApiResult.Success -> {
                        val availableOptions = result.data.option
                        _filterOptions.value = _filterOptions.value.copy(
                            availableOptions = availableOptions
                        )
                    }
                    is ApiResult.Error -> {
                        // Fall back to mock options on API failure
                        val mockOptions = listOf("샤워시설", "화장실", "침대", "주방", "에어컨", "난방", "TV", "냉장고")
                        _filterOptions.value = _filterOptions.value.copy(
                            availableOptions = mockOptions
                        )
                    }
                }
            } catch (e: Exception) {
                // Network failure - fall back to mock options for development
                val mockOptions = listOf("샤워시설", "화장실", "침대", "주방", "에어컨", "난방", "TV", "냉장고")
                _filterOptions.value = _filterOptions.value.copy(
                    availableOptions = mockOptions
                )
            }
        }
    }

    private fun fetchVehicles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch vehicles from API
                var mapped = when (val result = ProductApi.fetchProducts(page = 0, size = 30)) {
                    is ApiResult.Success -> {
                        result.data.content.map { VehicleMapper.mapToVehicle(it) }
                    }
                    is ApiResult.Error -> {
                        // Fall back to mock data on API failure
                        getMockVehicles()
                    }
                }

                // Client-side search filter
                val q = _query.value.trim().lowercase()
                if (q.isNotEmpty()) {
                    mapped = mapped.filter { vehicle ->
                        vehicle.title.lowercase().contains(q) || vehicle.location.lowercase().contains(q)
                    }
                }

                // Apply filter options (price/mileage/year/vehicleTypes/options)
                mapped = mapped.filter { vehicle ->
                    val price = priceValue(vehicle.price)
                    val mileage = mileageValue(vehicle.mileage)
                    val year = yearValue(vehicle.year)

                    val priceOK = price in _filterOptions.value.priceRange.start.toInt().._filterOptions.value.priceRange.endInclusive.toInt()
                    val mileageOK = mileage in _filterOptions.value.mileageRange.start.toInt().._filterOptions.value.mileageRange.endInclusive.toInt()
                    val yearOK = year in _filterOptions.value.yearRange.start.toInt().._filterOptions.value.yearRange.endInclusive.toInt()

                    // Vehicle type filter
                    val vehicleTypeOK = _filterOptions.value.selectedVehicleTypes.isEmpty() ||
                        _filterOptions.value.selectedVehicleTypes.any { selectedType ->
                            vehicle.title.contains(selectedType, ignoreCase = true)
                        }

                    // Options filter - for now we'll skip this as Vehicle model doesn't have options field
                    // In real implementation, this would check vehicle.options against selectedOptions
                    val optionsOK = true // _filterOptions.value.selectedOptions.isEmpty() || vehicle.options.containsAll(_filterOptions.value.selectedOptions)

                    priceOK && mileageOK && yearOK && vehicleTypeOK && optionsOK
                }

                // Apply sorting
                val sorted = when (_selectedSort.value) {
                    SortOption.RECENTLY_ADDED -> mapped
                    SortOption.LOW_PRICE -> mapped.sortedBy { priceValue(it.price) }
                    SortOption.HIGH_PRICE -> mapped.sortedByDescending { priceValue(it.price) }
                    SortOption.LOW_MILEAGE -> mapped.sortedBy { mileageValue(it.mileage) }
                    SortOption.NEWEST_YEAR -> mapped.sortedByDescending { yearValue(it.year) }
                }

                _vehicles.value = sorted

            } catch (e: Exception) {
                // Network failure - fall back to mock data for development
                _vehicles.value = getMockVehicles().let { vehicles ->
                    // Apply same filtering logic to mock data
                    var filtered = vehicles

                    // Search filter
                    val q = _query.value.trim().lowercase()
                    if (q.isNotEmpty()) {
                        filtered = filtered.filter { vehicle ->
                            vehicle.title.lowercase().contains(q) || vehicle.location.lowercase().contains(q)
                        }
                    }

                    // Apply filter options
                    filtered = filtered.filter { vehicle ->
                        val price = priceValue(vehicle.price)
                        val mileage = mileageValue(vehicle.mileage)
                        val year = yearValue(vehicle.year)

                        val priceOK = price in _filterOptions.value.priceRange.start.toInt().._filterOptions.value.priceRange.endInclusive.toInt()
                        val mileageOK = mileage in _filterOptions.value.mileageRange.start.toInt().._filterOptions.value.mileageRange.endInclusive.toInt()
                        val yearOK = year in _filterOptions.value.yearRange.start.toInt().._filterOptions.value.yearRange.endInclusive.toInt()

                        // Vehicle type filter
                        val vehicleTypeOK = _filterOptions.value.selectedVehicleTypes.isEmpty() ||
                            _filterOptions.value.selectedVehicleTypes.any { selectedType ->
                                vehicle.title.contains(selectedType, ignoreCase = true)
                            }

                        // Options filter - for now we'll skip this as Vehicle model doesn't have options field
                        val optionsOK = true

                        priceOK && mileageOK && yearOK && vehicleTypeOK && optionsOK
                    }

                    // Apply sorting
                    when (_selectedSort.value) {
                        SortOption.RECENTLY_ADDED -> filtered
                        SortOption.LOW_PRICE -> filtered.sortedBy { priceValue(it.price) }
                        SortOption.HIGH_PRICE -> filtered.sortedByDescending { priceValue(it.price) }
                        SortOption.LOW_MILEAGE -> filtered.sortedBy { mileageValue(it.mileage) }
                        SortOption.NEWEST_YEAR -> filtered.sortedByDescending { yearValue(it.year) }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // MARK: - Parsing helpers
    private fun digits(from: String): Int {
        val numbers = from.filter { it.isDigit() }
        return numbers.toIntOrNull() ?: 0
    }

    private fun priceValue(s: String): Int = digits(s)

    private fun mileageValue(s: String): Int {
        val normalized = s.replace(" ", "")
            .replace(",", "")
            .lowercase()

        if (normalized.contains("만")) {
            val numericString = normalized
                .replace("만km", "")
                .replace("만", "")
                .replace("km", "")
                .filter { it.isDigit() || it == '.' }
            return numericString.toDoubleOrNull()?.times(10000)?.toInt() ?: 0
        }

        val numericString = normalized.replace("km", "").filter { it.isDigit() }
        return numericString.toIntOrNull() ?: 0
    }

    private fun yearValue(s: String): Int = digits(s)

    private fun formatMileage(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "-"

        val normalized = trimmed.replace(",", "")
            .replace(" ", "")
            .replace("KM", "km")

        if (normalized.lowercase().contains("만")) {
            val numericString = normalized.lowercase()
                .replace("만km", "")
                .replace("만", "")
                .replace("km", "")
                .filter { it.isDigit() || it == '.' }
            return numericString.toDoubleOrNull()?.let { value ->
                "${formatManValue(value)}만km"
            } ?: (if (normalized.endsWith("km")) normalized else "${normalized}km")
        }

        val sanitized = normalized.replace("km", "")
        val numericString = sanitized.filter { it.isDigit() || it == '.' }

        val rawValue = numericString.toDoubleOrNull() ?: return trimmed

        if (sanitized.contains(".") && rawValue < 1000) {
            return "${formatManValue(rawValue)}만km"
        }

        if (rawValue >= 10000) {
            val manValue = rawValue / 10000.0
            return "${formatManValue(manValue)}만km"
        }

        val formatter = NumberFormat.getNumberInstance(Locale("ko", "KR"))
        formatter.maximumFractionDigits = 0
        val formatted = formatter.format(rawValue.toInt())
        return "${formatted}km"
    }

    private fun formatManValue(value: Double): String {
        val scaled = (value * 10).let { kotlin.math.round(it) } / 10
        return if (kotlin.math.abs(scaled - kotlin.math.round(scaled)) < 0.0001) {
            String.format("%.0f", scaled)
        } else {
            String.format("%.1f", scaled)
        }
    }

    // Mock data for testing
    private fun getMockVehicles(): List<Vehicle> {
        return listOf(
            Vehicle(
                id = "1",
                imageName = "testImage1",
                thumbnailURL = null,
                title = "현대 포레스트",
                price = "8,900만원",
                year = "2022년",
                mileage = "15,000km",
                fuelType = "디젤",
                transmission = "자동",
                location = "서울",
                status = VehicleStatus.ACTIVE,
                postedDate = null,
                isOnSale = true,
                isFavorite = false
            ),
            Vehicle(
                id = "2",
                imageName = "testImage2",
                thumbnailURL = null,
                title = "기아 봉고 캠퍼",
                price = "4,200만원",
                year = "2021년",
                mileage = "32,000km",
                fuelType = "디젤",
                transmission = "수동",
                location = "부산",
                status = VehicleStatus.RESERVED,
                postedDate = null,
                isOnSale = true,
                isFavorite = true
            ),
            Vehicle(
                id = "3",
                imageName = "testImage3",
                thumbnailURL = null,
                title = "스타리아 캠퍼",
                price = "7,200만원",
                year = "2023년",
                mileage = "8,000km",
                fuelType = "가솔린",
                transmission = "자동",
                location = "인천",
                status = VehicleStatus.ACTIVE,
                postedDate = null,
                isOnSale = true,
                isFavorite = false
            ),
            Vehicle(
                id = "4",
                imageName = "testImage1",
                thumbnailURL = null,
                title = "벤츠 스프린터",
                price = "12,500만원",
                year = "2024년",
                mileage = "5,000km",
                fuelType = "디젤",
                transmission = "자동",
                location = "경기",
                status = VehicleStatus.SOLD,
                postedDate = null,
                isOnSale = false,
                isFavorite = false
            )
        )
    }

    private fun loadMockData() {
        _vehicles.value = getMockVehicles()
    }
}