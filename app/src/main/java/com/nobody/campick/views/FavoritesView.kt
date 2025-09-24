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
import com.nobody.campick.views.components.VehicleCardView
import com.nobody.campick.views.components.TopBarView

@Composable
fun FavoritesView(
    onBackClick: (() -> Unit)? = null,
    onVehicleClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background),
        verticalArrangement = Arrangement.Top
    ) {
        // 헤더
        TopBarView(
            title = "찜"
        )

        // iOS와 완전히 동일한 상단 구분선
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.12f)
        )

        if (isLoading) {
            // Swift와 동일한 스켈레톤 UI
            LazyVerticalGrid(
                columns = GridCells.Adaptive(300.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Swift와 동일하게 4개의 스켈레톤 카드 표시
                items(count = 4) {
                    com.nobody.campick.views.components.VehicleCardSkeleton()
                }
            }
        } else if (favorites.isEmpty()) {
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
            // iOS와 완전히 동일한 그리드 구조
            LazyVerticalGrid(
                columns = GridCells.Adaptive(300.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 12.dp
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

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }
}