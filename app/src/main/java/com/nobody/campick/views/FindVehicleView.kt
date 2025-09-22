package com.nobody.campick.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val selectedSort by viewModel.selectedSort.collectAsState()
    val showingFilter by viewModel.showingFilter.collectAsState()
    val showingSortView by viewModel.showingSortView.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()

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
                title = "매물 찾기",
                onBackClick = onBackClick
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

            // Divider
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                thickness = 1.dp,
                color = AppColors.brandWhite20
            )

            // Vehicle Grid
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(
                        color = AppColors.brandOrange
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(300.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 80.dp // Account for bottom tab bar
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vehicles) { vehicle ->
                        VehicleCardView(
                            vehicle = vehicle,
                            onCardClick = onVehicleClick,
                            onFavoriteClick = { vehicleId ->
                                // TODO: Handle favorite toggle
                            }
                        )
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
    }

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }
}