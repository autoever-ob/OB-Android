package com.nobody.campick.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.FindVehicleViewModel
import com.nobody.campick.views.components.ChipView
import com.nobody.campick.views.components.FilterView
import com.nobody.campick.views.components.SortView
import com.nobody.campick.views.components.TopBarView
import com.nobody.campick.views.components.VehicleCardView

sealed class FilterType {
    object Price : FilterType()
    object Mileage : FilterType()
    object Year : FilterType()
    object Sort : FilterType()
    data class VehicleType(val type: String) : FilterType()
    data class Option(val option: String) : FilterType()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppliedFiltersSection(
    filterOptions: com.nobody.campick.models.vehicle.FilterOptions,
    selectedSort: com.nobody.campick.models.vehicle.SortOption,
    onRemoveFilter: (FilterType) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    val appliedFilters = mutableListOf<Pair<String, FilterType>>()

    // Check price filter
    if (filterOptions.priceRange.start > 0.0 || filterOptions.priceRange.endInclusive < 10000.0) {
        val priceText = if (filterOptions.priceRange.start > 0.0 && filterOptions.priceRange.endInclusive < 10000.0) {
            "${filterOptions.priceRange.start.toInt()}만원 - ${filterOptions.priceRange.endInclusive.toInt()}만원"
        } else if (filterOptions.priceRange.start > 0.0) {
            "${filterOptions.priceRange.start.toInt()}만원 이상"
        } else {
            "${filterOptions.priceRange.endInclusive.toInt()}만원 이하"
        }
        appliedFilters.add(priceText to FilterType.Price)
    }

    // Check mileage filter
    if (filterOptions.mileageRange.start > 0.0 || filterOptions.mileageRange.endInclusive < 100000.0) {
        val mileageText = if (filterOptions.mileageRange.start > 0.0 && filterOptions.mileageRange.endInclusive < 100000.0) {
            "${filterOptions.mileageRange.start.toInt()}km - ${filterOptions.mileageRange.endInclusive.toInt()}km"
        } else if (filterOptions.mileageRange.start > 0.0) {
            "${filterOptions.mileageRange.start.toInt()}km 이상"
        } else {
            "${filterOptions.mileageRange.endInclusive.toInt()}km 이하"
        }
        appliedFilters.add(mileageText to FilterType.Mileage)
    }

    // Check year filter
    if (filterOptions.yearRange.start > 1990.0 || filterOptions.yearRange.endInclusive < 2024.0) {
        val yearText = if (filterOptions.yearRange.start > 1990.0 && filterOptions.yearRange.endInclusive < 2024.0) {
            "${filterOptions.yearRange.start.toInt()}년 - ${filterOptions.yearRange.endInclusive.toInt()}년"
        } else if (filterOptions.yearRange.start > 1990.0) {
            "${filterOptions.yearRange.start.toInt()}년 이후"
        } else {
            "${filterOptions.yearRange.endInclusive.toInt()}년 이전"
        }
        appliedFilters.add(yearText to FilterType.Year)
    }

    // Check vehicle types
    filterOptions.selectedVehicleTypes.forEach { type ->
        appliedFilters.add(type to FilterType.VehicleType(type))
    }

    // Check selected options
    filterOptions.selectedOptions.forEach { option ->
        appliedFilters.add(option to FilterType.Option(option))
    }

    // Check sort (only if not default)
    if (selectedSort != com.nobody.campick.models.vehicle.SortOption.RECENTLY_ADDED) {
        appliedFilters.add(selectedSort.displayName to FilterType.Sort)
    }

    if (appliedFilters.isNotEmpty()) {
        val maxVisibleItems = 3
        val shouldShowExpandButton = appliedFilters.size > maxVisibleItems

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Always show initial filters
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val initialFilters = if (shouldShowExpandButton) {
                    appliedFilters.take(maxVisibleItems)
                } else {
                    appliedFilters
                }

                initialFilters.forEach { (text, filterType) ->
                    FilterBadge(
                        text = text,
                        onRemove = { onRemoveFilter(filterType) },
                        filterType = filterType
                    )
                }

                // Show expand button only when there are more filters and not expanded
                if (shouldShowExpandButton && !isExpanded) {
                    FilterBadge(
                        text = "+${appliedFilters.size - maxVisibleItems}",
                        onRemove = { onExpandedChange(!isExpanded) },
                        isExpandButton = true
                    )
                }
            }

            // Animated additional filters when expanded
            if (shouldShowExpandButton) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = tween(300)
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(300)
                    )
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show remaining filters
                        appliedFilters.drop(maxVisibleItems).forEach { (text, filterType) ->
                            FilterBadge(
                                text = text,
                                onRemove = { onRemoveFilter(filterType) },
                                filterType = filterType
                            )
                        }

