package com.nobody.campick.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.vehicle.FilterOptions
import com.nobody.campick.models.vehicle.SortOption
import com.nobody.campick.models.vehicle.Vehicle
import com.nobody.campick.models.vehicle.VehicleStatus
import com.nobody.campick.models.product.ProductFilterRequest
import com.nobody.campick.models.product.ProductSort
import com.nobody.campick.models.product.ProductMapper
import com.nobody.campick.repositories.FilterRepository
import com.nobody.campick.services.VehicleService
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class FindVehicleViewModel() : ViewModel() {

    // UI State from Repository
    val query: StateFlow<String> = FilterRepository.query
    val filterOptions: StateFlow<FilterOptions> = FilterRepository.filterOptions
    val selectedSort: StateFlow<SortOption> = FilterRepository.selectedSort

    private val _showingFilter = MutableStateFlow(false)
    val showingFilter: StateFlow<Boolean> = _showingFilter.asStateFlow()

    private val _showingSortView = MutableStateFlow(false)
    val showingSortView: StateFlow<Boolean> = _showingSortView.asStateFlow()

    // Data
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Pagination
    private var currentPage = 0
    private val pageSize = 30  // iOS와 동일한 페이지 크기
    private var isLastPage = false

    init {
        // Load vehicles on start
        fetchVehicles()
    }

    fun updateQuery(newQuery: String) {
        FilterRepository.updateQuery(newQuery)
    }

    fun onSubmitQuery() {
        fetchVehicles()
    }

    fun onChangeFilter(newFilters: FilterOptions) {
        FilterRepository.updateFilterOptions(newFilters)
        fetchVehicles()
    }

    fun onChangeFilter() {
        fetchVehicles()
    }

    fun onChangeSort(newSort: SortOption) {
        FilterRepository.updateSelectedSort(newSort)
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
                        val currentFilters = FilterRepository.filterOptions.value
                        FilterRepository.updateFilterOptions(
                            currentFilters.copy(availableOptions = availableOptions)
                        )
                    }
                    is ApiResult.Error -> {
                        // Fall back to mock options on API failure
                        val mockOptions = listOf("샤워시설", "화장실", "침대", "주방", "에어컨", "난방", "TV", "냉장고")
                        val currentFilters = FilterRepository.filterOptions.value
                        FilterRepository.updateFilterOptions(
                            currentFilters.copy(availableOptions = mockOptions)
                        )
                    }
                }
            } catch (e: Exception) {
                // Network failure - fall back to mock options for development
                val mockOptions = listOf("샤워시설", "화장실", "침대", "주방", "에어컨", "난방", "TV", "냉장고")
                val currentFilters = FilterRepository.filterOptions.value
                FilterRepository.updateFilterOptions(
                    currentFilters.copy(availableOptions = mockOptions)
                )
            }
        }
    }

    private fun fetchVehicles() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            currentPage = 0
            isLastPage = false

            try {
                val filter = createProductFilter()
                val sort = mapSortOption(FilterRepository.selectedSort.value)

                println("🔍 Fetching vehicles - page: $currentPage, size: $pageSize")
                println("🔍 Keyword: ${filter.keyword}")
                println("🔍 Filter: mileage=${filter.mileageFrom}-${filter.mileageTo}, cost=${filter.costFrom}-${filter.costTo}, year=${filter.generationFrom}-${filter.generationTo}, types=${filter.types}")
                println("🔍 Sort: ${sort.queryValue}")

                when (val result = VehicleService.fetchProducts(
                    page = currentPage,
                    size = pageSize,
                    filter = filter,
                    sort = sort
                )) {
                    is ApiResult.Success -> {
                        val vehicles = ProductMapper.toVehicleList(result.data.content)
                        println("✅ Successfully fetched ${vehicles.size} vehicles")
                        _vehicles.value = vehicles
                        _hasMoreData.value = !result.data.last
                        isLastPage = result.data.last
                    }
                    is ApiResult.Error -> {
                        println("❌ Error fetching vehicles: ${result.message}")
                        _errorMessage.value = result.message
                        // iOS와 동일: 네트워크 실패 시 빈 리스트로 설정
                        _vehicles.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다"
                _vehicles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 무한 스크롤을 위한 더 많은 데이터 로드
     */
    fun loadMoreVehicles() {
        if (_isLoadingMore.value || isLastPage || !_hasMoreData.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            try {
                val filter = createProductFilter()
                val sort = mapSortOption(FilterRepository.selectedSort.value)

                when (val result = VehicleService.fetchProducts(
                    page = currentPage + 1,
                    size = pageSize,
                    filter = filter,
                    sort = sort
                )) {
                    is ApiResult.Success -> {
                        val pageData = result.data
                        val newVehicles = ProductMapper.toVehicleList(pageData.content)
                        _vehicles.value = _vehicles.value + newVehicles
                        _hasMoreData.value = !pageData.last
                        isLastPage = pageData.last
                        currentPage++
                    }
                    is ApiResult.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "추가 데이터 로드 중 오류가 발생했습니다"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * FilterOptions를 ProductFilterRequest로 변환 (iOS 동일 로직)
     */
    private fun createProductFilter(): ProductFilterRequest {
        val filters = FilterRepository.filterOptions.value
        val searchQuery = FilterRepository.query.value

        // iOS와 동일: 항상 필터 객체 생성 (전체 범위라도 전송)
        val validTypes = filters.selectedVehicleTypes.toList()

        return ProductFilterRequest(
            keyword = if (searchQuery.isNotBlank()) searchQuery else null,
            mileageFrom = filters.mileageRange.start.toInt(),
            mileageTo = filters.mileageRange.endInclusive.toInt(),
            costFrom = (filters.priceRange.start * 10000).toInt(),
            costTo = (filters.priceRange.endInclusive * 10000).toInt(),
            generationFrom = filters.yearRange.start.toInt(),
            generationTo = filters.yearRange.endInclusive.toInt(),
            types = if (validTypes.isNotEmpty()) validTypes else null,
            options = null  // iOS는 options를 보내지 않음
        )
    }

    /**
     * SortOption을 ProductSort로 변환
     */
    private fun mapSortOption(sortOption: SortOption): ProductSort {
        return when (sortOption) {
            SortOption.RECENTLY_ADDED -> ProductSort.CREATED_AT_DESC
            SortOption.LOW_PRICE -> ProductSort.COST_ASC
            SortOption.HIGH_PRICE -> ProductSort.COST_DESC
            SortOption.LOW_MILEAGE -> ProductSort.MILEAGE_ASC
            SortOption.NEWEST_YEAR -> ProductSort.GENERATION_DESC
        }
    }

    /**
     * 좋아요/좋아요 취소 토글
     */
    fun toggleLike(vehicleId: String) {
        viewModelScope.launch {
            try {
                // 현재 차량 목록에서 해당 차량 찾기
                val currentVehicles = _vehicles.value.toMutableList()
                val vehicleIndex = currentVehicles.indexOfFirst { it.id == vehicleId }

                if (vehicleIndex != -1) {
                    val vehicle = currentVehicles[vehicleIndex]

                    // 낙관적 업데이트 (즉시 UI 업데이트)
                    val updatedVehicle = vehicle.copy(isFavorite = !vehicle.isFavorite)
                    currentVehicles[vehicleIndex] = updatedVehicle
                    _vehicles.value = currentVehicles

                    // API 호출
                    when (val result = VehicleService.toggleProductLike(vehicleId)) {
                        is ApiResult.Success -> {
                            // API 성공 - UI는 이미 업데이트됨
                        }
                        is ApiResult.Error -> {
                            // API 실패 - 원래 상태로 롤백
                            currentVehicles[vehicleIndex] = vehicle
                            _vehicles.value = currentVehicles
                            _errorMessage.value = "좋아요 처리 중 오류가 발생했습니다."
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            }
        }
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _errorMessage.value = null
    }

}