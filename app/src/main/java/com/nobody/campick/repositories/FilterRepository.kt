package com.nobody.campick.repositories

import com.nobody.campick.models.vehicle.FilterOptions
import com.nobody.campick.models.vehicle.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FilterRepository {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.RECENTLY_ADDED)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun updateFilterOptions(newFilters: FilterOptions) {
        _filterOptions.value = newFilters
    }

    fun updateSelectedSort(newSort: SortOption) {
        _selectedSort.value = newSort
    }

    fun clearAll() {
        _query.value = ""
        _filterOptions.value = FilterOptions()
        _selectedSort.value = SortOption.RECENTLY_ADDED
    }
}