                        // Add collapse button at the end
                        FilterBadge(
                            text = "접기",
                            onRemove = { onExpandedChange(!isExpanded) },
                            isExpandButton = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterBadge(
    text: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isExpandButton: Boolean = false,
    filterType: FilterType? = null
) {
    // Define colors based on filter type
    data class BadgeColors(
        val backgroundColor: Color,
        val textColor: Color,
        val borderColor: Color
    )

    val colors = when {
        isExpandButton -> BadgeColors(
            backgroundColor = AppColors.brandWhite10,
            textColor = AppColors.brandWhite60,
            borderColor = AppColors.brandWhite60
        )
        else -> when (filterType) {
            FilterType.Price -> BadgeColors(
                backgroundColor = AppColors.brandLightGreen20,
                textColor = AppColors.brandLightGreen,
                borderColor = AppColors.brandLightGreen.copy(alpha = 0.3f)
            )
            FilterType.Mileage -> BadgeColors(
                backgroundColor = AppColors.brandOrange20,
                textColor = AppColors.brandOrange,
                borderColor = AppColors.brandOrange.copy(alpha = 0.3f)
            )
            FilterType.Year -> BadgeColors(
                backgroundColor = AppColors.brandLightOrange.copy(alpha = 0.2f),
                textColor = AppColors.brandLightOrange,
                borderColor = AppColors.brandLightOrange.copy(alpha = 0.3f)
            )
            is FilterType.VehicleType -> BadgeColors(
                backgroundColor = Color(0xFF8B5CF6).copy(alpha = 0.2f), // Purple
                textColor = Color(0xFF8B5CF6),
                borderColor = Color(0xFF8B5CF6).copy(alpha = 0.3f)
            )
            is FilterType.Option -> BadgeColors(
                backgroundColor = Color(0xFF06B6D4).copy(alpha = 0.2f), // Cyan
                textColor = Color(0xFF06B6D4),
                borderColor = Color(0xFF06B6D4).copy(alpha = 0.3f)
            )
            FilterType.Sort -> BadgeColors(
                backgroundColor = AppColors.brandWhite20,
                textColor = AppColors.brandWhite80,
                borderColor = AppColors.brandWhite40
            )
            else -> BadgeColors(
                backgroundColor = AppColors.brandOrange.copy(alpha = 0.1f),
                textColor = AppColors.brandOrange,
                borderColor = AppColors.brandOrange.copy(alpha = 0.3f)
            )
        }
    }
    Surface(
        modifier = modifier.clickable { onRemove() },
        shape = RoundedCornerShape(16.dp),
        color = colors.backgroundColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                color = colors.textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            if (!isExpandButton) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove filter",
                    tint = colors.textColor,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .padding(2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindVehicleView(
    onBackClick: (() -> Unit)? = null,
    onVehicleClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FindVehicleViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val showingFilter by viewModel.showingFilter.collectAsState()
    val showingSortView by viewModel.showingSortView.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()

    var isFiltersExpanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopBarView(
                title = "매물 찾기"
            )

            // Search Field
            Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 4.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.brandWhite10
                )
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    placeholder = {
                        Text(
                            text = "차량명, 지역명으로 검색",
                            color = AppColors.brandWhite60,
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            viewModel.onSubmitQuery()
                        }
                    ),
                    singleLine = true
                )
            }

            // Filter and Sort Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipView(
                    title = "필터",
                    systemImage = "line.3.horizontal.decrease.circle",
                    onClick = { viewModel.showFilter() }
                )

                Spacer(modifier = Modifier.weight(1f))

                ChipView(
                    title = selectedSort.displayName,
                    systemImage = "arrow.up.arrow.down",
                    onClick = { viewModel.showSortView() }
                )
            }

