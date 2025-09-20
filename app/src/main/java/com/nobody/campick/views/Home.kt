package com.nobody.campick.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nobody.campick.components.home.BottomBanner
import com.nobody.campick.components.home.FindVehicle
import com.nobody.campick.components.home.Header
import com.nobody.campick.components.home.ProfileMenu
import com.nobody.campick.components.home.RecommendVehicle
import com.nobody.campick.components.home.TopBanner
import com.nobody.campick.components.home.VehicleCategory
import com.nobody.campick.components.home.VehicleModel

@Composable
fun Home() {
    var showSlideMenu by remember { mutableStateOf(false) }
    // 뷰모델 (웹소켓 연결용)
//    val viewModel = remember { HomeChatViewModel() }
    val vehicles = listOf(
        VehicleModel(1, "현대 모터홈", "2020", "20,000km", "₩45,000,000", "", false, 12),
        VehicleModel(2, "벤츠 스프린터", "2021", "15,000km", "₩80,000,000", "", true, 34),
        VehicleModel(3, "트레일러 캠핑카", "2019", "30,000km", "₩25,000,000", "", false, 7)
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
                TopBanner()
                FindVehicle()
                VehicleCategory()
                RecommendVehicle(vehicles, onAllClick = {}, onLikeClick = {})
                BottomBanner(
                    onDetailClick = { /* TODO: 네비게이션 처리 */ }
                )
                Spacer(modifier = Modifier.height(70.dp)) // safeAreaInset 대체
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