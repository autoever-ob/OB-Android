package com.nobody.campick.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.FavoritesViewModel
import com.nobody.campick.views.components.TopBarView
import com.nobody.campick.views.components.VehicleCardView

@Composable
fun FavoritesView(
    onBackClick: (() -> Unit)? = null,
    onVehicleClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                title = "찜 목록",
                onBackClick = onBackClick
            )

            // Divider
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = AppColors.brandWhite20
            )

            // Content
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
            } else if (favorites.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "찜한 매물이 없습니다",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "관심있는 매물을 찜해보세요",
                            color = AppColors.brandWhite70,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Favorites Grid
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
                    items(favorites) { vehicle ->
                        VehicleCardView(
                            vehicle = vehicle,
                            onCardClick = onVehicleClick,
                            onFavoriteClick = { vehicleId ->
                                viewModel.removeFavorite(vehicleId)
                            }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }
}