            // Applied Filters (Badges)
            AppliedFiltersSection(
                filterOptions = filterOptions,
                selectedSort = selectedSort,
                isExpanded = isFiltersExpanded,
                onExpandedChange = { isFiltersExpanded = it },
                onRemoveFilter = { filterType ->
                    when (filterType) {
                        FilterType.Price -> {
                            val newFilters = filterOptions.copy(priceRange = 0.0..10000.0)
                            viewModel.onChangeFilter(newFilters)
                        }
                        FilterType.Mileage -> {
                            val newFilters = filterOptions.copy(mileageRange = 0.0..100000.0)
                            viewModel.onChangeFilter(newFilters)
                        }
                        FilterType.Year -> {
                            val newFilters = filterOptions.copy(yearRange = 1990.0..2024.0)
                            viewModel.onChangeFilter(newFilters)
                        }
                        is FilterType.VehicleType -> {
                            val newTypes = filterOptions.selectedVehicleTypes.toMutableSet()
                            newTypes.remove(filterType.type)
                            val newFilters = filterOptions.copy(selectedVehicleTypes = newTypes)
                            viewModel.onChangeFilter(newFilters)
                        }
                        is FilterType.Option -> {
                            val newOptions = filterOptions.selectedOptions.toMutableSet()
                            newOptions.remove(filterType.option)
                            val newFilters = filterOptions.copy(selectedOptions = newOptions)
                            viewModel.onChangeFilter(newFilters)
                        }
                        FilterType.Sort -> {
                            viewModel.onChangeSort(com.nobody.campick.models.vehicle.SortOption.RECENTLY_ADDED)
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            // Divider
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                thickness = 1.dp,
                color = AppColors.brandWhite20
            )

            // Vehicle Grid
            if (isLoading) {
                // Swift와 동일한 스켈레톤 UI
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(300.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (isFiltersExpanded) {
                                isFiltersExpanded = false
                            }
                        },
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Swift와 동일하게 4개의 스켈레톤 카드 표시
                    items(count = 4) {
                        com.nobody.campick.views.components.VehicleCardSkeleton()
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(300.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            if (isFiltersExpanded) {
                                isFiltersExpanded = false
                            }
                        },
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 80.dp // Account for bottom tab bar
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(vehicles) { index, vehicle ->
                        VehicleCardView(
                            vehicle = vehicle,
                            onCardClick = onVehicleClick,
                            onFavoriteClick = { vehicleId ->
                                viewModel.toggleLike(vehicleId)
                            }
                        )

                        // 무한 스크롤 트리거
                        if (index >= vehicles.size - 2 && hasMoreData && !isLoadingMore) {
                            LaunchedEffect(index) {
                                viewModel.loadMoreVehicles()
                            }
                        }
                    }

                    // 로딩 더 많은 데이터 인디케이터
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AppColors.brandOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // FilterView Modal
    FilterView(
        filters = filterOptions,
        isPresented = showingFilter,
        onDismiss = { viewModel.hideFilter() },
        onApply = { newFilters ->
            viewModel.onChangeFilter(newFilters)
        }
    )

    // SortView Modal
    SortView(
        selectedSort = selectedSort,
        isPresented = showingSortView,
        onDismiss = { viewModel.hideSortView() },
        onSortSelected = { newSort ->
            viewModel.onChangeSort(newSort)
        }
    )

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }
}