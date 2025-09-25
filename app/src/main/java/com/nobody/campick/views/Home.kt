package com.nobody.campick.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nobody.campick.activities.MainTabActivity
import com.nobody.campick.repositories.FilterRepository
import com.nobody.campick.components.home.BottomBanner
import com.nobody.campick.components.home.FindVehicle
import com.nobody.campick.components.home.Header
import com.nobody.campick.components.home.ProfileMenu
import com.nobody.campick.components.home.RecommendVehicle
import com.nobody.campick.components.home.AutoSlidingBanner
import com.nobody.campick.components.home.VehicleCategory
import com.nobody.campick.models.home.RecommendedVehicle
import com.nobody.campick.models.home.RecommendedVehicleStatus

@Composable
fun Home() {
    var showSlideMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val navigateToFindVehicle: () -> Unit = {
        (context as? MainTabActivity)?.navigateToFindVehicle()
    }

    val navigateToFindVehicleWithFilter: (String) -> Unit = { vehicleType ->
        val currentFilters = FilterRepository.filterOptions.value
        FilterRepository.updateFilterOptions(
            currentFilters.copy(
                selectedVehicleTypes = setOf(vehicleType)
            )
        )
        navigateToFindVehicle()
    }

    val navigateToVehicleDetail: (String) -> Unit = { vehicleId ->
        val intent = com.nobody.campick.activities.VehicleDetailActivity.newIntent(
            context = context,
            vehicleId = vehicleId
        )
        context.startActivity(intent)
    }

    // 뷰모델 (웹소켓 연결용)
//    val viewModel = remember { HomeChatViewModel() }
    val vehicles = listOf(
        RecommendedVehicle(
            productId = 1,
            title = "현대 모터홈",
            price = "₩45,000,000",
            generation = 2025,
            mileage = "20,000km",
            location = "서울",
            createdAt = "2025-09-20T14:43:21",
            thumbNail = "",
            status = RecommendedVehicleStatus.AVAILABLE,
            isLiked = false,
            likeCount = 12
        ),
        RecommendedVehicle(
            productId = 2,
            title = "벤츠 스프린터",
            price = "₩80,000,000",
            mileage = "15,000km",
            generation = 2025,
            location = "부산",
            createdAt = "2025-09-21T09:15:00",
            thumbNail = "",
            status = RecommendedVehicleStatus.AVAILABLE,
            isLiked = true,
            likeCount = 34
        ),
        RecommendedVehicle(
            productId = 3,
            title = "트레일러 캠핑카",
            price = "₩25,000,000",
            mileage = "30,000km",
            generation = 2025,
            location = "대전",
            createdAt = "2025-09-19T19:00:00",
            thumbNail = "",
            status = RecommendedVehicleStatus.SOLD,
            isLiked = false,
            likeCount = 7
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B211A)) // AppColors.brandBackground 대체
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 헤더
            Header(
                showSlideMenu = showSlideMenu,
                onShowSlideMenuChange = { showSlideMenu = it },
            )

            // 컨텐츠 영역 (스크롤 가능)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AutoSlidingBanner()
                FindVehicle(onClick = navigateToFindVehicle)
                VehicleCategory(onCategoryClick = navigateToFindVehicleWithFilter)
                Spacer(modifier = Modifier.height((-24).dp))
                RecommendVehicle(
                    onVehicleClick = navigateToVehicleDetail,
                    onViewAllClick = navigateToFindVehicle
                )
                BottomBanner(
                    onDetailClick = { /* TODO: 네비게이션 처리 */ }
                )
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // 슬라이드 메뉴 (ProfileMenu)
        if (showSlideMenu) {
            ProfileMenu(
                showSlideMenu = showSlideMenu,
                onShowSlideMenuChange = { showSlideMenu = it },
                onNavigateToProfile = { /* TODO */ },
                onLogout = { /* TODO */ }
            )
        }
    }

    // onAppear 대체
    LaunchedEffect(Unit) {
//        viewModel.connectWebSocket(userId = "1")
    }
}